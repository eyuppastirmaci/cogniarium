<script setup lang="ts">
import { computed } from 'vue';
import { Search, X } from 'lucide-vue-next';

const props = defineProps<{
  modelValue: string;
  placeholder?: string;
  statusMessage?: string | null;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  'clear': [];
}>();

const value = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const clear = () => {
  emit('update:modelValue', '');
  emit('clear');
};
</script>

<template>
  <div class="p-6 border-b border-border-subtle shrink-0">
    <div class="relative">
      <Search :size="20" class="absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary" />
      <input
        v-model="value"
        type="text"
        :placeholder="placeholder || 'Search your notes...'"
        class="w-full pl-12 pr-10 py-3 bg-surface text-text-primary border border-border-subtle rounded-lg focus:ring-2 focus:ring-brand-primary focus:outline-none transition placeholder-text-secondary"
      />
      <button
        v-if="modelValue.trim()"
        @click="clear"
        class="absolute right-4 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary transition"
        aria-label="Clear search"
      >
        <X :size="18" />
      </button>
    </div>
    <!-- Search Status Message -->
    <div v-if="statusMessage" class="mt-3 text-sm text-text-secondary">
      {{ statusMessage }}
    </div>
  </div>
</template>

