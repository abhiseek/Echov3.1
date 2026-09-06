package com.example.echo.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Echo Design System Light Colors
val EchoPurple_Light = Color(0xFF5B43C6)
val EchoPurpleDark_Light = Color(0xFF4934A8)
val EchoPurpleLight_Light = Color(0xFF7B66D6)
val EchoLavender_Light = Color(0xFFF2EFFF)
val EchoLavenderDeep_Light = Color(0xFFE8E3FF)
val EchoBackground_Light = Color(0xFFFAFAF8)
val EchoSurface_Light = Color(0xFFFFFFFF)
val EchoCardBg_Light = Color(0xFFFFFFFF)
val EchoTextPrimary_Light = Color(0xFF0D0D0D)
val EchoTextSecondary_Light = Color(0xFF4A4A4A)
val EchoTextMuted_Light = Color(0xFF8E8E93)
val EchoBorder_Light = Color(0xFFE5E3EE)
val EchoBorderStrong_Light = Color(0xFFCCC8E8)

// Echo Design System Dark Colors
val EchoPurple_Dark = Color(0xFF9B85F5)
val EchoPurpleDark_Dark = Color(0xFF7A64E0)
val EchoPurpleLight_Dark = Color(0xFFB6A6FF)
val EchoLavender_Dark = Color(0xFF211F36)
val EchoLavenderDeep_Dark = Color(0xFF2C284C)
val EchoBackground_Dark = Color(0xFF101018)
val EchoSurface_Dark = Color(0xFF181824)
val EchoCardBg_Dark = Color(0xFF181824)
val EchoTextPrimary_Dark = Color(0xFFF4F4F8)
val EchoTextSecondary_Dark = Color(0xFFB0B0C0)
val EchoTextMuted_Dark = Color(0xFF727288)
val EchoBorder_Dark = Color(0xFF2B2B3E)
val EchoBorderStrong_Dark = Color(0xFF3F3F5A)

// Status colors
val EchoError = Color(0xFFD32F2F)
val EchoSuccess = Color(0xFF2E7D32)

data class EchoCustomColors(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val borderStrong: Color,
    val lavender: Color,
    val lavenderDeep: Color,
    val isDark: Boolean
)

val LightCustomColors = EchoCustomColors(
    primary = EchoPurple_Light,
    primaryDark = EchoPurpleDark_Light,
    primaryLight = EchoPurpleLight_Light,
    background = EchoBackground_Light,
    surface = EchoSurface_Light,
    cardBg = EchoCardBg_Light,
    textPrimary = EchoTextPrimary_Light,
    textSecondary = EchoTextSecondary_Light,
    textMuted = EchoTextMuted_Light,
    border = EchoBorder_Light,
    borderStrong = EchoBorderStrong_Light,
    lavender = EchoLavender_Light,
    lavenderDeep = EchoLavenderDeep_Light,
    isDark = false
)

val DarkCustomColors = EchoCustomColors(
    primary = EchoPurple_Dark,
    primaryDark = EchoPurpleDark_Dark,
    primaryLight = EchoPurpleLight_Dark,
    background = EchoBackground_Dark,
    surface = EchoSurface_Dark,
    cardBg = EchoCardBg_Dark,
    textPrimary = EchoTextPrimary_Dark,
    textSecondary = EchoTextSecondary_Dark,
    textMuted = EchoTextMuted_Dark,
    border = EchoBorder_Dark,
    borderStrong = EchoBorderStrong_Dark,
    lavender = EchoLavender_Dark,
    lavenderDeep = EchoLavenderDeep_Dark,
    isDark = true
)

val LocalEchoCustomColors = staticCompositionLocalOf { LightCustomColors }

// Dynamic properties that automatically resolve according to active light/dark theme
val EchoPurple: Color @Composable get() = LocalEchoCustomColors.current.primary
val EchoPurpleDark: Color @Composable get() = LocalEchoCustomColors.current.primaryDark
val EchoPurpleLight: Color @Composable get() = LocalEchoCustomColors.current.primaryLight
val EchoLavender: Color @Composable get() = LocalEchoCustomColors.current.lavender
val EchoLavenderDeep: Color @Composable get() = LocalEchoCustomColors.current.lavenderDeep
val EchoBackground: Color @Composable get() = LocalEchoCustomColors.current.background
val EchoSurface: Color @Composable get() = LocalEchoCustomColors.current.surface
val EchoCardBg: Color @Composable get() = LocalEchoCustomColors.current.cardBg
val EchoTextPrimary: Color @Composable get() = LocalEchoCustomColors.current.textPrimary
val EchoTextSecondary: Color @Composable get() = LocalEchoCustomColors.current.textSecondary
val EchoTextMuted: Color @Composable get() = LocalEchoCustomColors.current.textMuted
val EchoBorder: Color @Composable get() = LocalEchoCustomColors.current.border
val EchoBorderStrong: Color @Composable get() = LocalEchoCustomColors.current.borderStrong
