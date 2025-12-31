package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Utility class for accessing security context information.
 */
object SecurityUtils {
    /**
     * Gets the current authenticated user ID from SecurityContext.
     * @return User ID if authenticated, null otherwise
     */
    fun getCurrentUserId(): Long? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return authentication?.principal as? Long
    }

    /**
     * Checks if a user is currently authenticated.
     */
    fun isAuthenticated(): Boolean {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }
}

