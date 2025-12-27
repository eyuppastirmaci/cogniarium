package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteResponse
import com.eyuppastirmaci.cogniariumbackend.features.note.mapper.NoteMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/callbacks")
class CallbackController(
    private val noteService: NoteService,
    private val noteMapper: NoteMapper
) {

    /**
     * Callback endpoint for sentiment analysis results from AI service
     */
    @PostMapping("/sentiment/{noteId}")
    fun updateSentiment(
        @PathVariable noteId: Long,
        @RequestBody requestBody: Map<String, Any>
    ): ResponseEntity<NoteResponse> {
        val labelStr = requestBody["label"] as? String ?: "NEUTRAL"
        val score = (requestBody["score"] as? Number)?.toDouble() ?: 0.0

        val sentiment = try {
            Sentiment.valueOf(labelStr.uppercase())
        } catch (e: Exception) {
            Sentiment.NEUTRAL
        }

        val updatedNote = noteService.updateSentiment(noteId, sentiment, score)
        val noteResponse = noteMapper.toResponse(updatedNote)
        
        return ResponseEntity.ok(noteResponse)
    }

    /**
     * Callback endpoint for title generation results from AI service
     */
    @PostMapping("/title/{noteId}")
    fun updateTitle(
        @PathVariable noteId: Long,
        @RequestBody requestBody: Map<String, Any>
    ): ResponseEntity<NoteResponse> {
        val title = requestBody["title"] as? String
            ?: throw IllegalStateException("Title is required in request body")

        val updatedNote = noteService.updateTitle(noteId, title)
        val noteResponse = noteMapper.toResponse(updatedNote)
        
        return ResponseEntity.ok(noteResponse)
    }

    /**
     * Callback endpoint for summarization results from AI service
     */
    @PostMapping("/summary/{noteId}")
    fun updateSummary(
        @PathVariable noteId: Long,
        @RequestBody requestBody: Map<String, Any>
    ): ResponseEntity<NoteResponse> {
        val summary = requestBody["summary"] as? String
            ?: throw IllegalStateException("Summary is required in request body")

        val updatedNote = noteService.updateSummary(noteId, summary)
        val noteResponse = noteMapper.toResponse(updatedNote)
        
        return ResponseEntity.ok(noteResponse)
    }
}

