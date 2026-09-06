package com.example.echo.data.repository

import com.example.echo.data.local.InMemoryDataStore
import com.example.echo.domain.model.Recording
import kotlinx.coroutines.flow.Flow

class RecordingRepository(private val store: InMemoryDataStore) {

    fun getAllRecordings(): Flow<List<Recording>> = store.recordings

    fun getRecordingsForPerson(personId: String): Flow<List<Recording>> =
        store.recordingsForPerson(personId)

    suspend fun getRecordingById(id: String): Recording? =
        store.getRecordingById(id)

    suspend fun insertRecording(recording: Recording) =
        store.insertRecording(recording)

    suspend fun updateRecording(recording: Recording) =
        store.updateRecording(recording)

    suspend fun deleteRecording(id: String) =
        store.deleteRecording(id)
}
