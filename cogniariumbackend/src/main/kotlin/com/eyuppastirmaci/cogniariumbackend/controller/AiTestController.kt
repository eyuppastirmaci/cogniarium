package com.eyuppastirmaci.cogniariumbackend.controller

import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.HuggingFaceClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/test/ai")
class AiTestController(
    private val huggingFaceClient: HuggingFaceClient
) {

    @GetMapping("/sentiment")
    fun testSentiment(@RequestParam text: String): Mono<String> {
        return huggingFaceClient.analyze(text)
            .map { response ->
                "Local AI Response for '$text': $response"
            }
    }

}