<script setup lang="ts">
import { computed } from 'vue';
import { Loader2 } from 'lucide-vue-next';

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  disabled?: boolean;
  loading?: boolean;
  type?: 'button' | 'submit' | 'reset';
  className?: string;
}>(), {
  variant: 'primary',
  disabled: false,
  loading: false,
  type: 'button'
});

const emit = defineEmits<{
  click: [];
}>();

const buttonClasses = computed(() => {
  const base = 'px-4 py-2 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 font-medium';
  
  const variants = {
    primary: 'bg-brand-primary hover:bg-brand-hover text-white',
    secondary: 'bg-surface hover:bg-opacity-80 text-text-primary border border-border-subtle',
    danger: 'bg-red-500 hover:bg-red-600 text-white',
    ghost: 'text-text-secondary hover:text-text-primary hover:bg-surface'
  };
  
  return `${base} ${variants[props.variant]} ${props.className || ''}`;
});

const handleClick = () => {
  if (!props.disabled && !props.loading) {
    emit('click');
  }
};
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="handleClick"
  >
    <Loader2 v-if="loading" :size="16" class="animate-spin" />
    <slot />
  </button>
</template>

