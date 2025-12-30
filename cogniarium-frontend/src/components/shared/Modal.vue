<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { X } from 'lucide-vue-next';
import Button from './Button.vue';

const props = defineProps<{
  modelValue: boolean;
  title?: string;
  content?: string;
  footerButtons?: Array<{
    label: string;
    variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
    loading?: boolean;
    disabled?: boolean;
    onClick: () => void;
  }>;
  closeOnBackdrop?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const dialogRef = ref<HTMLDialogElement | null>(null);
const isOpen = ref(props.modelValue);

const openModal = () => {
  if (dialogRef.value) {
    dialogRef.value.showModal();
    isOpen.value = true;
  }
};

const closeModal = () => {
  if (dialogRef.value) {
    dialogRef.value.close();
    isOpen.value = false;
    emit('update:modelValue', false);
  }
};

const handleBackdropClick = (event: MouseEvent) => {
  if (props.closeOnBackdrop !== false && event.target === dialogRef.value) {
    closeModal();
  }
};

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    openModal();
  } else {
    closeModal();
  }
});

onMounted(() => {
  if (props.modelValue) {
    openModal();
  }
});

onUnmounted(() => {
  closeModal();
});
</script>

<template>
  <dialog
    ref="dialogRef"
    @click="handleBackdropClick"
    class="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent border-none outline-none p-0 rounded-lg max-w-lg w-full fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
  >
    <div class="bg-surface rounded-lg shadow-xl overflow-hidden">
      <!-- Header -->
      <div v-if="title || $slots.header" class="flex items-center justify-between p-6 border-b border-border-subtle">
        <div class="flex-1">
          <slot name="header">
            <h2 v-if="title" class="text-xl font-semibold text-text-primary">
              {{ title }}
            </h2>
          </slot>
        </div>
        <button
          @click="closeModal"
          class="p-1.5 text-text-secondary hover:text-text-primary hover:bg-surface rounded transition-colors"
          title="Close"
        >
          <X :size="20" />
        </button>
      </div>

      <!-- Body -->
      <div class="p-6">
        <slot name="body">
          <p v-if="content" class="text-text-primary leading-relaxed">
            {{ content }}
          </p>
        </slot>
      </div>

      <!-- Footer -->
      <div v-if="footerButtons && footerButtons.length > 0 || $slots.footer" class="flex justify-end gap-2 p-6 border-t border-border-subtle">
        <slot name="footer">
          <template v-for="(button, index) in footerButtons" :key="index">
            <Button
              :variant="button.variant || 'secondary'"
              :disabled="button.disabled"
              :loading="button.loading"
              @click="button.onClick"
            >
              {{ button.label }}
            </Button>
          </template>
        </slot>
      </div>
    </div>
  </dialog>
</template>

