package com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface

import com.eyuppastirmaci.cogniariumbackend.exception.EmbeddingGenerationException
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

    /**
     * Sends async embedding generation request with callback URL
     */
    fun generateEmbeddingAsync(input: String, callbackUrl: String) {
        val body = mapOf(
            "text" to input,
            "callback_url" to callbackUrl
        )

        client.post()
            .uri("/generate-embedding")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .subscribe(
                { result -> println("Embedding generation completed: $result") },
                { error -> println("Embedding generation error: ${error.message}") }
            )
    }

    /**
     * Synchronously generates embedding for a text query.
     * Used for search operations where we need immediate results.
     * 
     * @throws EmbeddingGenerationException if embedding generation fails
     */
    fun generateEmbeddingSync(input: String): List<Float> {
        val body = mapOf(
            "text" to input
            // No callback_url for sync calls
        )

        val response = client.post()
            .uri("/generate-embedding")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .block()
            ?: throw EmbeddingGenerationException("Failed to generate embedding: No response from AI service")

        @Suppress("UNCHECKED_CAST")
        val embeddingList = response["embedding"] as? List<*>
            ?: throw EmbeddingGenerationException("Failed to generate embedding: Invalid response format")
        
        val embedding = embeddingList.mapNotNull { value ->
            when (value) {
                is Number -> value.toFloat()
                is Double -> value.toFloat()
                is Float -> value
                else -> null
            }
        }
        
        if (embedding.isEmpty()) {
            throw EmbeddingGenerationException("Failed to generate embedding: Empty embedding list")
        }
        
        return embedding
    }
}