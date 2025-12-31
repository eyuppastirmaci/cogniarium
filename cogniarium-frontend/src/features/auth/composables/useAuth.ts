import { ref, computed, onMounted } from 'vue';
import { authService } from '@/features/auth/api/authService';
import type { User, AuthResponse, RegisterResponse } from '@/features/auth/types/Auth';

/**
 * Composable for authentication state management.
 * Provides reactive user state and authentication methods.
 */
export function useAuth() {
  const user = ref<User | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Computed properties
  const isAuthenticated = computed(() => !!user.value && authService.isAuthenticated());
  const isEmailVerified = computed(() => user.value?.emailVerified ?? false);
  const showEmailWarning = computed(() => !isEmailVerified.value && isAuthenticated.value);

  /**
   * Initialize auth state from localStorage.
   */
  const initializeAuth = () => {
    const storedUser = authService.getUser();
    if (storedUser && authService.isAuthenticated()) {
      user.value = storedUser;
    }
  };

  /**
   * Register a new user.
   */
  const register = async (email: string, password: string): Promise<RegisterResponse> => {
    loading.value = true;
    error.value = null;
    try {
      const response = await authService.register(email, password);
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Registration failed';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Login user.
   */
  const login = async (email: string, password: string): Promise<AuthResponse> => {
    loading.value = true;
    error.value = null;
    try {
      const response = await authService.login(email, password);
      user.value = response.user;
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Login failed';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Logout user.
   */
  const logout = async () => {
    loading.value = true;
    error.value = null;
    try {
      await authService.logout();
      user.value = null;
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Logout failed';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Refresh current user information from server.
   */
  const refreshUser = async () => {
    if (!authService.isAuthenticated()) {
      user.value = null;
      return;
    }

    loading.value = true;
    error.value = null;
    try {
      const currentUser = await authService.getCurrentUser();
      user.value = currentUser;
    } catch (err: any) {
      // If getting user fails, clear auth state
      if (err.response?.status === 401) {
        user.value = null;
        authService.clearTokens();
        authService.clearUser();
      }
      error.value = err.response?.data?.message || err.message || 'Failed to get user';
    } finally {
      loading.value = false;
    }
  };

  /**
   * Verify email with token.
   */
  const verifyEmail = async (token: string) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await authService.verifyEmail(token);
      // If user is logged in, refresh user info to update emailVerified status
      if (isAuthenticated.value) {
        await refreshUser();
      }
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Email verification failed';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Resend verification email.
   */
  const resendVerification = async () => {
    loading.value = true;
    error.value = null;
    try {
      await authService.resendVerification();
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Failed to resend verification email';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Clear error message.
   */
  const clearError = () => {
    error.value = null;
  };

  // Initialize auth state on mount
  onMounted(() => {
    initializeAuth();
  });

  return {
    // State
    user,
    loading,
    error,
    
    // Computed
    isAuthenticated,
    isEmailVerified,
    showEmailWarning,
    
    // Methods
    register,
    login,
    logout,
    refreshUser,
    verifyEmail,
    resendVerification,
    clearError,
    initializeAuth
  };
}

