package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class EmailRateLimitService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(EmailRateLimitService::class.java)

    companion object {
        private const val MAX_EMAILS_PER_24_HOURS = 5
        private const val RATE_LIMIT_WINDOW_HOURS = 24L
        private const val RATE_LIMIT_KEY_PREFIX = "email_rate_limit:"
    }

    /**
     * Checks if the user can send a verification email.
     * Returns true if allowed, false if rate limit exceeded.
     */
    fun canSendEmail(email: String): Boolean {
        val key = "$RATE_LIMIT_KEY_PREFIX$email"
        val currentCount = redisTemplate.opsForValue().get(key)?.toIntOrNull() ?: 0
        
        return currentCount < MAX_EMAILS_PER_24_HOURS
    }

    /**
     * Increments the email count for the user and sets TTL to 24 hours.
     * Should be called after successfully sending an email.
     */
    fun incrementEmailCount(email: String) {
        val key = "$RATE_LIMIT_KEY_PREFIX$email"
        val currentCount = redisTemplate.opsForValue().get(key)?.toIntOrNull() ?: 0
        val newCount = currentCount + 1
        
        redisTemplate.opsForValue().set(key, newCount.toString(), RATE_LIMIT_WINDOW_HOURS, TimeUnit.HOURS)
        logger.debug("Email rate limit for $email: $newCount/$MAX_EMAILS_PER_24_HOURS (TTL: ${RATE_LIMIT_WINDOW_HOURS}h)")
    }

    /**
     * Gets the remaining email count for the user.
     */
    fun getRemainingEmailCount(email: String): Int {
        val key = "$RATE_LIMIT_KEY_PREFIX$email"
        val currentCount = redisTemplate.opsForValue().get(key)?.toIntOrNull() ?: 0
        return (MAX_EMAILS_PER_24_HOURS - currentCount).coerceAtLeast(0)
    }
}

