package com.eyuppastirmaci.cogniariumbackend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "backend")
data class BackendProperties(
    val baseUrl: String,
    val callbacks: CallbackPaths
) {
    data class CallbackPaths(
        val sentiment: String,
        val title: String,
        val summary: String
    )
}

