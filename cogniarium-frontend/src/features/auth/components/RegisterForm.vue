<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '@/features/auth/composables/useAuth';
import Button from '@/components/shared/Button.vue';
import TextInput from '@/components/shared/TextInput.vue';
import Card from '@/components/shared/Card.vue';
import { Mail, Lock, AlertCircle, CheckCircle2 } from 'lucide-vue-next';

const emit = defineEmits<{
  success: [message: string];
  switchToLogin: [];
}>();

const { register, loading, error, clearError } = useAuth();

const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const registrationSuccess = ref(false);
const successMessage = ref('');

const validatePassword = (): string | null => {
  if (password.value.length < 8) {
    return 'Password must be at least 8 characters long';
  }
  if (!/[A-Z]/.test(password.value)) {
    return 'Password must contain at least one uppercase letter';
  }
  if (!/[a-z]/.test(password.value)) {
    return 'Password must contain at least one lowercase letter';
  }
  if (!/[0-9]/.test(password.value)) {
    return 'Password must contain at least one digit';
  }
  if (!/[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/.test(password.value)) {
    return 'Password must contain at least one special character';
  }
  if (password.value !== confirmPassword.value) {
    return 'Passwords do not match';
  }
  return null;
};

const handleSubmit = async (e: Event) => {
  e.preventDefault();
  clearError();
  registrationSuccess.value = false;

  const passwordError = validatePassword();
  if (passwordError) {
    error.value = passwordError;
    return;
  }

  try {
    const response = await register(email.value, password.value);
    registrationSuccess.value = true;
    successMessage.value = response.message;
    emit('success', response.message);
  } catch (err) {
    // Error is handled by useAuth composable
    console.error('Registration failed:', err);
  }
};
</script>

<template>
  <Card class="w-full max-w-md mx-auto">
    <div class="p-8">
      <h2 class="text-2xl font-bold text-text-primary mb-6">Create Account</h2>
      
      <!-- Success Message -->
      <div v-if="registrationSuccess" class="mb-6 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
        <div class="flex items-start gap-3">
          <CheckCircle2 class="text-green-600 dark:text-green-400 mt-0.5" :size="20" />
          <div>
            <p class="text-sm font-medium text-green-800 dark:text-green-200">
              {{ successMessage }}
            </p>
            <p class="text-xs text-green-700 dark:text-green-300 mt-1">
              Please check your email to verify your account.
            </p>
          </div>
        </div>
      </div>

      <form v-else @submit="handleSubmit" class="space-y-4">
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
              placeholder="Create a password"
              :disabled="loading"
              className="pl-10"
              required
            />
          </div>
          <p class="text-xs text-text-secondary mt-1">
            Must be at least 8 characters with uppercase, lowercase, number, and special character
          </p>
        </div>

        <!-- Confirm Password Input -->
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-2">
            Confirm Password
          </label>
          <div class="relative">
            <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 text-text-secondary" :size="18" />
            <TextInput
              v-model="confirmPassword"
              type="password"
              placeholder="Confirm your password"
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
          :disabled="!email || !password || !confirmPassword"
          className="w-full"
        >
          Create Account
        </Button>
      </form>

      <!-- Switch to Login -->
      <div class="mt-6 text-center">
        <p class="text-sm text-text-secondary">
          Already have an account?
          <button
            @click="emit('switchToLogin')"
            class="text-brand-primary hover:text-brand-hover font-medium ml-1"
            :disabled="loading"
          >
            Sign in
          </button>
        </p>
      </div>
    </div>
  </Card>
</template>

