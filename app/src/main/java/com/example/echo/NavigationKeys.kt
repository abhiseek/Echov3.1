package com.example.echo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Dashboard : NavKey
@Serializable data object History : NavKey
@Serializable data class RecordingDetail(val recordingId: String) : NavKey
@Serializable data class PersonDetail(val personId: String) : NavKey
@Serializable data object Profile : NavKey
