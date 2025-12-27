package com.eyuppastirmaci.cogniariumbackend.features.note.dto

import com.eyuppastirmaci.cogniariumbackend.features.note.NoteUpdateType
import com.eyuppastirmaci.cogniariumbackend.features.note.Sentiment
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * DTO that receives data from the frontend.
 */
data class CreateNoteRequest(
    @field:NotBlank(message = "Note content cannot be empty")
    @field:Size(min = 3, max = 2000, message = "Note content must be between 3 and 2000 characters")
    val text: String
)

/**
 * DTO that standardizes data sent to the frontend.
 * It is safer to use this instead of directly exposing the domain object (Entity).
 */
data class NoteResponse(
    val id: Long?,
    val content: String,
    val title: String?,
    val summary: String?,
    val sentimentLabel: Sentiment?,
    val sentimentScore: Double?,
    val createdAt: LocalDateTime
)

/**
 * DTO for WebSocket messages
 */
data class NoteUpdateMessage(
    val type: NoteUpdateType,
    val note: NoteResponse
)