package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.AuthResponse
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.RefreshTokenResponse
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.RegisterResponse
import com.eyuppastirmaci.cogniariumbackend.features.auth.dto.UserResponse
import com.eyuppastirmaci.cogniariumbackend.features.user.User
import com.eyuppastirmaci.cogniariumbackend.features.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service for authentication operations (registration, login, etc.).
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val jwtService: JwtService,
    private val emailVerificationService: EmailVerificationService,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService
) {

    /**
     * Registers a new user.
     * - Validates email uniqueness
     * - Validates password strength
     * - Creates user with emailVerified = false
     * - Generates and sends verification email
     */
    @Transactional
    fun register(email: String, password: String): RegisterResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already registered: $email")
        }

        // Validate password strength
        val passwordValidation = passwordService.validatePasswordStrength(password)
        if (!passwordValidation.isValid) {
            throw IllegalArgumentException(passwordValidation.errorMessage ?: "Invalid password")
        }

        // Hash password
        val passwordHash = passwordService.hashPassword(password)

        // Create user
        val user = User(
            email = email,
            passwordHash = passwordHash,
            emailVerified = false
        )
        val savedUser = userRepository.save(user)

        // Generate and send verification email
        emailVerificationService.generateAndSendVerificationToken(savedUser)

        return RegisterResponse(
            message = "Registration successful. Please check your email to verify your account.",
            email = savedUser.email
        )
    }

    /**
     * Authenticates a user and returns JWT tokens.
     * - Validates credentials
     * - Updates last login time
     * - Generates access and refresh tokens
     */
    @Transactional
    fun login(email: String, password: String): AuthResponse {
        // Authenticate using Spring Security
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(email, password)
        )

        // Load user details
        val userDetails = userDetailsService.loadUserByUsername(email) as CustomUserDetails
        val user = userDetails.getUser()

        // Update last login time
        val updatedUser = user.copy(lastLoginAt = LocalDateTime.now())
        val savedUser = userRepository.save(updatedUser)

        // Generate tokens
        val accessToken = jwtService.generateAccessToken(savedUser)
        val refreshToken = jwtService.generateRefreshToken(savedUser)

        // Save refresh token to user (optional, for token revocation)
        val userWithRefreshToken = savedUser.copy(
            refreshToken = refreshToken,
            refreshTokenExpiresAt = LocalDateTime.now().plusDays(7)
        )
        userRepository.save(userWithRefreshToken)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserResponse(
                id = savedUser.id!!,
                email = savedUser.email,
                emailVerified = savedUser.emailVerified,
                role = savedUser.role.name
            )
        )
    }

    /**
     * Refreshes an access token using a refresh token.
     * - Validates refresh token
     * - Checks if token is a refresh token
     * - Loads user from database
     * - Generates new access token
     */
    @Transactional
    fun refreshToken(refreshToken: String): RefreshTokenResponse {
        // Validate token
        val claims = jwtService.validateToken(refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        // Check if it's a refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw IllegalArgumentException("Token is not a refresh token")
        }

        // Get user ID from token
        val userId = jwtService.getUserIdFromToken(refreshToken)
            ?: throw IllegalArgumentException("Invalid token: user ID not found")

        // Load user from database
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Optional: Verify refresh token matches the one stored in database
        if (user.refreshToken != refreshToken) {
            throw IllegalArgumentException("Refresh token mismatch")
        }

        // Check if refresh token is expired (database check)
        val expiresAt = user.refreshTokenExpiresAt
        if (expiresAt == null || expiresAt.isBefore(java.time.LocalDateTime.now())) {
            throw IllegalArgumentException("Refresh token expired")
        }

        // Generate new access token
        val newAccessToken = jwtService.generateAccessToken(user)

        return RefreshTokenResponse(accessToken = newAccessToken)
    }

    /**
     * Resends verification email to the current user.
     * Requires authentication.
     * Includes rate limiting (max 5 emails per 24 hours via Redis).
     */
    @Transactional
    fun resendVerificationEmail(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Check if already verified
        if (user.emailVerified) {
            throw IllegalStateException("Email is already verified")
        }

        // Use the same resend method which includes rate limiting
        emailVerificationService.resendVerificationEmailByEmail(user.email)
    }

    /**
     * Resends verification email by email address.
     * Does not require authentication.
     * Includes rate limiting (max 3 emails per 24 hours).
     */
    @Transactional
    fun resendVerificationEmailByEmail(email: String) {
        emailVerificationService.resendVerificationEmailByEmail(email)
    }

    /**
     * Gets current user information.
     */
    fun getCurrentUser(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return UserResponse(
            id = user.id!!,
            email = user.email,
            emailVerified = user.emailVerified,
            role = user.role.name
        )
    }

    /**
     * Logout user by invalidating refresh token.
     * Since JWT is stateless, we can't invalidate access tokens,
     * but we can invalidate the refresh token stored in database.
     */
    @Transactional
    fun logout(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Clear refresh token
        val updatedUser = user.copy(
            refreshToken = null,
            refreshTokenExpiresAt = null
        )
        userRepository.save(updatedUser)
    }
}

