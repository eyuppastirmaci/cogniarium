<script setup lang="ts">
  import { ref, onMounted, onUnmounted, TransitionGroup } from 'vue';
  import { noteService } from '@/features/notes/api/noteService';
  import { websocketService } from '@/features/notes/api/websocketService';
  import type { Note, NoteUpdateMessage } from '@/features/notes/types/Note';
  import { NoteUpdateType } from '@/features/notes/types/Note';
  import NoteInput from '@/features/notes/components/NoteInput.vue';
  import NoteCard from '@/features/notes/components/NoteCard.vue';
  import { Sparkles } from 'lucide-vue-next';
  
  const notes = ref<Note[]>([]);
  const loading = ref(false);
  
  const fetchNotes = async () => {
    try {
      notes.value = await noteService.getAllNotes();
    } catch (err) {
      console.error('Failed to load notes', err);
    }
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
        break;
      
      case NoteUpdateType.SENTIMENT_UPDATE:
      case NoteUpdateType.TITLE_UPDATE:
      case NoteUpdateType.SUMMARY_UPDATE:
        // Update existing note
        const updateIndex = notes.value.findIndex(n => n.id === note.id);
        if (updateIndex !== -1) {
          notes.value[updateIndex] = note;
          // Create new array to trigger reactivity
          notes.value = [...notes.value];
        }
        break;
    }
  };
  
  const handleSaveNote = async (text: string) => {
    loading.value = true;
    try {
      // Create note - WebSocket will handle adding it to the list
      await noteService.createNote(text);
      // Optionally refresh to get the latest state
      await fetchNotes();
    } catch (err) {
      console.error('Failed to save note', err);
      alert('Error connecting to service');
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
        <div class="w-1/2 notes-scroll-container overflow-y-auto">
          <div v-if="notes.length === 0" class="flex flex-col items-center justify-center h-full px-8">
            <p class="text-text-secondary text-lg text-center">
              No thoughts recorded yet.<br />Start writing on the left to begin your journey.
            </p>
          </div>
          
          <TransitionGroup 
            v-else 
            name="note" 
            tag="div" 
            class="divide-y divide-border-subtle"
          >
            <NoteCard 
              v-for="note in notes" 
              :key="note.id" 
              :note="note" 
            />
          </TransitionGroup>
        </div>
      </div>
    </div>
  </template>