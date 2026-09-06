package com.example.echo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.echo.ui.components.EchoBottomNavBar
import com.example.echo.ui.dashboard.DashboardScreen
import com.example.echo.ui.dashboard.DashboardViewModel
import com.example.echo.ui.history.HistoryScreen
import com.example.echo.ui.history.HistoryViewModel
import com.example.echo.ui.person.PersonDetailScreen
import com.example.echo.ui.profile.ProfileScreen
import com.example.echo.ui.recording.RecordingDetailScreen
import androidx.navigation3.runtime.NavKey

@Composable
fun MainNavigation(container: AppContainer) {
    val backStack = rememberNavBackStack(Dashboard)
    val currentKey = backStack.lastOrNull()
    val isTopLevel = currentKey is Dashboard || currentKey is History

    val dashboardViewModel: DashboardViewModel = viewModel {
        DashboardViewModel(container.recordingRepository, container.personRepository, container.transcriptionService)
    }
    val historyViewModel: HistoryViewModel = viewModel {
        HistoryViewModel(container.recordingRepository, container.personRepository)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Dashboard> {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onRecordingClick = { id -> backStack.add(RecordingDetail(id)) },
                            onAvatarClick = { backStack.add(Profile) }
                        )
                    }
                    entry<History> {
                        HistoryScreen(
                            viewModel = historyViewModel,
                            onPersonClick = { id -> backStack.add(PersonDetail(id)) },
                            onRecordingClick = { id -> backStack.add(RecordingDetail(id)) },
                            onAvatarClick = { backStack.add(Profile) }
                        )
                    }
                    entry<RecordingDetail> { key ->
                        var recording by remember { mutableStateOf<com.example.echo.domain.model.Recording?>(null) }
                        LaunchedEffect(key.recordingId) {
                            recording = container.recordingRepository.getRecordingById(key.recordingId)
                        }
                        recording?.let { rec ->
                            RecordingDetailScreen(
                                recording = rec,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                    entry<PersonDetail> { key ->
                        var person by remember { mutableStateOf<com.example.echo.domain.model.Person?>(null) }
                        val personRecordings by container.recordingRepository
                            .getRecordingsForPerson(key.personId)
                            .collectAsStateWithLifecycle(emptyList())

                        LaunchedEffect(key.personId) {
                            person = container.personRepository.getPersonById(key.personId)
                        }
                        person?.let { p ->
                            PersonDetailScreen(
                                person = p,
                                recordings = personRecordings,
                                onBack = { backStack.removeLastOrNull() },
                                onRecordingClick = { id -> backStack.add(RecordingDetail(id)) }
                            )
                        }
                    }
                    entry<Profile> {
                        ProfileScreen(
                            settingsManager = container.settingsManager,
                            defaultApiKey = BuildConfig.GEMINI_API_KEY,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }

        // Show bottom nav only on top-level screens
        if (isTopLevel) {
            EchoBottomNavBar(
                currentRoute = currentKey,
                onNavigate = { destination ->
                    val navDestination = destination as? NavKey ?: return@EchoBottomNavBar

                    if (backStack.lastOrNull()?.javaClass != navDestination.javaClass) {
                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }

                        if (backStack.firstOrNull()?.javaClass != navDestination.javaClass) {
                            backStack.clear()
                            backStack.add(navDestination)
                        }
                    }
                }
            )
        }
    }
}
