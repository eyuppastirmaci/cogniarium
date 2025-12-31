<script setup lang="ts">
  import { ref, onMounted, onUnmounted, TransitionGroup, watch, computed } from 'vue';
  import { noteService } from '@/features/notes/api/noteService';
  import { websocketService } from '@/features/notes/api/websocketService';
  import type { Note, NoteUpdateMessage } from '@/features/notes/types/Note';
  import { NoteUpdateType } from '@/features/notes/types/Note';
  import NoteInput from '@/features/notes/components/NoteInput.vue';
  import NoteCard from '@/features/notes/components/NoteCard.vue';
  import SearchBar from '@/features/notes/components/SearchBar.vue';
  import EmailVerificationWarning from '@/features/auth/components/EmailVerificationWarning.vue';
  import { useAuth } from '@/features/auth/composables/useAuth';
  import { useRouter } from 'vue-router';
  import Button from '@/components/shared/Button.vue';
  import { Sparkles, LogOut } from 'lucide-vue-next';
  
  const router = useRouter();
  const { user, logout, isEmailVerified } = useAuth();
  
  const notes = ref<Note[]>([]);
  const loading = ref(false);
  const searchQuery = ref('');
  const searchResults = ref<Note[]>([]);
  const isSearching = ref(false);
  const searchDebounceTimer = ref<number | null>(null);
  
  const handleLogout = async () => {
    try {
      await logout();
      router.push('/login');
    } catch (err) {
      console.error('Logout failed:', err);
    }
  };
  
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

    // Set new timer
    searchDebounceTimer.value = window.setTimeout(() => {
      performSearch(newQuery);
    }, 300);
  });

  const clearSearch = () => {
    searchQuery.value = '';
    searchResults.value = [];
  };

  const displayedNotes = computed(() => {
    if (searchQuery.value.trim()) {
      return searchResults.value;
    }
    return notes.value;
  });

  const searchStatusMessage = computed(() => {
    if (!searchQuery.value.trim()) return '';
    if (isSearching.value) return 'Searching...';
    if (searchResults.value.length === 0) return 'No results found';
    return `${searchResults.value.length} result${searchResults.value.length === 1 ? '' : 's'} found`;
  });

  const handleSaveNote = async (content: string) => {
    if (!content.trim()) return;

    // Check email verification before attempting to save
    if (!isEmailVerified.value) {
      console.warn('Cannot save note: Email not verified');
      // The NoteInput component already shows a warning, but we can add additional feedback here if needed
      return;
    }

    loading.value = true;
    try {
      const newNote = await noteService.createNote(content);
      notes.value.unshift(newNote);
    } catch (err: any) {
      console.error('Failed to save note', err);
      // Check if error is due to email verification
      if (err.response?.status === 403 || err.response?.data?.message?.includes('email') || err.response?.data?.message?.includes('verified')) {
        // Email verification error - user will see the warning in NoteInput
        console.warn('Note creation failed: Email verification required');
      }
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const handleNoteUpdate = (message: NoteUpdateMessage) => {
    switch (message.type) {
      case NoteUpdateType.NOTE_CREATED:
        // Check if note already exists (might be added by current user)
        if (!notes.value.find(n => n.id === message.note.id)) {
          notes.value.unshift(message.note);
        }
        break;
      case NoteUpdateType.NOTE_UPDATED:
      case NoteUpdateType.SENTIMENT_UPDATE:
      case NoteUpdateType.TITLE_UPDATE:
      case NoteUpdateType.SUMMARY_UPDATE:
      case NoteUpdateType.EMBEDDING_UPDATE:
        const updateIndex = notes.value.findIndex(n => n.id === message.note.id);
        if (updateIndex !== -1) {
          notes.value[updateIndex] = message.note;
        }
        // Also update in search results if present
        const searchUpdateIndex = searchResults.value.findIndex(n => n.id === message.note.id);
        if (searchUpdateIndex !== -1) {
          searchResults.value[searchUpdateIndex] = message.note;
        }
        break;
      case NoteUpdateType.NOTE_DELETED:
        notes.value = notes.value.filter(n => n.id !== message.note.id);
        searchResults.value = searchResults.value.filter(n => n.id !== message.note.id);
        break;
    }
  };

  onMounted(() => {
    fetchNotes();
    websocketService.connect();
    websocketService.onNoteUpdate(handleNoteUpdate);
  });

  onUnmounted(() => {
    if (searchDebounceTimer.value !== null) {
      clearTimeout(searchDebounceTimer.value);
    }
    websocketService.disconnect();
  });
  </script>
  
  <template>
    <div class="h-screen bg-bg-main transition-colors duration-300 overflow-hidden flex flex-col">
      <!-- Email Verification Warning -->
      <EmailVerificationWarning />
      
      <!-- Header -->
      <header class="border-b border-border-subtle px-8 py-6 shrink-0">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <Sparkles :size="32" class="text-brand-primary" />
            <div>
              <h1 class="text-2xl font-bold text-text-primary tracking-tight">
                <span class="text-brand-primary">Cogniarium</span>
              </h1>
              <p class="text-sm text-text-secondary">Your cognitive sanctuary</p>
            </div>
          </div>
          
          <div class="flex items-center gap-4">
            <div v-if="user" class="text-right">
              <p class="text-sm font-medium text-text-primary">{{ user.email }}</p>
              <p class="text-xs text-text-secondary">{{ user.role }}</p>
            </div>
            <Button
              variant="secondary"
              @click="handleLogout"
              className="flex items-center gap-2"
            >
              <LogOut :size="16" />
              Logout
            </Button>
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

