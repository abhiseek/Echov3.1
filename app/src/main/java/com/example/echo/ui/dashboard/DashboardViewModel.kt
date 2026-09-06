package com.example.echo.ui.dashboard

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo.data.remote.TranscriptionService
import com.example.echo.data.repository.PersonRepository
import com.example.echo.data.repository.RecordingRepository
import com.example.echo.domain.model.Person
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.media.MediaMetadataRetriever
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val recordings: List<Recording>) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Paused : RecordingState()
    data class Processing(val fileName: String, val status: RecordingStatus) : RecordingState()
}

class DashboardViewModel(
    private val recordingRepository: RecordingRepository,
    private val personRepository: PersonRepository,
    private val transcriptionService: TranscriptionService
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _recordingTimerMs = MutableStateFlow(0L)
    val recordingTimerMs: StateFlow<Long> = _recordingTimerMs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _completedRecordingId = MutableStateFlow<String?>(null)
    val completedRecordingId: StateFlow<String?> = _completedRecordingId.asStateFlow()

    fun clearCompletedRecording() {
        _completedRecordingId.value = null
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var timerJob: Job? = null

    init {
        loadRecordings()
    }

    private fun loadRecordings() {
        viewModelScope.launch {
            recordingRepository.getAllRecordings()
                .catch { e -> _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error") }
                .collect { recordings ->
                    _uiState.value = DashboardUiState.Success(recordings)
                }
        }
    }

    fun processUploadedFile(context: Context, uri: Uri, displayName: String) {
        viewModelScope.launch {
            val title = displayName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
            val cleanTitle = title.ifBlank { "Recording ${formatTimestamp()}" }
            _recordingState.value = RecordingState.Processing(cleanTitle, RecordingStatus.PROCESSING)

            val savedFile = withContext(Dispatchers.IO) {
                try {
                    val safeName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                    val destFile = File(context.filesDir, "audio_${System.currentTimeMillis()}_$safeName")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (destFile.exists() && destFile.length() > 0) destFile else null
                } catch (e: Exception) {
                    null
                }
            }

            if (savedFile == null) {
                _errorMessage.value = "Failed to load audio file: could not read stream."
                _recordingState.value = RecordingState.Idle
                return@launch
            }

            var durationMs = 0L
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(savedFile.absolutePath)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationMs = durStr?.toLongOrNull() ?: 0L
                retriever.release()
            } catch (_: Exception) {}

            val recording = Recording(
                title = cleanTitle,
                fileUri = savedFile.absolutePath,
                fileName = displayName,
                fileSizeBytes = savedFile.length(),
                durationMs = durationMs,
                status = RecordingStatus.PROCESSING
            )
            recordingRepository.insertRecording(recording)
            processRecording(recording)
        }
    }

    fun startRecording(context: Context) {
        viewModelScope.launch {
            try {
                val outputFile = File(
                    context.externalCacheDir ?: context.cacheDir,
                    "echo_rec_${System.currentTimeMillis()}.m4a"
                )
                currentRecordingFile = outputFile

                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(outputFile.absolutePath)
                    prepare()
                    start()
                }

                _recordingState.value = RecordingState.Recording
                _recordingTimerMs.value = 0L
                startTimer()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start recording: ${e.message}"
                cleanupRecorder()
            }
        }
    }

    fun stopRecording(context: Context) {
        viewModelScope.launch {
            try {
                timerJob?.cancel()
                val durationMs = _recordingTimerMs.value
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null

                val file = currentRecordingFile ?: return@launch
                currentRecordingFile = null

                val title = "Recording ${formatTimestamp()}"
                val recording = Recording(
                    title = title,
                    fileUri = file.toURI().toString(),
                    fileName = file.name,
                    fileSizeBytes = file.length(),
                    durationMs = durationMs,
                    status = RecordingStatus.PROCESSING
                )
                recordingRepository.insertRecording(recording)
                _recordingState.value = RecordingState.Processing(title, RecordingStatus.PROCESSING)
                processRecording(recording)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to stop recording: ${e.message}"
                cleanupRecorder()
                _recordingState.value = RecordingState.Idle
            }
        }
    }

    fun cancelRecording() {
        timerJob?.cancel()
        cleanupRecorder()
        _recordingState.value = RecordingState.Idle
        _recordingTimerMs.value = 0L
    }

    private suspend fun processRecording(recording: Recording) {
        try {
            // Transcribing
            val updated = recording.copy(status = RecordingStatus.TRANSCRIBING)
            recordingRepository.updateRecording(updated)
            _recordingState.value = RecordingState.Processing(recording.title, RecordingStatus.TRANSCRIBING)

            val transcriptionResult = transcriptionService.transcribe(recording)
            if (!transcriptionResult.success) {
                val err = transcriptionResult.error ?: "AI Transcription failed"
                recordingRepository.updateRecording(
                    recording.copy(status = RecordingStatus.ERROR, errorMessage = err)
                )
                _recordingState.value = RecordingState.Idle
                _errorMessage.value = err
                return
            }

            // Summarizing
            val transcribedRecording = updated.copy(
                transcript = transcriptionResult.transcript,
                status = RecordingStatus.SUMMARIZING
            )
            recordingRepository.updateRecording(transcribedRecording)
            _recordingState.value = RecordingState.Processing(recording.title, RecordingStatus.SUMMARIZING)

            val summaryResult = transcriptionService.generateSummary(transcribedRecording, transcriptionResult.transcript)
            if (!summaryResult.success) {
                val err = summaryResult.error ?: "Summary generation failed"
                recordingRepository.updateRecording(
                    transcribedRecording.copy(status = RecordingStatus.ERROR, errorMessage = err)
                )
                _recordingState.value = RecordingState.Idle
                _errorMessage.value = err
                return
            }

            // Done
            val person = Person(
                name = recording.title,
                lastInteraction = "${recording.title} (${formatTimestamp()})",
                lastInteractionDate = System.currentTimeMillis()
            )
            personRepository.insertPerson(person)

            recordingRepository.updateRecording(
                transcribedRecording.copy(
                    summary = summaryResult.summary,
                    insights = summaryResult.insights,
                    personId = person.id,
                    status = RecordingStatus.COMPLETED
                )
            )
            _recordingState.value = RecordingState.Idle
            _completedRecordingId.value = recording.id
        } catch (e: Exception) {
            recordingRepository.updateRecording(
                recording.copy(status = RecordingStatus.ERROR, errorMessage = e.message)
            )
            _recordingState.value = RecordingState.Idle
            _errorMessage.value = "Processing failed: ${e.message}"
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _recordingTimerMs.value += 1000
            }
        }
    }

    private fun cleanupRecorder() {
        try {
            mediaRecorder?.apply { stop(); release() }
        } catch (_: Exception) {}
        mediaRecorder = null
        currentRecordingFile?.delete()
        currentRecordingFile = null
    }

    fun dismissError() { _errorMessage.value = null }

    private fun formatTimestamp(): String =
        SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date())

    override fun onCleared() {
        super.onCleared()
        cleanupRecorder()
        timerJob?.cancel()
    }
}
