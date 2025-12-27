package com.eyuppastirmaci.cogniariumbackend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize

@ConfigurationProperties(prefix = "webclient")
data class WebClientProperties(
    val maxInMemorySize: DataSize
)