package com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface

import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.config.HuggingFaceProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class HuggingFaceClient(
    @Qualifier("huggingFaceWebClient") private val client: WebClient,
    private val properties: HuggingFaceProperties
) {

    /**
     * Sends async sentiment analysis request with callback URL
     */
    fun analyzeSentiment(input: String, callbackUrl: String) {
        val body = mapOf(
            "text" to input,
            "callback_url" to callbackUrl
        )

        client.post()
            .uri("/analyze")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .subscribe(
                { result -> println("Sentiment analysis completed: $result") },
                { error -> println("Sentiment analysis error: ${error.message}") }
            )
    }

    /**
     * Sends async title generation request with callback URL
     */
    fun generateTitleAsync(input: String, callbackUrl: String) {
        val body = mapOf(
            "text" to input,
            "callback_url" to callbackUrl
        )

        client.post()
            .uri("/generate-title")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .subscribe(
                { result -> println("Title generation completed: $result") },
                { error -> println("Title generation error: ${error.message}") }
            )
    }

    /**
     * Sends async summarization request with callback URL
     */
    fun summarizeAsync(input: String, callbackUrl: String) {
        val body = mapOf(
            "text" to input,
            "callback_url" to callbackUrl
        )

        client.post()
            .uri("/summarize")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .subscribe(
                { result -> println("Summarization completed: $result") },
                { error -> println("Summarization error: ${error.message}") }
            )
    }
}