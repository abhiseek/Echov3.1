package com.example.echo.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.echo.theme.*
import com.example.echo.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onPersonClick: (String) -> Unit,
    onRecordingClick: (String) -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = EchoBackground,
        topBar = {
            EchoHeader(onAvatarClick = onAvatarClick)
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Title
            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(12.dp))

                // Search / filter field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search transcriptions & history",
                            color = EchoTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = EchoTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = EchoTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoPurple,
                        unfocusedBorderColor = EchoBorder,
                        focusedContainerColor = EchoSurface,
                        unfocusedContainerColor = EchoSurface
                    )
                )
                Spacer(Modifier.height(12.dp))
            }

            // Body
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    item { LoadingState() }
                }
                is HistoryUiState.Error -> {
                    item { ErrorCard(message = state.message) }
                }
                is HistoryUiState.Success -> {
                    if (state.recordings.isEmpty() && state.persons.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No history yet",
                                subtitle = "Your transcribed audio conversations and recordings will appear here",
                                icon = Icons.Outlined.History
                            )
                        }
                    } else {
                        // Section: Transcriptions
                        if (state.recordings.isNotEmpty()) {
                            item {
                                SectionTitle("Transcriptions (${state.recordings.size})")
                            }
                            items(state.recordings) { recording ->
                                SummaryCard(
                                    recording = recording,
                                    onClick = { onRecordingClick(recording.id) }
                                )
                            }
                        }

                        // Section: Contacts & Speakers
                        if (state.persons.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                SectionTitle("Contacts & Speakers (${state.persons.size})")
                            }
                            itemsIndexed(state.persons) { index, person ->
                                PersonInteractionCard(
                                    name = person.name,
                                    lastInteraction = person.lastInteraction,
                                    summary = null,
                                    isSelected = index == 0 && state.selectedPersonId == null,
                                    onClick = {
                                        viewModel.selectPerson(person.id)
                                        onPersonClick(person.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
