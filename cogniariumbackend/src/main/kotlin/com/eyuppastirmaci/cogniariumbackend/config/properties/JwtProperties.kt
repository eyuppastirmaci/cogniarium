package com.eyuppastirmaci.cogniariumbackend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Duration = Duration.ofMinutes(15),
    val refreshTokenExpiration: Duration = Duration.ofDays(7)
)

