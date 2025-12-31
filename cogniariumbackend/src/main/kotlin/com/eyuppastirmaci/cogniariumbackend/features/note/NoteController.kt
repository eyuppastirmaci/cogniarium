package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.features.auth.SecurityUtils
import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteRequest
import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteResponse
import com.eyuppastirmaci.cogniariumbackend.features.note.mapper.NoteMapper
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService,
    private val noteMapper: NoteMapper
) {

    /**
     * Creates a new note.
     * Requires authentication and email verification.
     */
    @PostMapping
    fun createNote(@Valid @RequestBody request: NoteRequest): Mono<NoteResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        return noteService.createNote(request.text, userId)
            .map { noteMapper.toResponse(it) }
    }

    /**
     * Updates an existing note.
     * Requires authentication, email verification, and note ownership.
     */
    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: NoteRequest
    ): Mono<NoteResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        return noteService.updateNote(id, request.text, userId)
            .map { noteMapper.toResponse(it) }
    }

    /**
     * Deletes a note.
     * Requires authentication and note ownership.
     */
    @DeleteMapping("/{id}")
    fun deleteNote(@PathVariable id: Long): Mono<ResponseEntity<Void>> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        return noteService.deleteNote(id, userId)
            .then(Mono.just(ResponseEntity.noContent().build()))
    }

    /**
     * Gets all notes for the current user.
     * Requires authentication.
     */
    @GetMapping
    fun getNotes(): List<NoteResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val notes = noteService.getAllNotes(userId)
        return noteMapper.toResponseList(notes)
    }

    /**
     * Searches notes semantically or returns all notes if query is empty.
     * Requires authentication.
     */
    @GetMapping("/search")
    fun searchNotes(@RequestParam(required = false) q: String?): List<NoteResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        // If query is empty or null, return normal list
        if (q.isNullOrBlank()) {
            val notes = noteService.getAllNotes(userId)
            return noteMapper.toResponseList(notes)
        }

        // Perform semantic search
        val notes = noteService.searchNotesSemantically(q, userId, limit = 20)
        return noteMapper.toResponseList(notes)
    }
}