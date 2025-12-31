import { describe, it, expect, beforeEach, vi } from 'vitest';
import { authService } from '../authService';
import apiClient from '@/api/axiosClient';
import { TOKEN_STORAGE_KEYS } from '@/features/auth/types/Auth';
import type { AuthResponse, RegisterResponse, User } from '@/features/auth/types/Auth';

// Mock axios client
vi.mock('@/api/axiosClient', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn()
  }
}));

describe('authService', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('register', () => {
    it('should register a new user successfully', async () => {
      const mockResponse: RegisterResponse = {
        message: 'Registration successful. Please check your email for verification.',
        email: 'test@example.com'
      };

      (apiClient.post as any).mockResolvedValue({ data: mockResponse });

      const result = await authService.register('test@example.com', 'Password123!');

      expect(result).toEqual(mockResponse);
      expect(apiClient.post).toHaveBeenCalledWith('/auth/register', {
        email: 'test@example.com',
        password: 'Password123!'
      });
    });

    it('should handle registration errors', async () => {
      const error = new Error('Email already exists');
      (apiClient.post as any).mockRejectedValue(error);

      await expect(authService.register('test@example.com', 'Password123!')).rejects.toThrow();
    });
  });

  describe('login', () => {
    it('should login and store tokens', async () => {
      const mockUser: User = {
        id: 1,
        email: 'test@example.com',
        emailVerified: true,
        role: 'USER'
      };

      const mockResponse: AuthResponse = {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        user: mockUser
      };

      (apiClient.post as any).mockResolvedValue({ data: mockResponse });

      const result = await authService.login('test@example.com', 'Password123!');

      expect(result).toEqual(mockResponse);
      expect(localStorage.getItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN)).toBe('access-token');
      expect(localStorage.getItem(TOKEN_STORAGE_KEYS.REFRESH_TOKEN)).toBe('refresh-token');
      expect(localStorage.getItem(TOKEN_STORAGE_KEYS.USER)).toBe(JSON.stringify(mockUser));
    });
  });

  describe('logout', () => {
    it('should clear tokens and user data', async () => {
      localStorage.setItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN, 'token');
      localStorage.setItem(TOKEN_STORAGE_KEYS.REFRESH_TOKEN, 'refresh');
      localStorage.setItem(TOKEN_STORAGE_KEYS.USER, '{}');

      (apiClient.post as any).mockResolvedValue({});

      await authService.logout();

      expect(localStorage.getItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN)).toBeNull();
      expect(localStorage.getItem(TOKEN_STORAGE_KEYS.REFRESH_TOKEN)).toBeNull();
      expect(localStorage.getItem(TOKEN_STORAGE_KEYS.USER)).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when access token exists', () => {
      localStorage.setItem(TOKEN_STORAGE_KEYS.ACCESS_TOKEN, 'token');
      expect(authService.isAuthenticated()).toBe(true);
    });

    it('should return false when access token does not exist', () => {
      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('getUser', () => {
    it('should return user from localStorage', () => {
      const mockUser: User = {
        id: 1,
        email: 'test@example.com',
        emailVerified: true,
        role: 'USER'
      };
      localStorage.setItem(TOKEN_STORAGE_KEYS.USER, JSON.stringify(mockUser));

      const user = authService.getUser();
      expect(user).toEqual(mockUser);
    });

    it('should return null when user does not exist', () => {
      expect(authService.getUser()).toBeNull();
    });
  });
});

