<script setup lang="ts">
const props = defineProps<{
  className?: string;
}>();
</script>

<template>
  <div :class="['p-6 bg-surface hover:bg-opacity-80 transition-colors duration-150', props.className]">
    <slot />
  </div>
</template>

