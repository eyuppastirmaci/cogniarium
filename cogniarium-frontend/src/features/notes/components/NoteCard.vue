<script setup lang="ts">
import type { Note } from "@/features/notes/types/Note";
import { SentimentLabel } from "@/features/notes/types/Note";
import { computed, ref } from "vue";
import { TrendingUp, TrendingDown, Minus, Loader2, Pencil, Save, X, Trash2 } from "lucide-vue-next";
import Card from "@/components/shared/Card.vue";
import Input from "@/components/shared/Input.vue";
import Modal from "@/components/shared/Modal.vue";
import { noteService } from "@/features/notes/api/noteService";

const props = defineProps<{ note: Note }>();

const isEditing = ref(false);
const editContent = ref("");
const isSaving = ref(false);
const isDeleting = ref(false);
const showDeleteModal = ref(false);

const startEdit = () => {
  editContent.value = props.note.content;
  isEditing.value = true;
};

const cancelEdit = () => {
  isEditing.value = false;
  editContent.value = "";
};

const saveEdit = async () => {
  if (!editContent.value.trim() || editContent.value === props.note.content) {
    cancelEdit();
    return;
  }

  if (!props.note.id) {
    console.error("Cannot update note: note ID is missing");
    return;
  }

  isSaving.value = true;
  try {
    // Update note - WebSocket will handle updating it in the list
    await noteService.updateNote(props.note.id, editContent.value.trim());
    isEditing.value = false;
    editContent.value = "";
  } catch (err) {
    console.error("Failed to update note", err);
    alert("Error updating note. Please try again.");
  } finally {
    isSaving.value = false;
  }
};

const openDeleteModal = () => {
  showDeleteModal.value = true;
};

const closeDeleteModal = () => {
  showDeleteModal.value = false;
};

const deleteNote = async () => {
  if (!props.note.id) {
    console.error("Cannot delete note: note ID is missing");
    return;
  }

  isDeleting.value = true;
  try {
    // Delete note - WebSocket will handle removing it from the list
    await noteService.deleteNote(props.note.id);
    closeDeleteModal();
  } catch (err) {
    console.error("Failed to delete note", err);
    alert("Error deleting note. Please try again.");
  } finally {
    isDeleting.value = false;
  }
};

const deleteModalFooterButtons = computed(() => [
  {
    label: 'Cancel',
    variant: 'secondary' as const,
    onClick: closeDeleteModal,
    disabled: isDeleting.value
  },
  {
    label: 'Delete',
    variant: 'danger' as const,
    onClick: deleteNote,
    loading: isDeleting.value,
    disabled: isDeleting.value
  }
]);

const badgeClasses = computed(() => {
  if (!props.note.sentimentLabel) {
    return "bg-sentiment-neu-bg text-sentiment-neu-text opacity-60";
  }
  
  switch (props.note.sentimentLabel) {
    case SentimentLabel.POSITIVE:
      return "bg-sentiment-pos-bg text-sentiment-pos-text";
    case SentimentLabel.NEGATIVE:
      return "bg-sentiment-neg-bg text-sentiment-neg-text";
    default:
      return "bg-sentiment-neu-bg text-sentiment-neu-text";
  }
});

const sentimentIcon = computed(() => {
  if (!props.note.sentimentLabel) {
    return Loader2;
  }
  
  switch (props.note.sentimentLabel) {
    case SentimentLabel.POSITIVE:
      return TrendingUp;
    case SentimentLabel.NEGATIVE:
      return TrendingDown;
    default:
      return Minus;
  }
});

const sentimentText = computed(() => {
  if (!props.note.sentimentLabel) {
    return "Analyzing...";
  }
  return props.note.sentimentLabel;
});

const sentimentScore = computed(() => {
  if (props.note.sentimentScore === null || props.note.sentimentScore === undefined) {
    return null;
  }
  return (props.note.sentimentScore * 100).toFixed(0);
});

const formattedDate = computed(() => {
  const date = new Date(props.note.createdAt);
  return date.toLocaleDateString('en-US', { 
    month: 'short', 
    day: 'numeric', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
});
</script>

<template>
  <Card :class="{ 'ring-2 ring-brand-primary': isEditing }">
    <!-- Title -->
    <div v-if="note.title" class="mb-2">
      <h3 class="text-lg font-semibold text-text-primary">
        {{ note.title }}
      </h3>
    </div>
    <div v-else class="mb-2">
      <div class="flex items-center gap-2 text-text-secondary text-sm">
        <Loader2 :size="14" class="animate-spin" />
        <span class="italic">Generating title...</span>
      </div>
    </div>

    <!-- Summary -->
    <div v-if="note.summary" class="mb-3">
      <p class="text-sm text-text-secondary italic leading-relaxed">
        {{ note.summary }}
      </p>
    </div>
    <div v-else-if="note.title" class="mb-3">
      <div class="flex items-center gap-2 text-text-secondary text-xs">
        <Loader2 :size="12" class="animate-spin" />
        <span class="italic">Generating summary...</span>
      </div>
    </div>

    <div class="flex justify-between items-start mb-3">
      <span class="text-xs font-medium text-text-secondary">
        {{ formattedDate }}
      </span>

      <div class="flex items-center gap-2">
        <div
          :class="[
            'flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-full',
            badgeClasses,
          ]"
        >
          <component :is="sentimentIcon" :size="12" :class="{ 'animate-spin': !note.sentimentLabel }" />
          <span>{{ sentimentText }}</span>
          <span v-if="sentimentScore !== null" class="opacity-75">({{ sentimentScore }}%)</span>
        </div>
        
        <!-- Edit Button (only show when not editing) -->
        <button
          v-if="!isEditing"
          @click="startEdit"
          :disabled="isSaving || isDeleting"
          class="p-1.5 text-text-secondary hover:text-text-primary hover:bg-surface rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          title="Edit note"
        >
          <Pencil :size="16" />
        </button>
        
        <!-- Delete Button (only show when not editing) -->
        <button
          v-if="!isEditing"
          @click="openDeleteModal"
          :disabled="isSaving || isDeleting"
          class="p-1.5 text-text-secondary hover:text-red-500 hover:bg-surface rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          title="Delete note"
        >
          <Trash2 :size="16" />
        </button>
      </div>
    </div>

    <!-- Content: Edit Mode -->
    <div v-if="isEditing" class="space-y-3">
      <Input
        v-model="editContent"
        :disabled="isSaving"
        :rows="6"
        placeholder="Edit your note..."
        class="min-h-[120px]"
      />
      <div class="flex justify-end gap-2">
        <button
          @click="cancelEdit"
          :disabled="isSaving"
          class="px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-surface rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <X :size="16" />
          <span>Cancel</span>
        </button>
        <button
          @click="saveEdit"
          :disabled="isSaving || !editContent.trim() || editContent.trim() === note.content"
          class="px-4 py-2 bg-brand-primary hover:bg-brand-hover text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <Loader2 v-if="isSaving" :size="16" class="animate-spin" />
          <Save v-else :size="16" />
          <span>{{ isSaving ? 'Saving...' : 'Save' }}</span>
        </button>
      </div>
    </div>

    <!-- Content: View Mode -->
    <p v-else class="text-text-primary leading-relaxed whitespace-pre-wrap">
      {{ note.content }}
    </p>
  </Card>

  <!-- Delete Confirmation Modal -->
  <Modal
    v-model="showDeleteModal"
    title="Delete Note"
    content="Are you sure you want to delete this note? This action cannot be undone."
    :footer-buttons="deleteModalFooterButtons"
    :close-on-backdrop="true"
  />
</template>
