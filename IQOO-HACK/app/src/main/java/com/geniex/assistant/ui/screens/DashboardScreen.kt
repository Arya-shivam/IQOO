package com.geniex.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geniex.assistant.ui.AssistantUiState

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    state: AssistantUiState,
    onRequestRecommendation: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("GenieX Personal Assistant", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Morning Briefing", style = MaterialTheme.typography.titleMedium)
                    Text(state.morningBriefing)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Proactive Nudges", style = MaterialTheme.typography.titleMedium)
                    if (state.proactiveNudges.isEmpty()) {
                        Text("No urgent nudges right now.")
                    } else {
                        state.proactiveNudges.forEach { nudge -> Text("• $nudge") }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Local Model Runtime", style = MaterialTheme.typography.titleMedium)
                    Text("Runtime: ${state.runtimeName}")
                    Text("Path: ${state.modelPath}")
                    Button(onClick = onRequestRecommendation) {
                        Text("Ask: What should I do now?")
                    }
                    if (state.recommendation.isNotBlank()) {
                        Text(state.recommendation)
                    }
                }
            }
        }

        item {
            Text("Recent Memories", style = MaterialTheme.typography.titleMedium)
        }

        items(state.memories.take(5)) { memory ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(memory.type.name)
                    Text(memory.content)
                    Text("Importance ${memory.importanceScore}/10")
                }
            }
        }
    }
}
