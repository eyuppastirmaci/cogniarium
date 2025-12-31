import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { useAuth } from '../useAuth';
import { authService } from '@/features/auth/api/authService';
import type { User, AuthResponse } from '@/features/auth/types/Auth';

// Mock authService
vi.mock('@/features/auth/api/authService', () => ({
  authService: {
    register: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
    verifyEmail: vi.fn(),
    resendVerification: vi.fn(),
    getUser: vi.fn(),
    isAuthenticated: vi.fn(),
    clearTokens: vi.fn(),
    clearUser: vi.fn()
  }
}));

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should initialize with null user', () => {
    (authService.getUser as any).mockReturnValue(null);
    (authService.isAuthenticated as any).mockReturnValue(false);

    const TestComponent = {
      setup() {
        const { user, isAuthenticated } = useAuth();
        return { user, isAuthenticated };
      },
      template: '<div></div>'
    };

    const wrapper = mount(TestComponent);
    expect(wrapper.vm.user).toBeNull();
    expect(wrapper.vm.isAuthenticated).toBe(false);
  });

  it('should initialize with user from localStorage', () => {
    const mockUser: User = {
      id: 1,
      email: 'test@example.com',
      emailVerified: true,
      role: 'USER'
    };

    (authService.getUser as any).mockReturnValue(mockUser);
    (authService.isAuthenticated as any).mockReturnValue(true);

    const TestComponent = {
      setup() {
        const { user, isAuthenticated } = useAuth();
        return { user, isAuthenticated };
      },
      template: '<div></div>'
    };

    const wrapper = mount(TestComponent);
    expect(wrapper.vm.user).toEqual(mockUser);
    expect(wrapper.vm.isAuthenticated).toBe(true);
  });

  it('should compute isEmailVerified correctly', () => {
    const mockUser: User = {
      id: 1,
      email: 'test@example.com',
      emailVerified: true,
      role: 'USER'
    };

    (authService.getUser as any).mockReturnValue(mockUser);
    (authService.isAuthenticated as any).mockReturnValue(true);

    const TestComponent = {
      setup() {
        const { isEmailVerified } = useAuth();
        return { isEmailVerified };
      },
      template: '<div></div>'
    };

    const wrapper = mount(TestComponent);
    expect(wrapper.vm.isEmailVerified).toBe(true);
  });

  it('should compute showEmailWarning when email is not verified', () => {
    const mockUser: User = {
      id: 1,
      email: 'test@example.com',
      emailVerified: false,
      role: 'USER'
    };

    (authService.getUser as any).mockReturnValue(mockUser);
    (authService.isAuthenticated as any).mockReturnValue(true);

    const TestComponent = {
      setup() {
        const { showEmailWarning } = useAuth();
        return { showEmailWarning };
      },
      template: '<div></div>'
    };

    const wrapper = mount(TestComponent);
    expect(wrapper.vm.showEmailWarning).toBe(true);
  });

  it('should handle login successfully', async () => {
    const mockUser: User = {
      id: 1,
      email: 'test@example.com',
      emailVerified: true,
      role: 'USER'
    };

    const mockResponse: AuthResponse = {
      accessToken: 'token',
      refreshToken: 'refresh',
      user: mockUser
    };

    (authService.login as any).mockResolvedValue(mockResponse);
    (authService.getUser as any).mockReturnValue(mockUser);
    (authService.isAuthenticated as any).mockReturnValue(true);

    const TestComponent = {
      setup() {
        const { login, user, loading } = useAuth();
        return { login, user, loading };
      },
      template: '<div></div>'
    };

    const wrapper = mount(TestComponent);
    await wrapper.vm.login('test@example.com', 'Password123!');

    expect(authService.login).toHaveBeenCalledWith('test@example.com', 'Password123!');
    expect(wrapper.vm.loading).toBe(false);
  });

  it('should handle login errors', async () => {
    const error = new Error('Invalid credentials');
    (authService.login as any).mockRejectedValue(error);

    const TestComponent = {
      setup() {
        const { login, error } = useAuth();
        return { login, error };
      },
      template: '<div></div>'
    };

    const wrapper = mount(TestComponent);
    try {
      await wrapper.vm.login('test@example.com', 'WrongPassword!');
    } catch (e) {
      // Expected
    }

    expect(wrapper.vm.error).toBeTruthy();
  });
});

