package com.eyuppastirmaci.cogniariumbackend.features.note

import com.eyuppastirmaci.cogniariumbackend.infrastructure.ai.huggingface.HuggingFaceClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class NoteService(
    private val aiClient: HuggingFaceClient,
    private val noteRepository: NoteRepository,
    private val objectMapper: ObjectMapper
) {

    fun createSmartNote(content: String): Mono<Note> {
        return aiClient.analyze(content)
            .map { jsonResponse ->
                Note.fromAiResponse(jsonResponse, content, objectMapper)
            }
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic())
            .map { note ->
                noteRepository.save(note)
            }
    }

    fun getAllNotes(): List<Note> {
        // Sort from newest to oldest
        return noteRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
    }
}