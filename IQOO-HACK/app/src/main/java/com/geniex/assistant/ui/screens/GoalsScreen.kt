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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geniex.assistant.ui.AssistantUiState
import java.time.LocalDate

@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier,
    state: AssistantUiState,
    onCreateGoal: (String, String, LocalDate) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var why by rememberSaveable { mutableStateOf("") }
    var deadlineText by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(14).toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Create Goal", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Goal title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = why,
                        onValueChange = { why = it },
                        label = { Text("Why this goal matters") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deadlineText,
                        onValueChange = { deadlineText = it },
                        label = { Text("Deadline (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        val parsed = runCatching { LocalDate.parse(deadlineText) }.getOrElse { LocalDate.now().plusDays(14) }
                        onCreateGoal(title.trim(), why.trim(), parsed)
                    }) {
                        Text("Create Goal + Auto Plan")
                    }
                }
            }
        }

        item {
            Text("Active Goals", style = MaterialTheme.typography.titleMedium)
        }

        items(state.goals) { goal ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(goal.title, style = MaterialTheme.typography.titleMedium)
                    Text("Why: ${goal.why.ifBlank { "Not specified" }}")
                    Text("Deadline: ${LocalDate.ofEpochDay(goal.deadlineEpochDay)}")
                    Text("Status: ${goal.status.name}")
                }
            }
        }
    }
}
