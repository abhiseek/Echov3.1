package com.example.echo.ui.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import com.example.echo.ui.components.*
import com.example.echo.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onRecordingClick: (String) -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val timerMs by viewModel.recordingTimerMs.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val completedRecordingId by viewModel.completedRecordingId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Auto-navigate to recording detail once completed
    LaunchedEffect(completedRecordingId) {
        completedRecordingId?.let { id ->
            onRecordingClick(id)
            viewModel.clearCompletedRecording()
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            val displayName = try {
                val cursor = context.contentResolver.query(selectedUri, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    if (nameIndex >= 0) it.getString(nameIndex) else "recording"
                } ?: "recording"
            } catch (e: Exception) { "recording.m4a" }
            viewModel.processUploadedFile(context, selectedUri, displayName)
        }
    }

    // Microphone permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording(context)
    }

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
            // Title section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Turn voice into insights",
                        style = MaterialTheme.typography.titleMedium,
                        color = EchoTextPrimary
                    )
                    Text(
                        text = "Upload or record directly to begin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EchoTextMuted
                    )
                }
            }

            // Upload zone
            item {
                Spacer(Modifier.height(16.dp))
                UploadRecordingCard(
                    recordingState = recordingState,
                    timerMs = timerMs,
                    onUploadClick = {
                        filePickerLauncher.launch("audio/*")
                    },
                    onRecordStart = {
                        val permission = Manifest.permission.RECORD_AUDIO
                        micPermissionLauncher.launch(permission)
                    },
                    onRecordStop = { viewModel.stopRecording(context) },
                    onRecordCancel = { viewModel.cancelRecording() }
                )
            }

            // Error message
            if (errorMessage != null) {
                item {
                    ErrorCard(
                        message = errorMessage!!,
                        onRetry = { viewModel.dismissError() }
                    )
                }
            }

            // Processing cards
            when (val state = recordingState) {
                is RecordingState.Processing -> {
                    item {
                        Spacer(Modifier.height(4.dp))
                        ProcessingCard(title = state.fileName, status = state.status)
                    }
                }
                else -> {}
            }

            // Recent summaries
            item {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Recent Summaries")
            }

            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    item { LoadingState() }
                }
                is DashboardUiState.Error -> {
                    item { ErrorCard(message = state.message) }
                }
                is DashboardUiState.Success -> {
                    val completed = state.recordings.filter { it.status == RecordingStatus.COMPLETED }
                    val inProgress = state.recordings.filter {
                        it.status in listOf(RecordingStatus.PROCESSING, RecordingStatus.TRANSCRIBING, RecordingStatus.SUMMARIZING)
                    }
                    val failed = state.recordings.filter { it.status == RecordingStatus.ERROR }

                    items(inProgress) { rec ->
                        ProcessingCard(title = rec.title, status = rec.status)
                    }

                    items(failed) { rec ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = EchoError.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, EchoError.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = EchoError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = rec.title,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EchoTextPrimary
                                    )
                                }
                                if (!rec.errorMessage.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = rec.errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EchoError
                                    )
                                }
                            }
                        }
                    }

                    if (completed.isEmpty() && inProgress.isEmpty() && failed.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No summaries yet",
                                subtitle = "Upload an audio file or record directly to get started",
                                icon = Icons.Outlined.MicNone
                            )
                        }
                    } else {
                        items(completed) { recording ->
                            SummaryCard(
                                recording = recording,
                                onClick = { onRecordingClick(recording.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Upload / Record Card ──────────────────────────────────────────────────────

@Composable
fun UploadRecordingCard(
    recordingState: RecordingState,
    timerMs: Long,
    onUploadClick: () -> Unit,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onRecordCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = recordingState is RecordingState.Recording
    val pulseAnim by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        // Dashed upload drop zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isRecording) EchoLavender else EchoLavender.copy(alpha = 0.6f))
                .clickable { if (!isRecording) onUploadClick() }
                .then(if (isRecording) Modifier.scale(pulseAnim) else Modifier)
        ) {
            val purpleColor = EchoPurple
            // Dashed border drawn with Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 1.5.dp.toPx()
                val dashLength = 10.dp.toPx()
                val gapLength = 6.dp.toPx()
                val cornerRadius = 18.dp.toPx()

                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                left = strokeWidth / 2,
                                top = strokeWidth / 2,
                                right = size.width - strokeWidth / 2,
                                bottom = size.height - strokeWidth / 2
                            ),
                            cornerRadius = CornerRadius(cornerRadius)
                        )
                    )
                }
                drawPath(
                    path = path,
                    color = if (isRecording) purpleColor else purpleColor.copy(alpha = 0.6f),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength))
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isRecording) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Recording",
                        tint = EchoPurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = formatDuration(timerMs),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoPurple
                    )
                    Text(
                        text = "Recording...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EchoTextMuted
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = "Upload",
                        tint = EchoPurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Tap to upload your recording",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Supported formats: MP3, WAV, M4A, OGG, MP4, AAC up to 2 GB",
                        style = MaterialTheme.typography.bodySmall,
                        color = EchoTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (isRecording) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cancel
                OutlinedButton(
                    onClick = onRecordCancel,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EchoBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoTextSecondary)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
                // Stop
                Button(
                    onClick = onRecordStop,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EchoError)
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            // Upload File button
            PrimaryButton(
                text = "Upload File",
                onClick = onUploadClick,
                icon = Icons.Outlined.FileUpload
            )
            Spacer(Modifier.height(10.dp))
            // Record button
            OutlinedButton(
                onClick = onRecordStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, EchoPurple.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EchoPurple)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Record Audio", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}
