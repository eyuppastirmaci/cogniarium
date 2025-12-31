package com.eyuppastirmaci.cogniariumbackend.features.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Request DTO for user registration.
 */
data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * Request DTO for user login.
 */
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * Request DTO for token refresh.
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

/**
 * Response DTO for authentication (login/register).
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

/**
 * Response DTO for user information.
 */
data class UserResponse(
    val id: Long,
    val email: String,
    val emailVerified: Boolean,
    val role: String
)

/**
 * Response DTO for registration (before email verification).
 */
data class RegisterResponse(
    val message: String,
    val email: String
)

/**
 * Response DTO for token refresh.
 */
data class RefreshTokenResponse(
    val accessToken: String
)

/**
 * Request DTO for resending verification email by email address.
 */
data class ResendVerificationEmailRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String
)