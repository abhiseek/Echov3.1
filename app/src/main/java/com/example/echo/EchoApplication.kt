package com.example.echo

import android.app.Application
import com.example.echo.data.local.InMemoryDataStore
import com.example.echo.data.local.SettingsManager
import com.example.echo.data.remote.GeminiTranscriptionService
import com.example.echo.data.remote.TranscriptionService
import com.example.echo.data.repository.PersonRepository
import com.example.echo.data.repository.RecordingRepository

/**
 * Application-level dependency container (simple manual DI).
 */
class AppContainer(application: Application) {
    val settingsManager: SettingsManager = SettingsManager(application)
    private val dataStore: InMemoryDataStore = InMemoryDataStore(application)
    val recordingRepository: RecordingRepository = RecordingRepository(dataStore)
    val personRepository: PersonRepository = PersonRepository(dataStore)
    val transcriptionService: TranscriptionService = GeminiTranscriptionService(
        context = application,
        apiKeyProvider = { settingsManager.getEffectiveApiKey(BuildConfig.GEMINI_API_KEY) }
    )
}

class EchoApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
