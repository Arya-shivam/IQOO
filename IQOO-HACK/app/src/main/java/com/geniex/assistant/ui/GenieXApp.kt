package com.geniex.assistant.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.ui.screens.AssistantHomeScreen

@Composable
fun GenieXApp(
    state: AssistantUiState,
    onSubmitChatInput: (String) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onClearAllData: () -> Unit,
    onDismissMessage: () -> Unit
) {
    Scaffold { innerPadding ->
        AssistantHomeScreen(
            modifier = Modifier.padding(innerPadding),
            state = state,
            onSubmitChatInput = onSubmitChatInput,
            onUpdateTask = onUpdateTask,
            onClearAllData = onClearAllData
        )

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
