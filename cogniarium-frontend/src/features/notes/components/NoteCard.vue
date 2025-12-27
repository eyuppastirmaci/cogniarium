<script setup lang="ts">
import type { Note } from "@/features/notes/types/Note";
import { SentimentLabel } from "@/features/notes/types/Note";
import { computed } from "vue";
import { TrendingUp, TrendingDown, Minus } from "lucide-vue-next";
import Card from "@/components/shared/Card.vue";

const props = defineProps<{ note: Note }>();

const badgeClasses = computed(() => {
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
  switch (props.note.sentimentLabel) {
    case SentimentLabel.POSITIVE:
      return TrendingUp;
    case SentimentLabel.NEGATIVE:
      return TrendingDown;
    default:
      return Minus;
  }
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
        <component :is="sentimentIcon" :size="12" />
        <span>{{ note.sentimentLabel }}</span>
        <span class="opacity-75">({{ (note.sentimentScore * 100).toFixed(0) }}%)</span>
      </div>
    </div>
    <p class="text-text-primary leading-relaxed whitespace-pre-wrap">
      {{ note.content }}
    </p>
  </Card>
</template>
