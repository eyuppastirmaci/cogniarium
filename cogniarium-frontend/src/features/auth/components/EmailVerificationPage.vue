<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/features/auth/composables/useAuth';
import Button from '@/components/shared/Button.vue';
import Card from '@/components/shared/Card.vue';
import { CheckCircle2, XCircle, Mail, Loader2 } from 'lucide-vue-next';

const props = defineProps<{
  token?: string;
}>();

const router = useRouter();
const { verifyEmail, resendVerification, loading, clearError, refreshUser } = useAuth();

const verificationStatus = ref<'verifying' | 'success' | 'error'>('verifying');
const message = ref('');

onMounted(async () => {
  const token = props.token || new URLSearchParams(window.location.search).get('token');
  
  if (!token) {
    verificationStatus.value = 'error';
    message.value = 'Verification token is missing';
    return;
  }

  try {
    const response = await verifyEmail(token);
    if (response.success) {
      verificationStatus.value = 'success';
      message.value = response.message || 'Email verified successfully!';
      // Refresh user data to update emailVerified status
      await refreshUser();
      // Redirect to login after 3 seconds
      setTimeout(() => {
        router.push('/login');
      }, 3000);
    } else {
      verificationStatus.value = 'error';
      message.value = response.message || 'Email verification failed';
    }
  } catch (err: any) {
    verificationStatus.value = 'error';
    message.value = err.response?.data?.message || err.message || 'Email verification failed';
  }
});

const handleResendVerification = async () => {
  clearError();
  try {
    await resendVerification();
    message.value = 'Verification email sent successfully. Please check your inbox.';
  } catch (err: any) {
    message.value = err.response?.data?.message || err.message || 'Failed to resend verification email';
  }
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-bg-main p-4">
    <Card class="w-full max-w-md">
      <div class="p-8 text-center">
        <!-- Verifying State -->
        <div v-if="verificationStatus === 'verifying'" class="space-y-4">
          <Loader2 class="mx-auto animate-spin text-brand-primary" :size="48" />
          <h2 class="text-2xl font-bold text-text-primary">Verifying Email</h2>
          <p class="text-text-secondary">Please wait while we verify your email address...</p>
        </div>

        <!-- Success State -->
        <div v-else-if="verificationStatus === 'success'" class="space-y-4">
          <CheckCircle2 class="mx-auto text-green-500" :size="64" />
          <h2 class="text-2xl font-bold text-text-primary">Email Verified!</h2>
          <p class="text-text-secondary">{{ message }}</p>
          <p class="text-sm text-text-secondary">Redirecting to login page...</p>
        </div>

        <!-- Error State -->
        <div v-else class="space-y-4">
          <XCircle class="mx-auto text-red-500" :size="64" />
          <h2 class="text-2xl font-bold text-text-primary">Verification Failed</h2>
          <p class="text-text-secondary">{{ message }}</p>
          
          <div class="pt-4 space-y-3">
            <Button
              @click="handleResendVerification"
              :loading="loading"
              className="w-full"
            >
              <Mail :size="16" />
              Resend Verification Email
            </Button>
            
            <Button
              variant="secondary"
              @click="router.push('/login')"
              className="w-full"
            >
              Go to Login
            </Button>
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>

