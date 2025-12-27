<script setup lang="ts">
import type { Note } from "@/features/notes/types/Note";
import { SentimentLabel } from "@/features/notes/types/Note";
import { computed } from "vue";
import { TrendingUp, TrendingDown, Minus, Loader2 } from "lucide-vue-next";
import Card from "@/components/shared/Card.vue";

const props = defineProps<{ note: Note }>();

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
  <Card>
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
    </div>
    <p class="text-text-primary leading-relaxed whitespace-pre-wrap">
      {{ note.content }}
    </p>
  </Card>
</template>
