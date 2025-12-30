<script setup lang="ts">
  import { ref, onMounted, onUnmounted, TransitionGroup, watch, computed } from 'vue';
  import { noteService } from '@/features/notes/api/noteService';
  import { websocketService } from '@/features/notes/api/websocketService';
  import type { Note, NoteUpdateMessage } from '@/features/notes/types/Note';
  import { NoteUpdateType } from '@/features/notes/types/Note';
  import NoteInput from '@/features/notes/components/NoteInput.vue';
  import NoteCard from '@/features/notes/components/NoteCard.vue';
  import SearchBar from '@/features/notes/components/SearchBar.vue';
  import { Sparkles } from 'lucide-vue-next';
  
  const notes = ref<Note[]>([]);
  const loading = ref(false);
  const searchQuery = ref('');
  const searchResults = ref<Note[]>([]);
  const isSearching = ref(false);
  const searchDebounceTimer = ref<number | null>(null);
  
  const fetchNotes = async () => {
    try {
      notes.value = await noteService.getAllNotes();
    } catch (err) {
      console.error('Failed to load notes', err);
    }
  };

  const performSearch = async (query: string) => {
    if (!query.trim()) {
      searchResults.value = [];
      return;
    }

    isSearching.value = true;
    try {
      searchResults.value = await noteService.searchNotes(query);
    } catch (err) {
      console.error('Failed to search notes', err);
      searchResults.value = [];
    } finally {
      isSearching.value = false;
    }
  };

  // Debounced search
  watch(searchQuery, (newQuery) => {
    // Clear previous timer
    if (searchDebounceTimer.value !== null) {
      clearTimeout(searchDebounceTimer.value);
    }

    // If query is empty, clear search results
    if (!newQuery.trim()) {
      searchResults.value = [];
      return;
    }

    // Set new timer for debounced search
    searchDebounceTimer.value = window.setTimeout(() => {
      performSearch(newQuery);
    }, 500); // 500ms debounce
  });

  // Computed property for displayed notes
  const displayedNotes = computed(() => {
    if (searchQuery.value.trim()) {
      return searchResults.value;
    }
    return notes.value;
  });

  // Computed property for search status message
  const searchStatusMessage = computed(() => {
    if (!searchQuery.value.trim()) {
      return null;
    }
    if (isSearching.value) {
      return 'Searching...';
    }
    const count = searchResults.value.length;
    return `${count} ${count === 1 ? 'note' : 'notes'} found`;
  });

  const clearSearch = () => {
    searchQuery.value = '';
    searchResults.value = [];
  };

  const handleWebSocketUpdate = (message: NoteUpdateMessage) => {
    const { type, note } = message;

    switch (type) {
      case NoteUpdateType.NOTE_CREATED:
        // Check if note already exists (avoid duplicates)
        const existingIndex = notes.value.findIndex(n => n.id === note.id);
        if (existingIndex === -1) {
          notes.value = [note, ...notes.value];
        }
        // If currently searching, also update search results if the new note matches
        if (searchQuery.value.trim()) {
          performSearch(searchQuery.value);
        }
        break;
      
      case NoteUpdateType.NOTE_DELETED:
        // Remove note from list
        notes.value = notes.value.filter(n => n.id !== note.id);
        // Also remove from search results if currently searching
        if (searchQuery.value.trim()) {
          searchResults.value = searchResults.value.filter(n => n.id !== note.id);
        }
        break;
      
      case NoteUpdateType.NOTE_UPDATED:
      case NoteUpdateType.SENTIMENT_UPDATE:
      case NoteUpdateType.TITLE_UPDATE:
      case NoteUpdateType.SUMMARY_UPDATE:
      case NoteUpdateType.EMBEDDING_UPDATE:
        // Update existing note
        const updateIndex = notes.value.findIndex(n => n.id === note.id);
        if (updateIndex !== -1) {
          notes.value[updateIndex] = note;
          // Create new array to trigger reactivity
          notes.value = [...notes.value];
        }
        // If currently searching, also update search results if the updated note matches
        if (searchQuery.value.trim()) {
          performSearch(searchQuery.value);
        }
        break;
    }
  };
  
  const handleSaveNote = async (text: string) => {
    loading.value = true;
    try {
      // Create note - WebSocket will handle adding it to the list
      await noteService.createNote(text);
      // Note: WebSocket will automatically add the note to the list via handleWebSocketUpdate
      // No need to fetchNotes() here as it would cause duplicate requests
    } catch (err) {
      console.error('Failed to save note', err);
      alert('Error connecting to service');
      // Only fetch notes on error to ensure UI is in sync
      await fetchNotes();
    } finally {
      loading.value = false;
    }
  };
  
  onMounted(() => {
    fetchNotes();
    
    // Setup WebSocket connection and message handler
    websocketService.onNoteUpdate(handleWebSocketUpdate);
    websocketService.connect();
  });

  onUnmounted(() => {
    websocketService.disconnect();
    // Clear search debounce timer
    if (searchDebounceTimer.value !== null) {
      clearTimeout(searchDebounceTimer.value);
    }
  });
  </script>
  
  <template>
    <div class="h-screen bg-bg-main transition-colors duration-300 overflow-hidden flex flex-col">
      <!-- Header -->
      <header class="border-b border-border-subtle px-8 py-6 shrink-0">
        <div class="flex items-center gap-3">
          <Sparkles :size="32" class="text-brand-primary" />
          <div>
            <h1 class="text-2xl font-bold text-text-primary tracking-tight">
              <span class="text-brand-primary">Cogniarium</span>
            </h1>
            <p class="text-sm text-text-secondary">Your cognitive sanctuary</p>
          </div>
        </div>
      </header>

      <!-- Main Layout: Left Input, Right Notes -->
      <div class="flex flex-1 min-h-0">
        <!-- Left Panel: Input -->
        <div class="w-1/2 border-r border-border-subtle p-8 overflow-y-auto">
          <NoteInput @save="handleSaveNote" :loading="loading" />
        </div>

        <!-- Right Panel: Notes List -->
        <div class="w-1/2 notes-scroll-container overflow-y-auto flex flex-col">
          <!-- Search Bar -->
          <SearchBar
            v-model="searchQuery"
            :status-message="searchStatusMessage"
            @clear="clearSearch"
          />

          <!-- Notes List -->
          <div class="flex-1 overflow-y-auto">
            <div v-if="displayedNotes.length === 0 && !isSearching" class="flex flex-col items-center justify-center h-full px-8">
              <p class="text-text-secondary text-lg text-center">
                <span v-if="searchQuery.trim()">No notes found matching your search.</span>
                <span v-else>No thoughts recorded yet.<br />Start writing on the left to begin your journey.</span>
              </p>
            </div>
            
            <TransitionGroup 
              v-else-if="displayedNotes.length > 0"
              name="note" 
              tag="div" 
              class="divide-y divide-border-subtle"
            >
              <NoteCard 
                v-for="note in displayedNotes" 
                :key="note.id" 
                :note="note" 
              />
            </TransitionGroup>

            <div v-else-if="isSearching" class="flex flex-col items-center justify-center h-full px-8">
              <p class="text-text-secondary text-lg text-center">Searching...</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>