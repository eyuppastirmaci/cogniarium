package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.AuthResponse
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.LoginRequest
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.RefreshTokenRequest
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.RefreshTokenResponse
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.RegisterRequest
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.RegisterResponse
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.ResendVerificationEmailRequest
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.UserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
) {

    /**
     * Register a new user.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        val response = authService.register(request.email, request.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Verify user email with verification token.
     * GET /api/auth/verify-email?token=xxx
     */
    @GetMapping("/verify-email")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<Map<String, String>> {
        // Validate token format (basic check)
        if (token.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to "false",
                    "message" to "Verification token is required"
                )
            )
        }

        // Check if token is valid (exists and not expired)
        if (!emailVerificationService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to "false",
                    "message" to "Invalid or expired verification token. Please request a new verification email."
                )
            )
        }

        val user = emailVerificationService.verifyEmail(token)
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to "false",
                    "message" to "Invalid or expired verification token"
                )
            )
        }

        // Check if already verified (edge case)
        if (user.emailVerified) {
            return ResponseEntity.ok(
                mapOf(
                    "success" to "true",
                    "message" to "Email is already verified"
                )
            )
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to "true",
                "message" to "Email verified successfully"
            )
        )
    }

    /**
     * Login user and return JWT tokens.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request.email, request.password)
        return ResponseEntity.ok(response)
    }

    /**
     * Refresh access token using refresh token.
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    /**
     * Get current authenticated user information.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<UserResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val user = authService.getCurrentUser(userId)
        return ResponseEntity.ok(user)
    }

    /**
     * Resend verification email to the current user.
     * POST /api/auth/resend-verification
     */
    @PostMapping("/resend-verification")
    fun resendVerification(): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        authService.resendVerificationEmail(userId)
        
        return ResponseEntity.ok(
            mapOf(
                "message" to "Verification email sent successfully"
            )
        )
    }

    /**
     * Resend verification email by email address (unauthenticated).
     * POST /api/auth/resend-verification-email
     */
    @PostMapping("/resend-verification-email")
    fun resendVerificationEmail(@Valid @RequestBody request: ResendVerificationEmailRequest): ResponseEntity<Map<String, String>> {
        authService.resendVerificationEmailByEmail(request.email)
        
        return ResponseEntity.ok(
            mapOf(
                "message" to "If the email exists and is not verified, a verification email has been sent"
            )
        )
    }

    /**
     * Logout user by invalidating refresh token.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, String>> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        authService.logout(userId)
        
        return ResponseEntity.ok(
            mapOf(
                "message" to "Logged out successfully"
            )
        )
    }
}

