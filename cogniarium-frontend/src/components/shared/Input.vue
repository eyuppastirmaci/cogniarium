<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  modelValue: string;
  placeholder?: string;
  disabled?: boolean;
  rows?: number;
  className?: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const value = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});
</script>

<template>
  <textarea
    v-model="value"
    :placeholder="placeholder"
    :disabled="disabled"
    :rows="rows"
    :class="[
      'w-full p-4 bg-surface text-text-primary border border-border-subtle rounded-lg',
      'focus:ring-2 focus:ring-brand-primary focus:outline-none transition resize-none',
      'placeholder-text-secondary disabled:opacity-50 disabled:cursor-not-allowed',
      props.rows ? '' : 'flex-1',
      props.className
    ]"
  />
</template>

