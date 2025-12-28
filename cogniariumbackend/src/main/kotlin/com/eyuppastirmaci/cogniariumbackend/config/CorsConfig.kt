package com.eyuppastirmaci.cogniariumbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/api/**") // Valid for all endpoints starting with /api
                    .allowedOrigins(
                        "http://localhost:5173", // Frontend dev server (Vite)
                        "http://localhost:3000"  // Frontend Docker container (nginx)
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed methods
                    .allowedHeaders("*") // Allow all headers
                    .allowCredentials(true) // Allow cookie or Auth header passing
                    .maxAge(3600) // Cache these settings for 1 hour
            }
        }
    }
}