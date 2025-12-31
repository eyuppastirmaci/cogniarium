package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.features.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Custom UserDetails implementation for Spring Security.
 * Wraps the User entity and provides UserDetails interface.
 */
class CustomUserDetails(
    private val user: User
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    override fun getPassword(): String {
        return user.passwordHash
    }

    override fun getUsername(): String {
        return user.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        // Account is enabled if email is verified
        return user.emailVerified
    }

    /**
     * Gets the underlying User entity.
     */
    fun getUser(): User {
        return user
    }

    /**
     * Gets the user ID.
     */
    fun getUserId(): Long? {
        return user.id
    }
}

