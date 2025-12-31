<script setup lang="ts">
import { ref, computed } from "vue";
import { Send, Loader2, AlertCircle } from "lucide-vue-next";
import Input from "@/components/shared/Input.vue";
import { useAuth } from "@/features/auth/composables/useAuth";

const text = ref("");
const emit = defineEmits<{ (e: "save", text: string): void }>();
const props = defineProps<{ loading: boolean }>();

const { isEmailVerified } = useAuth();

const isDisabled = computed(() => {
  return props.loading || !isEmailVerified.value;
});

const handleSubmit = () => {
  if (!text.value.trim() || !isEmailVerified.value) return;
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

    <!-- Email Verification Warning -->
    <div v-if="!isEmailVerified" class="mb-4 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
      <div class="flex items-start gap-3">
        <AlertCircle class="text-yellow-600 dark:text-yellow-400 shrink-0 mt-0.5" :size="20" />
        <div class="flex-1">
          <p class="text-sm font-medium text-yellow-800 dark:text-yellow-200 mb-1">
            Email Verification Required
          </p>
          <p class="text-sm text-yellow-700 dark:text-yellow-300">
            Please verify your email address before adding notes. Check your inbox for the verification link.
          </p>
        </div>
      </div>
    </div>

    <div class="flex-1 flex flex-col">
      <Input
        v-model="text"
        placeholder="What's on your mind?"
        :disabled="isDisabled"
        class="flex-1"
      />

      <div class="mt-4 flex justify-end">
        <button
          @click="handleSubmit"
          :disabled="isDisabled || !text.trim()"
          class="bg-brand-primary hover:bg-brand-hover text-white font-medium py-3 px-6 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 cursor-pointer"
        >
          <Loader2 v-if="loading" :size="18" class="animate-spin" />
          <Send v-else :size="18" />
          <span v-if="loading">Analyzing...</span>
          <span v-else-if="!isEmailVerified">Verify Email First</span>
          <span v-else>Save Note</span>
        </button>
      </div>
    </div>
  </div>
</template>
