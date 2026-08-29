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
import com.geniex.assistant.model.TaskStatus
import com.geniex.assistant.ui.AssistantUiState
import java.time.LocalDate

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    state: AssistantUiState,
    onCompleteTask: (Long, Long) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Tasks", style = MaterialTheme.typography.headlineSmall)
        }

        items(state.tasks) { task ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text("Status: ${task.status.name}")
                    Text("Priority: ${task.priority}/10")
                    Text("Owner: ${task.owner}")
                    val deadlineText = task.deadlineEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: "None"
                    Text("Deadline: $deadlineText")
                    if (task.details.isNotBlank()) {
                        Text("Details: ${task.details}")
                    }
                    if (task.status != TaskStatus.COMPLETED) {
                        Button(onClick = { onCompleteTask(task.id, task.goalId) }) {
                            Text("Mark Complete")
                        }
                    }
                }
            }
        }
    }
}
