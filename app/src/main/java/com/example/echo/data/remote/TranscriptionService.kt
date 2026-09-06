package com.example.echo.data.remote

import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import kotlinx.coroutines.delay

/**
 * Interface for the AI transcription backend.
 * Replace MockTranscriptionService with a real implementation
 * that calls your backend endpoint.
 */
interface TranscriptionService {
    suspend fun transcribe(recording: Recording): TranscriptionResult
    suspend fun generateSummary(recording: Recording, transcript: String): SummaryResult
}

data class TranscriptionResult(
    val transcript: String,
    val success: Boolean,
    val error: String? = null
)

data class SummaryResult(
    val summary: String,
    val insights: List<String>,
    val success: Boolean,
    val error: String? = null
)

/**
 * Mock implementation — simulates AI processing with delays.
 * Used when no real backend is configured.
 */
class MockTranscriptionService : TranscriptionService {

    override suspend fun transcribe(recording: Recording): TranscriptionResult {
        delay(2000L) // Simulate network/processing time
        return TranscriptionResult(
            transcript = generateMockTranscript(recording.title),
            success = true
        )
    }

    override suspend fun generateSummary(recording: Recording, transcript: String): SummaryResult {
        delay(1500L) // Simulate LLM processing
        return SummaryResult(
            summary = generateMockSummary(recording.title),
            insights = generateMockInsights(recording.title),
            success = true
        )
    }

    private fun generateMockTranscript(title: String): String {
        return """
Speaker 1: Welcome everyone to ${title}. Let's get started with today's agenda.

Speaker 2: Thank you. I'll begin with the key performance metrics from this period.

Speaker 1: Our team has been working hard and the results speak for themselves.

Speaker 2: Revenue targets have been exceeded, and customer satisfaction scores are at an all-time high.

Speaker 1: We also want to highlight the improvements in operational efficiency.

Speaker 2: Looking ahead, we have ambitious plans for the next quarter. The roadmap includes several exciting initiatives.

Speaker 1: We'll make sure to keep everyone aligned as we move forward.

Speaker 2: Thank you all for your continued dedication and effort.
        """.trim()
    }

    private fun generateMockSummary(title: String): String {
        return "The ${title} session covered key performance highlights and strategic initiatives. Participants discussed revenue performance, operational metrics, and forward-looking plans for the upcoming period."
    }

    private fun generateMockInsights(title: String): List<String> {
        return listOf(
            "Performance targets have been exceeded this period.",
            "Customer satisfaction scores reached an all-time high.",
            "Operational efficiency improvements noted across teams.",
            "Strong roadmap planned for the next quarter."
        )
    }
}
