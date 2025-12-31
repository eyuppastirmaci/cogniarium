/**
 * User role enum
 */
export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

/**
 * User information
 */
export interface User {
  id: number;
  email: string;
  emailVerified: boolean;
  role: UserRole;
}

/**
 * Request DTO for user registration
 */
export interface RegisterRequest {
  email: string;
  password: string;
}

/**
 * Request DTO for user login
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Request DTO for token refresh
 */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/**
 * Response DTO for authentication (login/register)
 */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

/**
 * Response DTO for registration (before email verification)
 */
export interface RegisterResponse {
  message: string;
  email: string;
}

/**
 * Response DTO for token refresh
 */
export interface RefreshTokenResponse {
  accessToken: string;
}

/**
 * Response DTO for email verification
 */
export interface EmailVerificationResponse {
  success: boolean;
  message: string;
}

/**
 * Token storage keys
 */
export const TOKEN_STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER: 'user'
} as const;

