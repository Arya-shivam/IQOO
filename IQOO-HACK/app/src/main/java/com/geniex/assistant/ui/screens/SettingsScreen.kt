package com.geniex.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geniex.assistant.llm.ModelConfig
import com.geniex.assistant.ui.AssistantUiState

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: AssistantUiState,
    onSaveModelConfig: (String, String) -> Unit
) {
    var path by remember(state.modelPath) { mutableStateOf(state.modelPath) }
    var runtime by remember(state.runtimeName) { mutableStateOf(state.runtimeName) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Runtime Settings", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Set your on-device model directory for GenieX/Qwen integration.")
                    OutlinedTextField(
                        value = runtime,
                        onValueChange = { runtime = it },
                        label = { Text("Runtime name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        label = { Text("Model directory path") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        onSaveModelConfig(
                            path.ifBlank { state.modelPath },
                            runtime.ifBlank { ModelConfig.DEFAULT_RUNTIME }
                        )
                    }) {
                        Text("Save Runtime Config")
                    }
                }
            }
        }
    }
}
