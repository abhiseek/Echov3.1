package com.example.echo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echo.Dashboard
import com.example.echo.History
import com.example.echo.domain.model.Recording
import com.example.echo.domain.model.RecordingStatus
import com.example.echo.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

// ── EchoHeader ─────────────────────────────────────────────────────────────

@Composable
fun EchoHeader(
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo + wordmark
        Row(verticalAlignment = Alignment.CenterVertically) {
            EchoLogoIcon(size = 34.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Echo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
        Spacer(Modifier.weight(1f))
        if (showAvatar) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EchoLavenderDeep)
                    .border(2.dp, EchoBorderStrong, CircleShape)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = EchoPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ── EchoLogoIcon ────────────────────────────────────────────────────────────

@Composable
fun EchoLogoIcon(size: Dp = 32.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(EchoPurple, EchoPurpleDark),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        WaveformIcon(tint = Color.White, modifier = Modifier.size(size * 0.6f))
    }
}

// ── WaveformIcon ─────────────────────────────────────────────────────────────

@Composable
fun WaveformIcon(
    tint: Color = EchoPurple,
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.defaultMinSize(minWidth = 28.dp, minHeight = 16.dp)) {
        val w = size.width
        val h = size.height
        val mid = h / 2
        val bars = 5
        val barWidth = w / (bars * 2f)
        val maxHeight = h * 0.8f

        val heights = if (animated) {
            listOf(0.3f, 0.8f, 1.0f, 0.8f, 0.3f).mapIndexed { i, base ->
                base * (0.7f + 0.3f * sin((phase + i * 0.8f).toDouble()).toFloat().coerceIn(-1f, 1f))
            }
        } else {
            listOf(0.35f, 0.75f, 1.0f, 0.75f, 0.35f)
        }

        heights.forEachIndexed { i, heightFactor ->
            val x = i * (w / bars) + barWidth / 2
            val barH = maxHeight * heightFactor.coerceAtLeast(0.1f)
            drawLine(
                color = tint,
                start = Offset(x, mid - barH / 2),
                end = Offset(x, mid + barH / 2),
                strokeWidth = barWidth * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

// ── PrimaryButton ─────────────────────────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EchoPurple,
            contentColor = Color.White,
            disabledContainerColor = EchoPurple.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

// ── SecondaryButton ─────────────────────────────────────────────────────────

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EchoTextSecondary
        ),
        border = BorderStroke(1.dp, EchoBorder)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text = text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

// ── SectionTitle ─────────────────────────────────────────────────────────────

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

// ── Avatar ────────────────────────────────────────────────────────────────────

@Composable
fun Avatar(
    name: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercaseChar() ?: '?'
    val hue = (name.hashCode().and(0xFFFFFF) % 60).toFloat() + 200f // blue-purple range
    val color = Color.hsl(hue, 0.5f, 0.7f)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .border(1.5.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            color = color,
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── SummaryCard ───────────────────────────────────────────────────────────────

@Composable
fun SummaryCard(
    recording: Recording,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EchoCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, EchoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EchoLavender),
                    contentAlignment = Alignment.Center
                ) {
                    WaveformIcon(tint = EchoPurple, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatDate(recording.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = EchoTextMuted
                    )
                }
            }

            if (recording.insights.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                recording.insights.take(3).forEach { insight ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = EchoPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else if (recording.summary != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = recording.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EchoTextSecondary
                ),
                border = BorderStroke(1.dp, EchoBorder)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Full Summary & Transcript", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── PersonInteractionCard ─────────────────────────────────────────────────────

@Composable
fun PersonInteractionCard(
    name: String,
    lastInteraction: String?,
    summary: String?,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) EchoPurple else EchoBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) EchoLavender else EchoCardBg

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Avatar(name = name, size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (lastInteraction != null) {
                    Text(
                        text = "Last Meeting: $lastInteraction",
                        style = MaterialTheme.typography.bodySmall,
                        color = EchoTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (summary != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = EchoTextSecondary
                    )
                }
            }
        }
    }
}

// ── EmptyState ────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Outlined.MicNone,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(EchoLavender),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = EchoPurple, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = EchoTextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = EchoTextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── LoadingState ──────────────────────────────────────────────────────────────

@Composable
fun LoadingState(message: String = "Loading...", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = EchoPurple, strokeWidth = 3.dp)
        Spacer(Modifier.height(12.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = EchoTextMuted)
    }
}

// ── ProcessingCard ────────────────────────────────────────────────────────────

@Composable
fun ProcessingCard(
    title: String,
    status: RecordingStatus,
    modifier: Modifier = Modifier
) {
    val statusText = when (status) {
        RecordingStatus.PROCESSING -> "Processing..."
        RecordingStatus.TRANSCRIBING -> "Transcribing..."
        RecordingStatus.SUMMARIZING -> "Generating insights..."
        else -> "Processing..."
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EchoLavender),
        border = BorderStroke(1.dp, EchoBorderStrong)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = EchoPurple,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = EchoPurple)
            }
        }
    }
}

// ── ErrorCard ─────────────────────────────────────────────────────────────────

@Composable
fun ErrorCard(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
        border = BorderStroke(1.dp, Color(0xFFFFCDD2))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = EchoError, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = EchoError)
                if (onRetry != null) {
                    TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
                        Text("Retry", color = EchoError, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── BottomNavBar ──────────────────────────────────────────────────────────────

@Composable
fun EchoBottomNavBar(
    currentRoute: Any,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(Dashboard, "Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(History, "History", Icons.Filled.History, Icons.Outlined.History)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute::class == item.route::class
                val color = if (isSelected) EchoPurple else EchoTextMuted

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onNavigate(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = color
                    )
                }
            }
        }
    }
}

private data class NavItem(
    val route: Any,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

// ── Utils ─────────────────────────────────────────────────────────────────────

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.0f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
