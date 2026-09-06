package com.example.echo.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val EchoLightColorScheme = lightColorScheme(
    primary = EchoPurple_Light,
    onPrimary = Color.White,
    primaryContainer = EchoLavender_Light,
    onPrimaryContainer = EchoPurpleDark_Light,
    secondary = EchoPurpleLight_Light,
    onSecondary = Color.White,
    secondaryContainer = EchoLavenderDeep_Light,
    onSecondaryContainer = EchoPurpleDark_Light,
    background = EchoBackground_Light,
    onBackground = EchoTextPrimary_Light,
    surface = EchoSurface_Light,
    onSurface = EchoTextPrimary_Light,
    surfaceVariant = EchoLavender_Light,
    onSurfaceVariant = EchoTextSecondary_Light,
    outline = EchoBorder_Light,
    outlineVariant = EchoBorderStrong_Light,
    error = EchoError,
    onError = Color.White,
)

private val EchoDarkColorScheme = darkColorScheme(
    primary = EchoPurple_Dark,
    onPrimary = Color.Black,
    primaryContainer = EchoLavender_Dark,
    onPrimaryContainer = EchoPurpleLight_Dark,
    secondary = EchoPurpleLight_Dark,
    onSecondary = Color.Black,
    secondaryContainer = EchoLavenderDeep_Dark,
    onSecondaryContainer = EchoPurple_Dark,
    background = EchoBackground_Dark,
    onBackground = EchoTextPrimary_Dark,
    surface = EchoSurface_Dark,
    onSurface = EchoTextPrimary_Dark,
    surfaceVariant = EchoLavender_Dark,
    onSurfaceVariant = EchoTextSecondary_Dark,
    outline = EchoBorder_Dark,
    outlineVariant = EchoBorderStrong_Dark,
    error = EchoError,
    onError = Color.White,
)

@Composable
fun EchoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) EchoDarkColorScheme else EchoLightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalEchoCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EchoTypography,
            content = content
        )
    }
}
