package com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface

import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.config.HuggingFaceProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class HuggingFaceClient(
    @Qualifier("huggingFaceWebClient") private val client: WebClient,
    private val properties: HuggingFaceProperties
) {

    fun analyze(input: String): Mono<String> {

        // ai-service input expects text
        val body = mapOf("text" to input)

        return client.post()
            .uri("")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .doOnError { e ->
                println("Local AI Service Error: ${e.message}")
            }
    }
}