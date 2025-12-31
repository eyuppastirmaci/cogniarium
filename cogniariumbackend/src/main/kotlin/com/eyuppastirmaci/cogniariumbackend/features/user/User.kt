package com.eyuppastirmaci.cogniariumbackend.features.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "email_verified", nullable = false)
    val emailVerified: Boolean = false,

    @Column(name = "email_verification_token")
    val emailVerificationToken: String? = null,

    @Column(name = "email_verification_token_expires_at")
    val emailVerificationTokenExpiresAt: LocalDateTime? = null,

    @Column(name = "refresh_token")
    val refreshToken: String? = null,

    @Column(name = "refresh_token_expires_at")
    val refreshTokenExpiresAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER
)

