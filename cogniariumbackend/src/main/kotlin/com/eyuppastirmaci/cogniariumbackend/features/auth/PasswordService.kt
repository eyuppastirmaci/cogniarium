package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Service for password hashing and validation.
 * Uses BCrypt for secure password hashing.
 */
@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {
    /**
     * Hashes a plain text password using BCrypt.
     */
    fun hashPassword(password: String): String {
        return passwordEncoder.encode(password) ?: throw IllegalStateException("Password encoding failed")
    }

    /**
     * Verifies if a plain text password matches a hashed password.
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return passwordEncoder.matches(password, hashedPassword)
    }

    /**
     * Validates password strength.
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     * 
     * @return PasswordValidationResult with isValid flag and error message if invalid
     */
    fun validatePasswordStrength(password: String): PasswordValidationResult {
        if (password.length < 8) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "Password must be at least 8 characters long"
            )
        }

        if (!password.any { it.isUpperCase() }) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "Password must contain at least one uppercase letter"
            )
        }

        if (!password.any { it.isLowerCase() }) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "Password must contain at least one lowercase letter"
            )
        }

        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "Password must contain at least one digit"
            )
        }

        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!password.any { it in specialChars }) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)"
            )
        }

        return PasswordValidationResult(isValid = true)
    }
}

/**
 * Result of password validation.
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

