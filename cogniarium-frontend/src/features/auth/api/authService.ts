import apiClient from '@/api/axiosClient';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  User,
  EmailVerificationResponse
} from '@/features/auth/types/Auth';
import { TOKEN_STORAGE_KEYS } from '@/features/auth/types/Auth';

/**
 * Auth service for handling authentication API calls.
 */
export const authService = {
  /**
   * Register a new user.
   */
  async register(email: string, password: string): Promise<RegisterResponse> {
    const payload: RegisterRequest = { email, password };
    const response = await apiClient.post<RegisterResponse>('/auth/register', payload);
    return response.data;
  },

  /**
   * Login user and get JWT tokens.
   */
  async login(email: string, password: string): Promise<AuthResponse> {
    const payload: LoginRequest = { email, password };
    const response = await apiClient.post<AuthResponse>('/auth/login', payload);
    
    // Store tokens and user in localStorage
    this.storeTokens(response.data.accessToken, response.data.refreshToken);
    this.storeUser(response.data.user);
    
    return response.data;
  },

  /**
   * Logout user and clear tokens.
   */
  async logout(): Promise<void> {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      // Even if logout fails, clear local storage
      console.error('Logout error:', error);
    } finally {
      this.clearTokens();
      this.clearUser();
    }
  },

  /**
   * Refresh access token using refresh token.
   */
  async refreshToken(): Promise<string> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const payload: RefreshTokenRequest = { refreshToken };
    const response = await apiClient.post<RefreshTokenResponse>('/auth/refresh', payload);
    
    // Update access token
    this.storeAccessToken(response.data.accessToken);
    
    return response.data.accessToken;
  },

  /**
   * Get current user information.
   */
  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<User>('/auth/me');
    this.storeUser(response.data);
    return response.data;
  },

  /**
   * Verify email with verification token.
   */
  async verifyEmail(token: string): Promise<EmailVerificationResponse> {
    const response = await apiClient.get<EmailVerificationResponse>('/auth/verify-email', {
      params: { token }
    });
    return response.data;
  },

  /**
   * Resend verification email (requires authentication).
   */
  async resendVerification(): Promise<void> {
    await apiClient.post('/auth/resend-verification');
  },

  /**
   * Resend verification email by email address (unauthenticated).
   */
  async resendVerificationEmail(email: string): Promise<{ message: string }> {
    const response = await apiClient.post<{ message: string }>('/auth/resend-verification-email', { email });
    return response.data;
  },

  /**
   * Store access and refresh tokens in localStorage.
   */
  storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN, accessToken);
    localStorage.setItem(TOKEN_STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
  },

  /**
   * Store access token in localStorage.
   */
  storeAccessToken(accessToken: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN, accessToken);
  },

  /**
   * Get access token from localStorage.
   */
  getAccessToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN);
  },

  /**
   * Get refresh token from localStorage.
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEYS.REFRESH_TOKEN);
  },

  /**
   * Clear tokens from localStorage.
   */
  clearTokens(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(TOKEN_STORAGE_KEYS.REFRESH_TOKEN);
  },

  /**
   * Store user in localStorage.
   */
  storeUser(user: User): void {
    localStorage.setItem(TOKEN_STORAGE_KEYS.USER, JSON.stringify(user));
  },

  /**
   * Get user from localStorage.
   */
  getUser(): User | null {
    const userStr = localStorage.getItem(TOKEN_STORAGE_KEYS.USER);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  },

  /**
   * Clear user from localStorage.
   */
  clearUser(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEYS.USER);
  },

  /**
   * Check if user is authenticated (has access token).
   */
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }
};

