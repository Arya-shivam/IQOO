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

@Composable
fun MeetingScreen(
    modifier: Modifier = Modifier,
    state: AssistantUiState,
    onProcessMeeting: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("Standup") }
    var transcript by rememberSaveable {
        mutableStateOf(
            "Raj will send API credentials by Tuesday. Once credentials are received, we start integration. Client demo is Friday."
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Meeting Intelligence", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current build uses transcript input. Recorder/STT hook comes next.")
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Meeting title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = transcript,
                        onValueChange = { transcript = it },
                        label = { Text("Transcript") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5
                    )
                    Button(onClick = { onProcessMeeting(title.trim(), transcript.trim()) }) {
                        Text("Extract Tasks + Commitments")
                    }
                }
            }
        }

        item {
            Text("Stored Meetings", style = MaterialTheme.typography.titleMedium)
        }

        items(state.meetings) { meeting ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                    Text(meeting.summary)
                    Text(meeting.transcript)
                }
            }
        }
    }
}
