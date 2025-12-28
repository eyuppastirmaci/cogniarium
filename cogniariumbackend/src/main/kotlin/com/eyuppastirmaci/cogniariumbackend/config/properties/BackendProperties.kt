package com.eyuppastirmaci.cogniariumbackend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "backend")
data class BackendProperties(
    val baseUrl: String,
    val callbacks: CallbackPaths,
    val search: SearchConfig = SearchConfig()
) {
    data class CallbackPaths(
        val sentiment: String,
        val title: String,
        val summary: String,
        val embedding: String
    )
    
    data class SearchConfig(
        /**
         * Maximum cosine distance threshold for semantic search.
         * Lower values = stricter matching (only very similar results).
         * Higher values = more lenient matching (more results).
         * 
         * Range: 0.0 (identical) to 1.0 (completely different)
         */
        val maxSimilarityDistance: Double = 0.7
    )
}

