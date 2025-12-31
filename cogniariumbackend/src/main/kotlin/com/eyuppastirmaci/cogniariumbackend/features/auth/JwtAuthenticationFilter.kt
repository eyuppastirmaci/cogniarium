package com.eyuppastirmaci.cogniariumbackend.features.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 * Extracts the token from the Authorization header and sets the authentication in SecurityContext.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7) // Remove "Bearer " prefix
        val claims = jwtService.validateToken(token)

        if (claims != null && jwtService.isAccessToken(token)) {
            val userId = claims.subject.toLongOrNull()
            val email = claims["email"] as? String
            val role = claims["role"] as? String

            if (userId != null && email != null && role != null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
                ).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }

                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}

