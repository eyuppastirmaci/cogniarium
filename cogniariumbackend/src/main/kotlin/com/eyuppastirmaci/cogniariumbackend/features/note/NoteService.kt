package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.config.properties.BackendProperties
import com.eyuppastirmaci.cogniariumbackend.features.note.mapper.NoteMapper
import com.eyuppastirmaci.cogniariumbackend.features.note.websocket.NoteWebSocketService
import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.HuggingFaceClient
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
            val summaryCallbackUrl = "${backendProperties.baseUrl}${backendProperties.callbacks.summary}/${savedNote.id}"
            val embeddingCallbackUrl = "${backendProperties.baseUrl}${backendProperties.callbacks.embedding}/${savedNote.id}"

            aiClient.analyzeSentiment(content, sentimentCallbackUrl)
            aiClient.generateTitleAsync(content, titleCallbackUrl)
            aiClient.summarizeAsync(content, summaryCallbackUrl)
            aiClient.generateEmbeddingAsync(content, embeddingCallbackUrl)
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

    /**
     * Updates the summary of a note and broadcasts the change via WebSocket
     */
    fun updateSummary(noteId: Long, summary: String): Note {
        val note = noteRepository.findById(noteId)
            .orElseThrow { IllegalArgumentException("Note with id $noteId not found") }

        val updatedNote = note.copy(summary = summary)
        val savedNote = noteRepository.save(updatedNote)

        // Broadcast update via WebSocket
        val noteResponse = noteMapper.toResponse(savedNote)
        webSocketService.broadcastNoteUpdate(NoteUpdateType.SUMMARY_UPDATE, noteResponse)

        return savedNote
    }

    /**
     * Updates the embedding of a note and broadcasts the change via WebSocket.
     * Uses native SQL to properly cast the embedding string to PostgreSQL vector type.
     */
    @Transactional
    fun updateEmbedding(noteId: Long, embedding: List<Float>): Note {
        // Verify note exists
        val note = noteRepository.findById(noteId)
            .orElseThrow { IllegalArgumentException("Note with id $noteId not found") }

        // Convert embedding to PostgreSQL vector string format
        val embeddingString = embedding.joinToString(",", "[", "]")
        
        // Update using native SQL to properly cast to vector type
        noteRepository.updateEmbeddingNative(noteId, embeddingString)
        
        // Refresh the entity to get the updated embedding
        val updatedNote = noteRepository.findById(noteId)
            .orElseThrow { IllegalArgumentException("Note with id $noteId not found after update") }

        // Broadcast update via WebSocket
        val noteResponse = noteMapper.toResponse(updatedNote)
        webSocketService.broadcastNoteUpdate(NoteUpdateType.EMBEDDING_UPDATE, noteResponse)

        return updatedNote
    }

    fun getAllNotes(): List<Note> {
        // Sort from newest to oldest
        return noteRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
    }

    /**
     * Performs semantic search on notes using the provided query string.
     * Converts the query to an embedding and searches for similar notes.
     * Only returns notes with similarity above the configured threshold.
     * 
     * @param query The search query string
     * @param limit Maximum number of results to return
     * @return List of notes ordered by similarity (most similar first)
     */
    fun searchNotesSemantically(query: String, limit: Int = 10): List<Note> {
        if (query.isBlank()) {
            return emptyList()
        }

        // Generate embedding for the query
        // Note: This is a synchronous call for search - in production you might want to cache embeddings
        val embedding = aiClient.generateEmbeddingSync(query)
        
        // Convert embedding to PostgreSQL vector string format
        val embeddingString = embedding.joinToString(",", "[", "]")
        
        // Search using the embedding with similarity threshold
        val maxDistance = backendProperties.search.maxSimilarityDistance
        
        return noteRepository.searchByEmbedding(embeddingString, maxDistance, limit)
    }
}