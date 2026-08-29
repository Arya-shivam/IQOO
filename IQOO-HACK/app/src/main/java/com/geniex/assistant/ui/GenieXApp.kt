package com.geniex.assistant.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.geniex.assistant.ui.screens.DashboardScreen
import com.geniex.assistant.ui.screens.GoalsScreen
import com.geniex.assistant.ui.screens.MeetingScreen
import com.geniex.assistant.ui.screens.SettingsScreen
import com.geniex.assistant.ui.screens.TasksScreen
import java.time.LocalDate

enum class TabItem(val title: String) {
    DASHBOARD("Dashboard"),
    GOALS("Goals"),
    TASKS("Tasks"),
    MEETINGS("Meetings"),
    SETTINGS("Settings")
}

@Composable
fun GenieXApp(
    state: AssistantUiState,
    onCreateGoal: (String, String, LocalDate) -> Unit,
    onCompleteTask: (Long, Long) -> Unit,
    onProcessMeeting: (String, String) -> Unit,
    onSaveModelConfig: (String, String) -> Unit,
    onRequestRecommendation: () -> Unit,
    onDismissMessage: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(TabItem.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TabItem.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.title) },
                        icon = {}
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            TabItem.DASHBOARD -> DashboardScreen(
                modifier = Modifier.padding(innerPadding),
                state = state,
                onRequestRecommendation = onRequestRecommendation
            )

            TabItem.GOALS -> GoalsScreen(
                modifier = Modifier.padding(innerPadding),
                state = state,
                onCreateGoal = onCreateGoal
            )

            TabItem.TASKS -> TasksScreen(
                modifier = Modifier.padding(innerPadding),
                state = state,
                onCompleteTask = onCompleteTask
            )

            TabItem.MEETINGS -> MeetingScreen(
                modifier = Modifier.padding(innerPadding),
                state = state,
                onProcessMeeting = onProcessMeeting
            )

            TabItem.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                state = state,
                onSaveModelConfig = onSaveModelConfig
            )
        }

        state.message?.let { message ->
            AlertDialog(
                onDismissRequest = onDismissMessage,
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = onDismissMessage) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
