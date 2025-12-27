package com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "huggingface.api")
data class HuggingFaceProperties(
    val url: String,
    val timeout: Long
)