package com.example.echo.ui.person

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo.domain.model.Person
import com.example.echo.domain.model.Recording
import com.example.echo.theme.*
import com.example.echo.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    person: Person,
    recordings: List<Recording>,
    onBack: () -> Unit,
    onRecordingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = EchoBackground,
        topBar = {
            TopAppBar(
                title = { Text(person.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EchoBackground)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Person header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EchoSurface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, EchoBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(name = person.name, size = 64.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = person.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            if (person.lastInteraction != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Last: ${person.lastInteraction}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EchoTextMuted
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = EchoLavender,
                                border = BorderStroke(1.dp, EchoBorderStrong)
                            ) {
                                Text(
                                    text = "${recordings.size} interaction${if (recordings.size != 1) "s" else ""}",
                                    color = EchoPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recordings section
            item {
                SectionTitle("Recordings & Meetings")
            }

            if (recordings.isEmpty()) {
                item {
                    EmptyState(
                        title = "No recordings yet",
                        subtitle = "Recordings involving ${person.name} will appear here",
                        icon = Icons.Outlined.MicNone
                    )
                }
            } else {
                items(recordings) { recording ->
                    SummaryCard(
                        recording = recording,
                        onClick = { onRecordingClick(recording.id) }
                    )
                }
            }
        }
    }
}
