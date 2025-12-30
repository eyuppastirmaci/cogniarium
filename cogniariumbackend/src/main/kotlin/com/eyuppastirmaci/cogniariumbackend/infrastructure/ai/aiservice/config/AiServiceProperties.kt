package com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.aiservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aiservice.api")
data class AiServiceProperties(
    val url: String,
    val timeout: Long
)

