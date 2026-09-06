package com.example.echo.ui.recording

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import com.example.echo.theme.*
import com.example.echo.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingDetailScreen(
    recording: Recording,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentMs by remember { mutableLongStateOf(0L) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(recording.id) {
        onDispose {
            mediaPlayer?.apply { stop(); release() }
            mediaPlayer = null
        }
    }

    Scaffold(
        containerColor = EchoBackground,
        topBar = {
            TopAppBar(
                title = { Text("Recording Detail", fontWeight = FontWeight.SemiBold) },
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
            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EchoSurface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, EchoBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(EchoLavender),
                                contentAlignment = Alignment.Center
                            ) {
                                WaveformIcon(tint = EchoPurple, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recording.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = formatDate(recording.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EchoTextMuted
                                )
                            }
                            StatusChip(recording.status)
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = EchoBorder)
                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            MetadataItem("Duration", formatDuration(recording.durationMs))
                            MetadataItem("Size", formatFileSize(recording.fileSizeBytes))
                            MetadataItem("File", recording.fileName.take(12) + if (recording.fileName.length > 12) "…" else "")
                        }
                    }
                }
            }

            // Audio player (only if file exists and completed)
            if (recording.status == RecordingStatus.COMPLETED && recording.fileUri.isNotEmpty()) {
                item {
                    AudioPlayerCard(
                        isPlaying = isPlaying,
                        progress = progress,
                        currentMs = currentMs,
                        totalMs = recording.durationMs,
                        onPlayPause = {
                            if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                try {
                                    if (mediaPlayer == null) {
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(context, android.net.Uri.parse(recording.fileUri))
                                            prepare()
                                            setOnCompletionListener {
                                                isPlaying = false
                                                progress = 0f
                                                currentMs = 0L
                                            }
                                        }
                                    }
                                    mediaPlayer?.start()
                                    isPlaying = true
                                    scope.launch {
                                        while (isPlaying) {
                                            val mp = mediaPlayer ?: break
                                            currentMs = mp.currentPosition.toLong()
                                            progress = if (recording.durationMs > 0) {
                                                currentMs.toFloat() / recording.durationMs
                                            } else 0f
                                            delay(500)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // File might not be available in demo mode
                                    isPlaying = false
                                }
                            }
                        },
                        onSeek = { seekProgress ->
                            mediaPlayer?.let { mp ->
                                val position = (seekProgress * recording.durationMs).toInt()
                                mp.seekTo(position)
                                progress = seekProgress
                                currentMs = position.toLong()
                            }
                        }
                    )
                }
            }

            // AI Summary
            if (recording.summary != null) {
                item {
                    SectionCard(title = "AI Summary") {
                        Text(
                            text = recording.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EchoTextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Key Insights
            if (recording.insights.isNotEmpty()) {
                item {
                    SectionCard(title = "Key Insights") {
                        recording.insights.forEachIndexed { i, insight ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EchoPurple)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(text = insight, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Transcript
            if (recording.transcript != null) {
                item {
                    SectionCard(title = "Transcript") {
                        val segments = recording.transcript.split("\n\n")
                        segments.forEach { rawSegment ->
                            val segment = rawSegment.trim()
                            if (segment.isBlank()) return@forEach

                            val isSpeaker = segment.startsWith("Speaker 1", ignoreCase = true) ||
                                            segment.startsWith("Speaker 2", ignoreCase = true) ||
                                            segment.startsWith("Speaker ", ignoreCase = true)

                            if (isSpeaker) {
                                val colonIdx = segment.indexOf(':')
                                val (speaker, text) = if (colonIdx != -1) {
                                    val spk = segment.substring(0, colonIdx).trim()
                                    val txt = segment.substring(colonIdx + 1).trim()
                                    spk to txt
                                } else {
                                    val lines = segment.lines()
                                    val spk = lines.firstOrNull()?.trim() ?: ""
                                    val txt = lines.drop(1).joinToString(" ").trim()
                                    spk to txt
                                }

                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Text(
                                        text = speaker,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EchoPurple,
                                        fontSize = 13.sp
                                    )
                                    if (text.isNotBlank()) {
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            } else {
                                val isHeader = segment.contains("MALAYALAM") ||
                                               segment.contains("ENGLISH") ||
                                               segment.startsWith("=")
                                Text(
                                    text = segment,
                                    style = if (isHeader) {
                                        MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EchoPurple
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyMedium
                                    },
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Processing state
            if (recording.status != RecordingStatus.COMPLETED && recording.status != RecordingStatus.ERROR) {
                item {
                    ProcessingCard(title = recording.title, status = recording.status)
                }
            }

            // Error state
            if (recording.status == RecordingStatus.ERROR) {
                item {
                    ErrorCard(message = recording.errorMessage ?: "An error occurred during processing")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: RecordingStatus) {
    val (text, color) = when (status) {
        RecordingStatus.COMPLETED -> "Done" to EchoSuccess
        RecordingStatus.ERROR -> "Error" to EchoError
        RecordingStatus.PROCESSING, RecordingStatus.TRANSCRIBING, RecordingStatus.SUMMARIZING -> "Processing" to EchoPurple
        else -> "Pending" to EchoTextMuted
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MetadataItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = EchoTextMuted)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EchoSurface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, EchoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EchoTextPrimary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AudioPlayerCard(
    isPlaying: Boolean,
    progress: Float,
    currentMs: Long,
    totalMs: Long,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EchoLavender),
        border = BorderStroke(1.dp, EchoBorderStrong)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Audio Player", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Slider(
                value = progress,
                onValueChange = onSeek,
                colors = SliderDefaults.colors(
                    thumbColor = EchoPurple,
                    activeTrackColor = EchoPurple,
                    inactiveTrackColor = EchoBorderStrong
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(currentMs), style = MaterialTheme.typography.bodySmall, color = EchoTextMuted)
                Text(formatDuration(totalMs), style = MaterialTheme.typography.bodySmall, color = EchoTextMuted)
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Rewind 10s */ }) {
                    Icon(Icons.Filled.Replay10, contentDescription = "Rewind", tint = EchoPurple)
                }
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = EchoPurple)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { /* Forward 10s */ }) {
                    Icon(Icons.Filled.Forward10, contentDescription = "Forward", tint = EchoPurple)
                }
            }
        }
    }
}
