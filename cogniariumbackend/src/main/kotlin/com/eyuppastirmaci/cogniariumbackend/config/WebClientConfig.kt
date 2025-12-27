package com.eyuppastirmaci.cogniariumbackend.config

import com.eyuppastirmaci.cogniariumbackend.config.properties.WebClientProperties
import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.config.HuggingFaceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    private val webClientProperties: WebClientProperties,
    private val huggingFaceProperties: HuggingFaceProperties
) {

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        val size = webClientProperties.maxInMemorySize.toBytes().toInt()

        val strategies = ExchangeStrategies.builder()
            .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(size) }
            .build()

        return WebClient.builder()
            .exchangeStrategies(strategies)
    }

    @Bean
    fun huggingFaceWebClient(builder: WebClient.Builder): WebClient {
        // Extract base URL (remove /analyze if present)
        val baseUrl = huggingFaceProperties.url.replace("/analyze", "").replace("/generate-title", "")
        return builder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}