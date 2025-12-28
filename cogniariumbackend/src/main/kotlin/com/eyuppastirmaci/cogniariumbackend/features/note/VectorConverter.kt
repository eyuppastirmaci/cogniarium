package com.eyuppastirmaci.cogniariumbackend.features.note

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Converts between List<Float> (Kotlin/Java) and PostgreSQL vector type (pgvector).
 * 
 * PostgreSQL vector format: "[0.1, 0.2, 0.3, ...]"
 * This converter handles the string representation of vectors.
 */
@Converter
class VectorConverter : AttributeConverter<List<Float>?, String?> {

    override fun convertToDatabaseColumn(attribute: List<Float>?): String? {
        if (attribute == null) return null
        
        // Convert List<Float> to PostgreSQL vector string format: "[0.1, 0.2, 0.3]"
        val vectorString = attribute.joinToString(",", "[", "]")
        return vectorString
    }

    override fun convertToEntityAttribute(dbData: String?): List<Float>? {
        if (dbData == null || dbData.isBlank()) return null
        
        try {
            // Parse PostgreSQL vector string format: "[0.1, 0.2, 0.3]"
            val cleaned = dbData.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .trim()
            
            if (cleaned.isEmpty()) return null
            
            return cleaned.split(",")
                .map { it.trim().toFloat() }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse vector string: $dbData", e)
        }
    }
}

