package com.eyuppastirmaci.cogniariumbackend.features.note

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    
    /**
     * Finds all notes for a specific user.
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Note>
    
    /**
     * Finds a note by ID and user ID (for security).
     */
    fun findByIdAndUserId(id: Long, userId: Long): Note?

    /**
     * Semantic search using pgvector cosine similarity.
     * Searches for notes with embeddings similar to the provided query embedding.
     * Only returns notes that have embeddings (not null) and similarity above the threshold.
     * Filters by user_id to ensure users only see their own notes.
     * 
     * @param queryEmbedding The embedding vector of the search query (384 dimensions)
     * @param userId The user ID to filter notes by
     * @param maxDistance Maximum cosine distance threshold (0.0 = identical, 1.0 = completely different)
     *                    Lower distance = higher similarity. Recommended: 0.4-0.5 for good results
     * @param limit Maximum number of results to return
     * @return List of notes ordered by similarity (most similar first)
     */
    @Query(
        value = """
            SELECT * FROM notes 
            WHERE embedding IS NOT NULL 
            AND user_id = :userId
            AND (embedding <=> CAST(:queryEmbedding AS vector)) <= :maxDistance
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector) 
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun searchByEmbedding(
        @Param("queryEmbedding") queryEmbedding: String,
        @Param("userId") userId: Long,
        @Param("maxDistance") maxDistance: Double,
        @Param("limit") limit: Int
    ): List<Note>

    /**
     * Updates the embedding of a note using native SQL with proper CAST.
     * This is necessary because Hibernate's AttributeConverter doesn't handle
     * PostgreSQL vector type casting correctly.
     * 
     * @param noteId The ID of the note to update
     * @param embeddingString The embedding in PostgreSQL vector string format: "[0.1, 0.2, 0.3]"
     */
    @Modifying
    @Query(
        value = """
            UPDATE notes 
            SET embedding = CAST(:embeddingString AS vector) 
            WHERE id = :noteId
        """,
        nativeQuery = true
    )
    fun updateEmbeddingNative(
        @Param("noteId") noteId: Long,
        @Param("embeddingString") embeddingString: String
    )

    /**
     * Clears the embedding of a note by setting it to NULL.
     * Used when note content is updated and embedding needs to be regenerated.
     * 
     * @param noteId The ID of the note to clear embedding for
     */
    @Modifying(clearAutomatically = true)
    @Query(
        value = """
            UPDATE notes 
            SET embedding = NULL 
            WHERE id = :noteId
        """,
        nativeQuery = true
    )
    fun clearEmbedding(@Param("noteId") noteId: Long)
}