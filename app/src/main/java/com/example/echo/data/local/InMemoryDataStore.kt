package com.example.echo.data.local

import android.content.Context
import com.example.echo.domain.model.Interaction
import com.example.echo.domain.model.Person
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistent DataStore that saves data to JSON files in internal storage.
 * Completely free of mock data. Starts fresh with empty lists.
 */
class InMemoryDataStore(private val context: Context? = null) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val recordingsFile: File? get() = context?.let { File(it.filesDir, "echo_recordings.json") }
    private val personsFile: File? get() = context?.let { File(it.filesDir, "echo_persons.json") }
    private val interactionsFile: File? get() = context?.let { File(it.filesDir, "echo_interactions.json") }

    private val _recordings = MutableStateFlow<List<Recording>>(loadRecordings())
    private val _persons = MutableStateFlow<List<Person>>(loadPersons())
    private val _interactions = MutableStateFlow<List<Interaction>>(loadInteractions())

    // ── Recordings ───────────────────────────────────────────────────────────

    val recordings: Flow<List<Recording>> = _recordings

    fun recordingsForPerson(personId: String): Flow<List<Recording>> =
        _recordings.map { list -> list.filter { it.personId == personId } }

    suspend fun getRecordingById(id: String): Recording? =
        _recordings.value.find { it.id == id }

    fun insertRecording(recording: Recording) {
        _recordings.update { list -> listOf(recording) + list.filter { it.id != recording.id } }
        persistRecordings()
    }

    fun updateRecording(recording: Recording) {
        _recordings.update { list -> list.map { if (it.id == recording.id) recording else it } }
        persistRecordings()
    }

    fun deleteRecording(id: String) {
        _recordings.update { list -> list.filter { it.id != id } }
        persistRecordings()
    }

    // ── Persons ──────────────────────────────────────────────────────────────

    val persons: Flow<List<Person>> = _persons

    suspend fun getPersonById(id: String): Person? =
        _persons.value.find { it.id == id }

    fun insertPerson(person: Person) {
        _persons.update { list ->
            val existing = list.find { it.id == person.id || it.name.equals(person.name, ignoreCase = true) }
            if (existing != null) {
                list.map { if (it.id == existing.id) person.copy(id = existing.id) else it }
            } else {
                list + person
            }
        }
        persistPersons()
    }

    fun updatePerson(person: Person) {
        _persons.update { list -> list.map { if (it.id == person.id) person else it } }
        persistPersons()
    }

    // ── Interactions ─────────────────────────────────────────────────────────

    fun interactionsForPerson(personId: String): Flow<List<Interaction>> =
        _interactions.map { list -> list.filter { it.personId == personId } }

    fun insertInteraction(interaction: Interaction) {
        _interactions.update { list -> list + interaction }
        persistInteractions()
    }

    // ── Persistence Helpers ───────────────────────────────────────────────────

    private fun persistRecordings() {
        val file = recordingsFile ?: return
        val currentList = _recordings.value
        scope.launch {
            try {
                val array = JSONArray()
                currentList.forEach { array.put(recordingToJson(it)) }
                file.writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun persistPersons() {
        val file = personsFile ?: return
        val currentList = _persons.value
        scope.launch {
            try {
                val array = JSONArray()
                currentList.forEach { array.put(personToJson(it)) }
                file.writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun persistInteractions() {
        val file = interactionsFile ?: return
        val currentList = _interactions.value
        scope.launch {
            try {
                val array = JSONArray()
                currentList.forEach { array.put(interactionToJson(it)) }
                file.writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadRecordings(): List<Recording> {
        val file = recordingsFile ?: return emptyList()
        return try {
            if (!file.exists()) return emptyList()
            val text = file.readText().trim()
            if (text.isBlank()) return emptyList()
            val array = JSONArray(text)
            val result = mutableListOf<Recording>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(jsonToRecording(obj))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadPersons(): List<Person> {
        val file = personsFile ?: return emptyList()
        return try {
            if (!file.exists()) return emptyList()
            val text = file.readText().trim()
            if (text.isBlank()) return emptyList()
            val array = JSONArray(text)
            val result = mutableListOf<Person>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(jsonToPerson(obj))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadInteractions(): List<Interaction> {
        val file = interactionsFile ?: return emptyList()
        return try {
            if (!file.exists()) return emptyList()
            val text = file.readText().trim()
            if (text.isBlank()) return emptyList()
            val array = JSONArray(text)
            val result = mutableListOf<Interaction>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(jsonToInteraction(obj))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── JSON Converters ───────────────────────────────────────────────────────

    private fun recordingToJson(r: Recording): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("title", r.title)
        put("fileUri", r.fileUri)
        put("fileName", r.fileName)
        put("fileSizeBytes", r.fileSizeBytes)
        put("durationMs", r.durationMs)
        put("createdAt", r.createdAt)
        put("status", r.status.name)
        if (r.transcript != null) put("transcript", r.transcript)
        if (r.summary != null) put("summary", r.summary)
        val insightsArray = JSONArray()
        r.insights.forEach { insightsArray.put(it) }
        put("insights", insightsArray)
        if (r.personId != null) put("personId", r.personId)
        if (r.errorMessage != null) put("errorMessage", r.errorMessage)
    }

    private fun jsonToRecording(obj: JSONObject): Recording {
        val insightsList = mutableListOf<String>()
        val arr = obj.optJSONArray("insights")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                insightsList.add(arr.getString(i))
            }
        }
        val statusVal = try {
            RecordingStatus.valueOf(obj.optString("status", "COMPLETED"))
        } catch (_: Exception) {
            RecordingStatus.COMPLETED
        }
        return Recording(
            id = obj.getString("id"),
            title = obj.getString("title"),
            fileUri = obj.optString("fileUri", ""),
            fileName = obj.optString("fileName", ""),
            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
            durationMs = obj.optLong("durationMs", 0L),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            status = statusVal,
            transcript = if (obj.has("transcript") && !obj.isNull("transcript")) obj.getString("transcript") else null,
            summary = if (obj.has("summary") && !obj.isNull("summary")) obj.getString("summary") else null,
            insights = insightsList,
            personId = if (obj.has("personId") && !obj.isNull("personId")) obj.getString("personId") else null,
            errorMessage = if (obj.has("errorMessage") && !obj.isNull("errorMessage")) obj.getString("errorMessage") else null
        )
    }

    private fun personToJson(p: Person): JSONObject = JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        if (p.avatarUri != null) put("avatarUri", p.avatarUri)
        if (p.lastInteraction != null) put("lastInteraction", p.lastInteraction)
        if (p.lastInteractionDate != null) put("lastInteractionDate", p.lastInteractionDate)
    }

    private fun jsonToPerson(obj: JSONObject): Person = Person(
        id = obj.getString("id"),
        name = obj.getString("name"),
        avatarUri = if (obj.has("avatarUri") && !obj.isNull("avatarUri")) obj.getString("avatarUri") else null,
        lastInteraction = if (obj.has("lastInteraction") && !obj.isNull("lastInteraction")) obj.getString("lastInteraction") else null,
        lastInteractionDate = if (obj.has("lastInteractionDate") && !obj.isNull("lastInteractionDate")) obj.getLong("lastInteractionDate") else null
    )

    private fun interactionToJson(i: Interaction): JSONObject = JSONObject().apply {
        put("id", i.id)
        put("personId", i.personId)
        put("recordingId", i.recordingId)
        put("type", i.type)
        put("date", i.date)
        if (i.summary != null) put("summary", i.summary)
    }

    private fun jsonToInteraction(obj: JSONObject): Interaction = Interaction(
        id = obj.getString("id"),
        personId = obj.getString("personId"),
        recordingId = obj.getString("recordingId"),
        type = obj.optString("type", "meeting"),
        date = obj.optLong("date", System.currentTimeMillis()),
        summary = if (obj.has("summary") && !obj.isNull("summary")) obj.getString("summary") else null
    )
}
