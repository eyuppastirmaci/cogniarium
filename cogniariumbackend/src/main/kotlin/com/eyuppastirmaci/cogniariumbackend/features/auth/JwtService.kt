package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.config.properties.JwtProperties
import com.eyuppastirmaci.cogniariumbackend.features.user.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import javax.crypto.SecretKey
import java.util.*

@Service
class JwtService(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    /**
     * Generates an access token for the user.
     * Access tokens are short-lived (default: 15 minutes).
     */
    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.accessTokenExpiration.toMillis())

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("email", user.email)
            .claim("role", user.role.name)
            .claim("emailVerified", user.emailVerified)
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Generates a refresh token for the user.
     * Refresh tokens are long-lived (default: 7 days).
     */
    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.refreshTokenExpiration.toMillis())

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("email", user.email)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Validates and extracts claims from a JWT token.
     * @return Claims if token is valid, null otherwise
     */
    fun validateToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts user ID from token claims.
     */
    fun getUserIdFromToken(token: String): Long? {
        val claims = validateToken(token) ?: return null
        return claims.subject.toLongOrNull()
    }

    /**
     * Extracts email from token claims.
     */
    fun getEmailFromToken(token: String): String? {
        val claims = validateToken(token) ?: return null
        return claims["email"] as? String
    }

    /**
     * Checks if token is a refresh token.
     */
    fun isRefreshToken(token: String): Boolean {
        val claims = validateToken(token) ?: return false
        return claims["type"] == "refresh"
    }

    /**
     * Checks if token is an access token.
     */
    fun isAccessToken(token: String): Boolean {
        val claims = validateToken(token) ?: return false
        return claims["type"] == "access"
    }
}

