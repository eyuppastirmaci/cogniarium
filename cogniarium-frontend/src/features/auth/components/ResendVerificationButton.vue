<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '@/features/auth/composables/useAuth';
import Button from '@/components/shared/Button.vue';
import { Mail, CheckCircle2 } from 'lucide-vue-next';

const { resendVerification, loading, error } = useAuth();
const success = ref(false);
const message = ref('');

const handleResend = async () => {
  success.value = false;
  message.value = '';
  
  try {
    await resendVerification();
    success.value = true;
    message.value = 'Verification email sent! Please check your inbox.';
    
    // Clear success message after 5 seconds
    setTimeout(() => {
      success.value = false;
      message.value = '';
    }, 5000);
  } catch (err: any) {
    message.value = err.response?.data?.message || err.message || 'Failed to resend verification email';
  }
};
</script>

<template>
  <div class="space-y-2">
    <Button
      variant="secondary"
      @click="handleResend"
      :loading="loading"
      :disabled="loading"
      className="w-full"
    >
      <Mail v-if="!success" :size="16" />
      <CheckCircle2 v-else class="text-green-500" :size="16" />
      <span v-if="!success">Resend Verification Email</span>
      <span v-else>Email Sent!</span>
    </Button>
    
    <!-- Success Message -->
    <p v-if="success && message" class="text-sm text-green-600 dark:text-green-400 text-center">
      {{ message }}
    </p>
    
    <!-- Error Message -->
    <p v-else-if="error && message" class="text-sm text-red-500 text-center">
      {{ message }}
    </p>
  </div>
</template>

