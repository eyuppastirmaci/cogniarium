<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuth } from '@/features/auth/composables/useAuth';
import RegisterForm from '@/features/auth/components/RegisterForm.vue';
import { Sparkles } from 'lucide-vue-next';

const router = useRouter();
const { isAuthenticated } = useAuth();

// Redirect if already authenticated
if (isAuthenticated.value) {
  router.push('/notes');
}

const handleRegisterSuccess = (message: string) => {
  // Show success message, user will verify email
  console.log('Registration successful:', message);
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-bg-main p-4">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="flex items-center justify-center gap-3 mb-8">
        <Sparkles :size="40" class="text-brand-primary" />
        <h1 class="text-3xl font-bold text-text-primary">
          <span class="text-brand-primary">Cogniarium</span>
        </h1>
      </div>

      <!-- Register Form -->
      <RegisterForm @success="handleRegisterSuccess" />
    </div>
  </div>
</template>

