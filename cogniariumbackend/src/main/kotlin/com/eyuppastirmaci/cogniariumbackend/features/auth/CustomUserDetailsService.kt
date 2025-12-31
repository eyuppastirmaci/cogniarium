package com.eyuppastirmaci.cogniariumbackend.features.auth

import com.eyuppastirmaci.cogniariumbackend.features.user.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user by email (username) for authentication.
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        // In our system, username is email
        val user = userRepository.findByEmail(username)
            .orElseThrow {
                UsernameNotFoundException("User not found with email: $username")
            }

        return CustomUserDetails(user)
    }
}

