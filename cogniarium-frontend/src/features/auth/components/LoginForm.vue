<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '@/features/auth/composables/useAuth';
import { authService } from '@/features/auth/api/authService';
import Button from '@/components/shared/Button.vue';
import TextInput from '@/components/shared/TextInput.vue';
import Card from '@/components/shared/Card.vue';
import { Mail, Lock, AlertCircle, X, CheckCircle2 } from 'lucide-vue-next';

const emit = defineEmits<{
  success: [];
  switchToRegister: [];
}>();

const { login, loading, error, clearError } = useAuth();

const email = ref('');
const password = ref('');
const showResendModal = ref(false);
const resendEmail = ref('');
const resendLoading = ref(false);
const resendError = ref<string | null>(null);
const resendSuccess = ref(false);

const handleSubmit = async (e: Event) => {
  e.preventDefault();
  clearError();
  
  if (!email.value || !password.value) {
    return;
  }

  try {
    await login(email.value, password.value);
    emit('success');
  } catch (err) {
    // Error is handled by useAuth composable
    console.error('Login failed:', err);
  }
};

const openResendModal = () => {
  showResendModal.value = true;
  resendEmail.value = email.value; // Pre-fill with login email
  resendError.value = null;
  resendSuccess.value = false;
};

const closeResendModal = () => {
  showResendModal.value = false;
  resendEmail.value = '';
  resendError.value = null;
  resendSuccess.value = false;
};

const handleResendEmail = async (e: Event) => {
  e.preventDefault();
  resendLoading.value = true;
  resendError.value = null;
  resendSuccess.value = false;

  try {
    await authService.resendVerificationEmail(resendEmail.value);
    resendSuccess.value = true;
    // Close modal after 2 seconds
    setTimeout(() => {
      closeResendModal();
    }, 2000);
  } catch (err: any) {
    resendError.value = err.response?.data?.message || err.message || 'Failed to resend verification email';
  } finally {
    resendLoading.value = false;
  }
};
</script>

<template>
  <Card class="w-full max-w-md mx-auto">
    <div class="p-8">
      <h2 class="text-2xl font-bold text-text-primary mb-6">Welcome Back</h2>
      
      <form @submit="handleSubmit" class="space-y-4">
        <!-- Email Input -->
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-2">
            Email
          </label>
          <div class="relative">
            <Mail class="absolute left-3 top-1/2 transform -translate-y-1/2 text-text-secondary" :size="18" />
            <TextInput
              v-model="email"
              type="email"
              placeholder="your@email.com"
              :disabled="loading"
              className="pl-10"
              required
            />
          </div>
        </div>

        <!-- Password Input -->
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-2">
            Password
          </label>
          <div class="relative">
            <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 text-text-secondary" :size="18" />
            <TextInput
              v-model="password"
              type="password"
              placeholder="Enter your password"
              :disabled="loading"
              className="pl-10"
              required
            />
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="flex items-center gap-2 text-red-500 text-sm">
          <AlertCircle :size="16" />
          <span>{{ error }}</span>
        </div>

        <!-- Submit Button -->
        <Button
          type="submit"
          :loading="loading"
          :disabled="!email || !password"
          className="w-full"
        >
          Sign In
        </Button>
      </form>

      <!-- Didn't receive verification email? -->
      <div class="mt-4 text-center">
        <button
          @click="openResendModal"
          class="text-sm text-brand-primary hover:text-brand-hover font-medium"
          :disabled="loading"
        >
          Didn't receive verification email?
        </button>
      </div>

      <!-- Switch to Register -->
      <div class="mt-6 text-center">
        <p class="text-sm text-text-secondary">
          Don't have an account?
          <button
            @click="emit('switchToRegister')"
            class="text-brand-primary hover:text-brand-hover font-medium ml-1"
            :disabled="loading"
          >
            Sign up
          </button>
        </p>
      </div>
    </div>
  </Card>

  <!-- Resend Verification Email Modal -->
  <div
    v-if="showResendModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="closeResendModal"
  >
    <Card class="w-full max-w-md relative">
      <button
        @click="closeResendModal"
        class="absolute top-4 right-4 text-text-secondary hover:text-text-primary"
        :disabled="resendLoading"
      >
        <X :size="20" />
      </button>

      <div class="p-6">
        <h3 class="text-xl font-bold text-text-primary mb-4">Resend Verification Email</h3>
        
        <form @submit="handleResendEmail" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-text-secondary mb-2">
              Email
            </label>
            <div class="relative">
              <Mail class="absolute left-3 top-1/2 transform -translate-y-1/2 text-text-secondary" :size="18" />
              <TextInput
                v-model="resendEmail"
                type="email"
                placeholder="your@email.com"
                :disabled="resendLoading"
                className="pl-10"
                required
              />
            </div>
          </div>

          <!-- Success Message -->
          <div v-if="resendSuccess" class="flex items-center gap-2 text-green-600 text-sm bg-green-50 p-3 rounded">
            <CheckCircle2 :size="16" />
            <span>Verification email sent successfully!</span>
          </div>

          <!-- Error Message -->
          <div v-if="resendError" class="flex items-center gap-2 text-red-500 text-sm">
            <AlertCircle :size="16" />
            <span>{{ resendError }}</span>
          </div>

          <div class="flex gap-3">
            <Button
              type="button"
              @click="closeResendModal"
              :disabled="resendLoading"
              className="flex-1"
              variant="secondary"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              :loading="resendLoading"
              :disabled="!resendEmail"
              className="flex-1"
            >
              Send
            </Button>
          </div>
        </form>
      </div>
    </Card>
  </div>
</template>

