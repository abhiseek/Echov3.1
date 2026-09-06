package com.example.echo.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo.data.repository.PersonRepository
import com.example.echo.data.repository.RecordingRepository
import com.example.echo.domain.model.Person
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(
        val recordings: List<Recording>,
        val persons: List<Person>,
        val selectedPersonId: String? = null
    ) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(
    private val recordingRepository: RecordingRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var _selectedPersonId: String? = null

    init {
        viewModelScope.launch {
            combine(
                recordingRepository.getAllRecordings(),
                personRepository.getAllPersons(),
                _searchQuery
            ) { recordings, persons, query ->
                val q = query.trim()
                val completedRecordings = recordings.filter { it.status == RecordingStatus.COMPLETED }
                val filteredRecordings = if (q.isBlank()) {
                    completedRecordings
                } else {
                    completedRecordings.filter { rec ->
                        rec.title.contains(q, ignoreCase = true) ||
                        (rec.summary?.contains(q, ignoreCase = true) == true) ||
                        (rec.transcript?.contains(q, ignoreCase = true) == true)
                    }
                }

                val filteredPersons = if (q.isBlank()) {
                    persons
                } else {
                    persons.filter { it.name.contains(q, ignoreCase = true) }
                }

                HistoryUiState.Success(
                    recordings = filteredRecordings,
                    persons = filteredPersons,
                    selectedPersonId = _selectedPersonId
                ) as HistoryUiState
            }
                .catch { e -> emit(HistoryUiState.Error(e.message ?: "Unknown error")) }
                .collect { _uiState.value = it }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPerson(personId: String) {
        _selectedPersonId = personId
        val current = _uiState.value
        if (current is HistoryUiState.Success) {
            _uiState.value = current.copy(selectedPersonId = personId)
        }
    }
}
