package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class PasswordServiceTest {

    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        passwordEncoder = BCryptPasswordEncoder()
        passwordService = PasswordService(passwordEncoder)
    }

    @Test
    fun `should hash password successfully`() {
        val password = "TestPassword123!"
        val hashed = passwordService.hashPassword(password)

        assertNotNull(hashed)
        assertNotEquals(password, hashed)
        assertTrue(hashed.length > 50) // BCrypt hashes are typically 60 characters
    }

    @Test
    fun `should verify correct password`() {
        val password = "TestPassword123!"
        val hashed = passwordService.hashPassword(password)

        assertTrue(passwordService.verifyPassword(password, hashed))
    }

    @Test
    fun `should reject incorrect password`() {
        val password = "TestPassword123!"
        val wrongPassword = "WrongPassword123!"
        val hashed = passwordService.hashPassword(password)

        assertFalse(passwordService.verifyPassword(wrongPassword, hashed))
    }

    @Test
    fun `should validate strong password`() {
        val password = "StrongPass123!"
        val result = passwordService.validatePasswordStrength(password)

        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `should reject password shorter than 8 characters`() {
        val password = "Short1!"
        val result = passwordService.validatePasswordStrength(password)

        assertFalse(result.isValid)
        assertEquals("Password must be at least 8 characters long", result.errorMessage)
    }

    @Test
    fun `should reject password without uppercase letter`() {
        val password = "lowercase123!"
        val result = passwordService.validatePasswordStrength(password)

        assertFalse(result.isValid)
        assertEquals("Password must contain at least one uppercase letter", result.errorMessage)
    }

    @Test
    fun `should reject password without lowercase letter`() {
        val password = "UPPERCASE123!"
        val result = passwordService.validatePasswordStrength(password)

        assertFalse(result.isValid)
        assertEquals("Password must contain at least one lowercase letter", result.errorMessage)
    }

    @Test
    fun `should reject password without digit`() {
        val password = "NoDigitPass!"
        val result = passwordService.validatePasswordStrength(password)

        assertFalse(result.isValid)
        assertEquals("Password must contain at least one digit", result.errorMessage)
    }

    @Test
    fun `should reject password without special character`() {
        val password = "NoSpecialChar123"
        val result = passwordService.validatePasswordStrength(password)

        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("special character"))
    }
}

