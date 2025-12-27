package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.features.note.dto.CreateNoteRequest
import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteResponse
import com.eyuppastirmaci.cogniariumbackend.features.note.mapper.NoteMapper
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService,
    private val noteMapper: NoteMapper
) {

    @PostMapping
    fun createNote(@Valid @RequestBody request: CreateNoteRequest): Mono<NoteResponse> {
        return noteService.createSmartNote(request.text)
            .map { noteMapper.toResponse(it) }
    }

    @GetMapping
    fun getNotes(): List<NoteResponse> {
        val notes = noteService.getAllNotes()
        return noteMapper.toResponseList(notes)
    }
}