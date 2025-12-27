<script setup lang="ts">
import { ref } from "vue";
import { Send, Loader2 } from "lucide-vue-next";
import Input from "@/components/shared/Input.vue";

const text = ref("");
const emit = defineEmits<{ (e: "save", text: string): void }>();
const props = defineProps<{ loading: boolean }>();

const handleSubmit = () => {
  if (!text.value.trim()) return;
  emit("save", text.value);
  text.value = "";
};
</script>

<template>
  <div class="h-full flex flex-col">
    <div class="mb-6">
      <h2 class="text-xl font-semibold text-text-primary mb-2 font-heading">New Thought</h2>
      <p class="text-sm text-text-secondary">Express your thoughts.</p>
    </div>

    <div class="flex-1 flex flex-col">
      <Input
        v-model="text"
        placeholder="What's on your mind?"
        :disabled="loading"
        class="flex-1"
      />

      <div class="mt-4 flex justify-end">
        <button
          @click="handleSubmit"
          :disabled="loading || !text.trim()"
          class="bg-brand-primary hover:bg-brand-hover text-white font-medium py-3 px-6 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 cursor-pointer"
        >
          <Loader2 v-if="loading" :size="18" class="animate-spin" />
          <Send v-else :size="18" />
          <span v-if="loading">Analyzing...</span>
          <span v-else>Save Note</span>
        </button>
      </div>
    </div>
  </div>
</template>
