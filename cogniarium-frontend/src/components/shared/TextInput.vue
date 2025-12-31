<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  modelValue: string;
  type?: 'text' | 'email' | 'password';
  placeholder?: string;
  disabled?: boolean;
  required?: boolean;
  className?: string;
}>(), {
  type: 'text',
  required: false
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const value = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});
</script>

<template>
  <input
    v-model="value"
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :required="required"
    :class="[
      'w-full p-3 bg-surface text-text-primary border border-border-subtle rounded-lg',
      'focus:ring-2 focus:ring-brand-primary focus:outline-none transition',
      'placeholder-text-secondary disabled:opacity-50 disabled:cursor-not-allowed',
      props.className
    ]"
  />
</template>

