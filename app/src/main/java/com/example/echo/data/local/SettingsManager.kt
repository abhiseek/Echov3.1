package com.example.echo.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user preferences: Custom Gemini API Key & Theme Mode (system, light, dark).
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("echo_user_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString(KEY_CUSTOM_API_KEY, "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setCustomApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString(KEY_CUSTOM_API_KEY, trimmed).apply()
        _customApiKey.value = trimmed
    }

    fun getEffectiveApiKey(defaultApiKey: String): String {
        val custom = _customApiKey.value.trim()
        return if (custom.isNotBlank()) custom else defaultApiKey.trim()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
    }
}
