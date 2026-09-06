package com.example.echo.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.echo.data.local.SettingsManager
import com.example.echo.theme.*
import com.example.echo.ui.components.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    settingsManager: SettingsManager,
    defaultApiKey: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle()
    val customApiKey by settingsManager.customApiKey.collectAsStateWithLifecycle()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = EchoBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EchoBackground,
                    titleContentColor = EchoTextPrimary,
                    navigationIconContentColor = EchoTextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar + title
            item {
                Spacer(Modifier.height(16.dp))
                Avatar(name = "Echo User", size = 76.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Malayalam Speech AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = EchoTextPrimary
                )
                Text(
                    text = "Gemini 3.6 Flash Powered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EchoTextMuted
                )
                Spacer(Modifier.height(20.dp))
            }

            // AI Configuration
            item {
                SettingsGroup(title = "AI Configuration") {
                    val keyPreview = when {
                        customApiKey.isNotBlank() -> "Custom key active (••••${customApiKey.takeLast(4)})"
                        defaultApiKey.isNotBlank() -> "Default key from .env active"
                        else -> "No API key configured"
                    }
                    SettingsRow(
                        icon = Icons.Outlined.Key,
                        label = "Gemini API Key",
                        value = keyPreview,
                        onClick = { showApiKeyDialog = true }
                    )
                }
            }

            // Preferences
            item {
                Spacer(Modifier.height(8.dp))
                val themeLabel = when (themeMode) {
                    "dark" -> "Dark Mode"
                    "light" -> "Light Mode"
                    else -> "System Default"
                }
                SettingsGroup(title = "Preferences") {
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        label = "Appearance",
                        value = themeLabel,
                        onClick = { showThemeDialog = true }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        label = "Spoken Language",
                        value = "Malayalam (ml-IN) & English",
                        onClick = {}
                    )
                    SettingsRow(
                        icon = Icons.Outlined.GraphicEq,
                        label = "Transcription Model",
                        value = "gemini-3.6-flash",
                        onClick = {}
                    )
                }
            }

            // About
            item {
                Spacer(Modifier.height(8.dp))
                SettingsGroup(title = "About") {
                    SettingsRow(icon = Icons.Outlined.Info, label = "About Echo", value = "v1.0.0") {}
                    SettingsRow(icon = Icons.Outlined.Policy, label = "Privacy", value = "Audio processed via Gemini API") {}
                }
            }

            // Version
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Echo — Malayalam Speech-to-Text v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = EchoTextMuted
                )
            }
        }
    }

    // ── API Key Dialog ────────────────────────────────────────────────────────
    if (showApiKeyDialog) {
        var tempKey by remember { mutableStateOf(customApiKey) }
        var showPassword by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Key, contentDescription = null, tint = EchoPurple)
                    Spacer(Modifier.width(10.dp))
                    Text("Gemini API Key", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your custom Google Gemini API key. If left blank, the app will use the default key configured in .env.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EchoTextSecondary
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (showPassword) "Hide key" else "Show key"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (tempKey.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Key length: ${tempKey.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = EchoTextMuted
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsManager.setCustomApiKey(tempKey)
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoPurple)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    if (customApiKey.isNotBlank()) {
                        TextButton(
                            onClick = {
                                settingsManager.setCustomApiKey("")
                                tempKey = ""
                                showApiKeyDialog = false
                            }
                        ) {
                            Text("Reset to Default", color = EchoError)
                        }
                    }
                    TextButton(onClick = { showApiKeyDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            containerColor = EchoSurface,
            titleContentColor = EchoTextPrimary,
            textContentColor = EchoTextSecondary
        )
    }

    // ── Appearance (Dark Mode) Dialog ─────────────────────────────────────────
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = EchoPurple)
                    Spacer(Modifier.width(10.dp))
                    Text("Appearance", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose your preferred application theme:",
                        style = MaterialTheme.typography.bodySmall,
                        color = EchoTextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ThemeOptionRow(
                        label = "System Default",
                        description = "Matches your Android device theme",
                        isSelected = themeMode == "system",
                        onClick = {
                            settingsManager.setThemeMode("system")
                            showThemeDialog = false
                        }
                    )

                    ThemeOptionRow(
                        label = "Light Mode",
                        description = "Clean, bright aesthetic",
                        isSelected = themeMode == "light",
                        onClick = {
                            settingsManager.setThemeMode("light")
                            showThemeDialog = false
                        }
                    )

                    ThemeOptionRow(
                        label = "Dark Mode",
                        description = "Sleek dark design, easier on the eyes",
                        isSelected = themeMode == "dark",
                        onClick = {
                            settingsManager.setThemeMode("dark")
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = EchoSurface,
            titleContentColor = EchoTextPrimary,
            textContentColor = EchoTextSecondary
        )
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = EchoPurple)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = EchoTextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = EchoTextMuted
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = EchoTextMuted,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EchoSurface),
            border = BorderStroke(1.dp, EchoBorder)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EchoPurple,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = EchoTextPrimary)
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = EchoTextMuted)
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = EchoTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 50.dp), color = EchoBorder)
}
