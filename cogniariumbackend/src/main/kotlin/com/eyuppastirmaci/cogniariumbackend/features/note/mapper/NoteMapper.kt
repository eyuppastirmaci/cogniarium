package com.eyuppastirmaci.cogniariumbackend.features.note.mapper

import com.eyuppastirmaci.cogniariumbackend.features.note.Note
import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteResponse
import org.springframework.stereotype.Component

@Component
class NoteMapper {

    /**
     * Converts a single Entity to DTO.
     */
    fun toResponse(note: Note): NoteResponse {
        return NoteResponse(
            id = note.id,
            content = note.content,
            sentimentLabel = note.sentimentLabel,
            sentimentScore = note.sentimentScore,
            createdAt = note.createdAt
        )
    }

    /**
     * Converts a list of Entities to a list of DTOs.
     */
    fun toResponseList(notes: List<Note>): List<NoteResponse> {
        return notes.map { toResponse(it) }
    }
}