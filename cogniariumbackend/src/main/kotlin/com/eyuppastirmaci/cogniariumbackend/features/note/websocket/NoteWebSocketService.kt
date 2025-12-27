package com.eyuppastirmaci.cogniariumbackend.features.note.websocket

import com.eyuppastirmaci.cogniariumbackend.features.note.NoteUpdateType
import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteResponse
import com.eyuppastirmaci.cogniariumbackend.features.note.dto.NoteUpdateMessage
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class NoteWebSocketService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * Broadcasts a note update to all connected clients via WebSocket
     * @param type The type of update: NOTE_CREATED, SENTIMENT_UPDATE, or TITLE_UPDATE
     * @param note The updated note to broadcast
     */
    fun broadcastNoteUpdate(type: NoteUpdateType, note: NoteResponse) {
        val message = NoteUpdateMessage(type = type, note = note)
        messagingTemplate.convertAndSend("/topic/notes", message)
    }
}

