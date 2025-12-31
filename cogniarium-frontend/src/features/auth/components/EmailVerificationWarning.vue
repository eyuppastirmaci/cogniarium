<script setup lang="ts">
import { ref, watch } from 'vue';
import { useAuth } from '@/features/auth/composables/useAuth';
import ResendVerificationButton from './ResendVerificationButton.vue';
import { AlertCircle, X } from 'lucide-vue-next';

const { showEmailWarning, user } = useAuth();
const dismissed = ref(false);

const handleDismiss = () => {
  dismissed.value = true;
};

// Reset dismissed state when warning should show again
watch(showEmailWarning, (shouldShow) => {
  if (shouldShow) {
    dismissed.value = false;
  }
});
</script>

<template>
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="opacity-0 -translate-y-4"
    enter-to-class="opacity-100 translate-y-0"
    leave-active-class="transition-all duration-300 ease-in"
    leave-from-class="opacity-100 translate-y-0"
    leave-to-class="opacity-0 -translate-y-4"
  >
    <div
      v-if="showEmailWarning && !dismissed"
      class="bg-yellow-50 dark:bg-yellow-900/20 border-l-4 border-yellow-400 dark:border-yellow-500 p-4 mb-4"
    >
      <div class="flex items-start gap-3">
        <AlertCircle class="text-yellow-600 dark:text-yellow-400 mt-0.5 flex-shrink-0" :size="20" />
        
        <div class="flex-1">
          <h3 class="text-sm font-medium text-yellow-800 dark:text-yellow-200 mb-1">
            Please verify your email address
          </h3>
          <p class="text-sm text-yellow-700 dark:text-yellow-300 mb-3">
            You need to verify your email address ({{ user?.email }}) before you can create notes.
            Please check your inbox for the verification email.
          </p>
          
          <ResendVerificationButton />
        </div>
        
        <button
          @click="handleDismiss"
          class="text-yellow-600 dark:text-yellow-400 hover:text-yellow-800 dark:hover:text-yellow-200 flex-shrink-0"
          aria-label="Dismiss warning"
        >
          <X :size="18" />
        </button>
      </div>
    </div>
  </Transition>
</template>

