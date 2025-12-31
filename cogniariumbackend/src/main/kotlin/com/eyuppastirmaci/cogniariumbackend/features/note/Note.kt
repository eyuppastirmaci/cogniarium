package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.features.user.User
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

    @Column(name = "embedding", columnDefinition = "vector(384)", nullable = true, insertable = false, updatable = false)
    @Convert(converter = VectorConverter::class)
    val embedding: List<Float>? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

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
            user: User,
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
                sentimentScore = score,
                user = user
            )
        }

        /**
         * Creates an initial Note without AI-generated data.
         * Title, summary, and sentiment will be generated asynchronously.
         */
        fun createInitial(content: String, user: User): Note {
            return Note(
                content = content,
                title = null,
                summary = null,
                sentimentLabel = null,
                sentimentScore = null,
                user = user
            )
        }
    }
}