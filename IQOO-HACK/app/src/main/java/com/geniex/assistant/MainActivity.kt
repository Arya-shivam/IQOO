package com.geniex.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geniex.assistant.ui.GenieXApp
import com.geniex.assistant.ui.AssistantViewModel

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(applicationContext) }

    private val viewModel by viewModels<AssistantViewModel> {
        AssistantViewModel.Factory(container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            GenieXApp(
                state = state,
                onSubmitChatInput = viewModel::submitChatInput,
                onUpdateTask = viewModel::updateTask,
                onClearAllData = viewModel::clearAllData,
                onDismissMessage = viewModel::clearMessage
            )
        }
    }
}
