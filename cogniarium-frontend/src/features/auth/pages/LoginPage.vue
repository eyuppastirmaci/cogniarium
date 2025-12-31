<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/features/auth/composables/useAuth';
import LoginForm from '@/features/auth/components/LoginForm.vue';
import RegisterForm from '@/features/auth/components/RegisterForm.vue';
import { Sparkles } from 'lucide-vue-next';

const router = useRouter();
const { isAuthenticated } = useAuth();
const showRegister = ref(false);

// Redirect if already authenticated
if (isAuthenticated.value) {
  router.push('/notes');
}

const handleLoginSuccess = () => {
  router.push('/notes');
};

const handleRegisterSuccess = () => {
  // Stay on page to show success message
  showRegister.value = false;
};

const handleSwitchToRegister = () => {
  showRegister.value = true;
};

const handleSwitchToLogin = () => {
  showRegister.value = false;
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

      <!-- Auth Forms -->
      <LoginForm
        v-if="!showRegister"
        @success="handleLoginSuccess"
        @switch-to-register="handleSwitchToRegister"
      />
      <RegisterForm
        v-else
        @success="handleRegisterSuccess"
        @switch-to-login="handleSwitchToLogin"
      />
    </div>
  </div>
</template>

