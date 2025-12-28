package com.eyuppastirmaci.cogniariumbackend.exception

/**
 * Exception thrown when embedding generation fails.
 */
class EmbeddingGenerationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

