package com.eyuppastirmaci.cogniariumbackend.features.note

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

    @PostMapping
    fun createNote(@Valid @RequestBody request: NoteRequest): Mono<NoteResponse> {
        return noteService.createNote(request.text)
            .map { noteMapper.toResponse(it) }
    }

    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: NoteRequest
    ): Mono<NoteResponse> {
        return noteService.updateNote(id, request.text)
            .map { noteMapper.toResponse(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteNote(@PathVariable id: Long): Mono<ResponseEntity<Void>> {
        return noteService.deleteNote(id)
            .then(Mono.just(ResponseEntity.noContent().build()))
    }

    @GetMapping
    fun getNotes(): List<NoteResponse> {
        val notes = noteService.getAllNotes()
        return noteMapper.toResponseList(notes)
    }

    @GetMapping("/search")
    fun searchNotes(@RequestParam(required = false) q: String?): List<NoteResponse> {
        // If query is empty or null, return normal list
        if (q.isNullOrBlank()) {
            val notes = noteService.getAllNotes()
            return noteMapper.toResponseList(notes)
        }

        // Perform semantic search
        val notes = noteService.searchNotesSemantically(q, limit = 20)
        return noteMapper.toResponseList(notes)
    }
}