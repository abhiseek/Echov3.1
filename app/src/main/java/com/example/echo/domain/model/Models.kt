package com.example.echo.domain.model

import java.util.UUID

enum class RecordingStatus {
    PENDING, PROCESSING, TRANSCRIBING, SUMMARIZING, COMPLETED, ERROR
}

data class Recording(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val fileUri: String,
    val fileName: String,
    val fileSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val status: RecordingStatus = RecordingStatus.PENDING,
    val transcript: String? = null,
    val summary: String? = null,
    val insights: List<String> = emptyList(),
    val personId: String? = null,
    val errorMessage: String? = null
)

data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarUri: String? = null,
    val lastInteraction: String? = null,
    val lastInteractionDate: Long? = null
)

data class Interaction(
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val recordingId: String,
    val type: String, // "meeting", "call", "review", "shared_file"
    val date: Long = System.currentTimeMillis(),
    val summary: String? = null
)
