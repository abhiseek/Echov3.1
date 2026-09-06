package com.example.echo.ui.main

import com.example.echo.data.remote.MockTranscriptionService
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun testMockTranscriptionService() = runTest {
        val service = MockTranscriptionService()
        val recording = Recording(
            title = "Test Meeting",
            fileUri = "content://audio/1",
            fileName = "meeting.mp3",
            status = RecordingStatus.PENDING
        )
        val result = service.transcribe(recording)
        assertTrue(result.success)
        assertTrue(result.transcript.contains("Speaker 1"))
        assertTrue(result.transcript.contains("Speaker 2"))

        val summaryResult = service.generateSummary(recording, result.transcript)
        assertTrue(summaryResult.success)
        assertTrue(summaryResult.insights.isNotEmpty())
    }

    @Test
    fun testRecordingModelDefaults() {
        val recording = Recording(
            title = "Project Sync",
            fileUri = "content://audio/2",
            fileName = "sync.mp3"
        )
        assertEquals(RecordingStatus.PENDING, recording.status)
        assertEquals(null, recording.transcript)
    }
}


