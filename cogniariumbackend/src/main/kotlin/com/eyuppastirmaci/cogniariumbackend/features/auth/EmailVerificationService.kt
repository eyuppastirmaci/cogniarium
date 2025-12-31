package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.config.properties.BackendProperties
import com.eyuppastirmaci.cogniariumbackend.features.user.User
import com.eyuppastirmaci.cogniariumbackend.features.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val backendProperties: BackendProperties,
    private val emailService: EmailService,
    private val emailRateLimitService: EmailRateLimitService
) {
    companion object {
        private const val TOKEN_EXPIRATION_HOURS = 24L
    }

    /**
     * Generates a unique verification token (UUID) and saves it to the user.
     * Also sends the verification email.
     * Note: Rate limiting should be checked before calling this method.
     * This method will increment the rate limit counter after sending the email.
     */
    @Transactional
    fun generateAndSendVerificationToken(user: User) {
        val token = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS)

        // Update user with verification token
        val updatedUser = user.copy(
            emailVerificationToken = token,
            emailVerificationTokenExpiresAt = expiresAt
        )
        userRepository.save(updatedUser)

        // Send verification email
        val verificationLink = buildVerificationLink(token)
        emailService.sendVerificationEmail(user.email, verificationLink)
        
        // Increment rate limit counter in Redis (with 24h TTL)
        emailRateLimitService.incrementEmailCount(user.email)
    }

    /**
     * Validates the verification token and verifies the user's email.
     * @return User if token is valid, null otherwise
     */
    @Transactional
    fun verifyEmail(token: String): User? {
        val user = userRepository.findByEmailVerificationToken(token)
            .orElse(null) ?: return null

        // Check if token is expired
        val expiresAt = user.emailVerificationTokenExpiresAt
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            return null
        }

        // Check if already verified
        if (user.emailVerified) {
            return user
        }

        // Verify email and clear token
        val verifiedUser = user.copy(
            emailVerified = true,
            emailVerificationToken = null,
            emailVerificationTokenExpiresAt = null
        )
        return userRepository.save(verifiedUser)
    }

    /**
     * Checks if a verification token is valid (exists and not expired).
     */
    fun isTokenValid(token: String): Boolean {
        val user = userRepository.findByEmailVerificationToken(token)
            .orElse(null) ?: return false

        val expiresAt = user.emailVerificationTokenExpiresAt
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now())
    }

    /**
     * Resends verification email by email address.
     * Checks if user exists, email is not verified, and rate limit is not exceeded.
     * Rate limit: Maximum 5 emails per 24 hours (enforced via Redis with TTL).
     */
    @Transactional
    fun resendVerificationEmailByEmail(email: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("User not found with email: $email") }

        // Check if email is already verified
        if (user.emailVerified) {
            throw IllegalStateException("Email is already verified")
        }

        // Check rate limit using Redis
        if (!emailRateLimitService.canSendEmail(email)) {
            val remaining = emailRateLimitService.getRemainingEmailCount(email)
            throw IllegalStateException(
                "Rate limit exceeded. Maximum 5 emails allowed per 24 hours. " +
                "Please wait before requesting another verification email."
            )
        }

        // Generate and send new verification email
        // (rate limit counter is incremented inside generateAndSendVerificationToken)
        generateAndSendVerificationToken(user)
    }

    /**
     * Builds the verification link URL.
     */
    private fun buildVerificationLink(token: String): String {
        val baseUrl = backendProperties.baseUrl
        return "$baseUrl/api/auth/verify-email?token=$token"
    }
}

