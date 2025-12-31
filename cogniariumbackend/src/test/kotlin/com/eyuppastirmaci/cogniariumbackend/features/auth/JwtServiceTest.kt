package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.config.properties.JwtProperties
import com.eyuppastirmaci.cogniariumbackend.features.user.User
import com.eyuppastirmaci.cogniariumbackend.features.user.UserRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        val jwtProperties = JwtProperties(
            secret = "test-secret-key-that-is-at-least-32-characters-long-for-testing",
            accessTokenExpiration = Duration.ofMinutes(15),
            refreshTokenExpiration = Duration.ofDays(7)
        )
        jwtService = JwtService(jwtProperties)

        testUser = User(
            id = 1L,
            email = "test@example.com",
            passwordHash = "hashed",
            emailVerified = true,
            role = UserRole.USER
        )
    }

    @Test
    fun `should generate access token`() {
        val token = jwtService.generateAccessToken(testUser)

        assertNotNull(token)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `should generate refresh token`() {
        val token = jwtService.generateRefreshToken(testUser)

        assertNotNull(token)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `should validate valid access token`() {
        val token = jwtService.generateAccessToken(testUser)
        val claims = jwtService.validateToken(token)

        assertNotNull(claims)
        assertEquals(testUser.id.toString(), claims!!.subject)
        assertEquals(testUser.email, claims["email"])
        assertEquals("access", claims["type"])
    }

    @Test
    fun `should validate valid refresh token`() {
        val token = jwtService.generateRefreshToken(testUser)
        val claims = jwtService.validateToken(token)

        assertNotNull(claims)
        assertEquals(testUser.id.toString(), claims!!.subject)
        assertEquals("refresh", claims["type"])
    }

    @Test
    fun `should reject invalid token`() {
        val invalidToken = "invalid.token.here"
        val claims = jwtService.validateToken(invalidToken)

        assertNull(claims)
    }

    @Test
    fun `should extract user ID from token`() {
        val token = jwtService.generateAccessToken(testUser)
        val userId = jwtService.getUserIdFromToken(token)

        assertEquals(testUser.id, userId)
    }

    @Test
    fun `should extract email from token`() {
        val token = jwtService.generateAccessToken(testUser)
        val email = jwtService.getEmailFromToken(token)

        assertEquals(testUser.email, email)
    }

    @Test
    fun `should identify access token type`() {
        val token = jwtService.generateAccessToken(testUser)

        assertTrue(jwtService.isAccessToken(token))
        assertFalse(jwtService.isRefreshToken(token))
    }

    @Test
    fun `should identify refresh token type`() {
        val token = jwtService.generateRefreshToken(testUser)

        assertTrue(jwtService.isRefreshToken(token))
        assertFalse(jwtService.isAccessToken(token))
    }

    @Test
    fun `should return null for invalid token when extracting user ID`() {
        val invalidToken = "invalid.token.here"
        val userId = jwtService.getUserIdFromToken(invalidToken)

        assertNull(userId)
    }

    @Test
    fun `should return null for invalid token when extracting email`() {
        val invalidToken = "invalid.token.here"
        val email = jwtService.getEmailFromToken(invalidToken)

        assertNull(email)
    }
}

