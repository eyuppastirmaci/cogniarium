package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.config.properties.BackendProperties
import com.eyuppastirmaci.cogniariumbackend.features.note.mapper.NoteMapper
import com.eyuppastirmaci.cogniariumbackend.features.note.websocket.NoteWebSocketService
import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.HuggingFaceClient
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class NoteService(
    private val aiClient: HuggingFaceClient,
    private val noteRepository: NoteRepository,
    private val noteMapper: NoteMapper,
    private val webSocketService: NoteWebSocketService,
    private val backendProperties: BackendProperties
) {

    /**
     * Creates a note immediately without waiting for AI analysis.
     * Sends async requests to AI service for sentiment and title generation.
     */
    fun createNote(content: String): Mono<Note> {
        return Mono.fromCallable<Note> {
            // Create and save note immediately (blocking operation)
            // JPA repository.save() always returns non-null Note
            val note = Note.createInitial(content)
            noteRepository.save(note)
        }
        .subscribeOn(Schedulers.boundedElastic())
        .doOnNext { savedNote ->
            // Broadcast note creation via WebSocket
            val noteResponse = noteMapper.toResponse(savedNote)
            webSocketService.broadcastNoteUpdate(NoteUpdateType.NOTE_CREATED, noteResponse)

            // Fire async AI requests with callback URLs
            val sentimentCallbackUrl = "${backendProperties.baseUrl}${backendProperties.callbacks.sentiment}/${savedNote.id}"
            val titleCallbackUrl = "${backendProperties.baseUrl}${backendProperties.callbacks.title}/${savedNote.id}"

            aiClient.analyzeSentiment(content, sentimentCallbackUrl)
            aiClient.generateTitleAsync(content, titleCallbackUrl)
        }
    }

    /**
     * Updates the sentiment of a note and broadcasts the change via WebSocket
     */
    fun updateSentiment(noteId: Long, label: Sentiment, score: Double): Note {
        val note = noteRepository.findById(noteId)
            .orElseThrow { IllegalArgumentException("Note with id $noteId not found") }

        val updatedNote = note.copy(
            sentimentLabel = label,
            sentimentScore = score
        )
        val savedNote = noteRepository.save(updatedNote)

        // Broadcast update via WebSocket
        val noteResponse = noteMapper.toResponse(savedNote)
        webSocketService.broadcastNoteUpdate(NoteUpdateType.SENTIMENT_UPDATE, noteResponse)

        return savedNote
    }

    /**
     * Updates the title of a note and broadcasts the change via WebSocket
     */
    fun updateTitle(noteId: Long, title: String): Note {
        val note = noteRepository.findById(noteId)
            .orElseThrow { IllegalArgumentException("Note with id $noteId not found") }

        val updatedNote = note.copy(title = title)
        val savedNote = noteRepository.save(updatedNote)

        // Broadcast update via WebSocket
        val noteResponse = noteMapper.toResponse(savedNote)
        webSocketService.broadcastNoteUpdate(NoteUpdateType.TITLE_UPDATE, noteResponse)

        return savedNote
    }

    fun getAllNotes(): List<Note> {
        // Sort from newest to oldest
        return noteRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
    }
}