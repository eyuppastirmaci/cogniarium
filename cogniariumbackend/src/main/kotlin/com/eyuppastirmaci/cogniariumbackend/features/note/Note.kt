package com.eyuppastirmaci.cogniariumbackend.features.note

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notes")
data class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(columnDefinition = "TEXT")
    val content: String,

    val title: String? = null,

    @Column(columnDefinition = "TEXT")
    val summary: String? = null,

    @Enumerated(EnumType.STRING)
    val sentimentLabel: Sentiment? = null,

    val sentimentScore: Double? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * Creates a Note from AI service JSON response.
         * Parses the JSON response and extracts sentiment information.
         */
        fun fromAiResponse(
            jsonResponse: String,
            content: String,
            objectMapper: ObjectMapper
        ): Note {
            val resultMap = objectMapper.readValue(jsonResponse, Map::class.java)

            val labelStr = resultMap["label"] as? String ?: "NEUTRAL"

            val sentiment = try {
                Sentiment.valueOf(labelStr.uppercase())
            } catch (e: Exception) {
                Sentiment.NEUTRAL
            }

            val score = (resultMap["score"] as? Number)?.toDouble() ?: 0.0

            return Note(
                content = content,
                sentimentLabel = sentiment,
                sentimentScore = score
            )
        }

        /**
         * Creates an initial Note without AI-generated data.
         * Title, summary, and sentiment will be generated asynchronously.
         */
        fun createInitial(content: String): Note {
            return Note(
                content = content,
                title = null,
                summary = null,
                sentimentLabel = null,
                sentimentScore = null
            )
        }
    }
}