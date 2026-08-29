package com.opengranola.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.opengranola.android.ai.GenieXLocalLlmProvider
import com.opengranola.android.model.Meeting
import com.opengranola.android.recording.RecordingService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OpenGranolaApp() }
    }

    private fun recordingIntent() = Intent(this, RecordingService::class.java)

    @androidx.compose.runtime.Composable
    private fun OpenGranolaApp() {
        val meetings = remember { mutableStateListOf<Meeting>() }
        var selected by remember { mutableStateOf<Meeting?>(null) }
        val scope = rememberCoroutineScope()
        val llm = remember { GenieXLocalLlmProvider() }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) startForegroundService(recordingIntent())
        }

        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                if (selected == null) {
                    MeetingList(
                        meetings = meetings,
                        onOpen = { selected = it },
                        onNew = {
                            val meeting = Meeting(title = "New meeting")
                            meetings.add(0, meeting)
                            selected = meeting
                        }
                    )
                } else {
                    MeetingEditor(
                        meeting = selected!!,
                        providerName = llm.name,
                        onBack = { selected = null },
                        onRecord = {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) startForegroundService(recordingIntent())
                            else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onStopRecording = {
                            startService(Intent(this@MainActivity, RecordingService::class.java).apply {
                                action = RecordingService.ACTION_STOP
                            })
                        },
                        onSummarize = { meeting ->
                            scope.launch {
                                val generated = llm.summarize(meeting.transcript, meeting.notes)
                                val updated = meeting.copy(notes = generated)
                                val index = meetings.indexOfFirst { it.id == meeting.id }
                                if (index >= 0) meetings[index] = updated
                                selected = updated
                            }
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun MeetingList(meetings: List<Meeting>, onOpen: (Meeting) -> Unit, onNew: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Open Granola", style = MaterialTheme.typography.headlineMedium)
        Text("Private meeting notes powered by local AI", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Text("New meeting") }
        Spacer(Modifier.height(16.dp))
        if (meetings.isEmpty()) Text("No meetings yet. Start your first local session.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(meetings, key = { it.id }) { meeting ->
                Card(onClick = { onOpen(meeting) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                        Text("Local transcript and notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun MeetingEditor(
    meeting: Meeting,
    providerName: String,
    onBack: () -> Unit,
    onRecord: () -> Unit,
    onStopRecording: () -> Unit,
    onSummarize: (Meeting) -> Unit
) {
    var title by remember(meeting.id) { mutableStateOf(meeting.title) }
    var transcript by remember(meeting.id) { mutableStateOf(meeting.transcript) }
    var notes by remember(meeting.id) { mutableStateOf(meeting.notes) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Meetings") }
            Text("On-device", color = MaterialTheme.colorScheme.primary)
        }
        OutlinedTextField(title, { title = it }, label = { Text("Meeting title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRecord, modifier = Modifier.weight(1f)) { Text("Start recording") }
            TextButton(onClick = onStopRecording, modifier = Modifier.weight(1f)) { Text("Stop") }
        }
        Spacer(Modifier.height(12.dp))
        Text("Transcript", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            transcript,
            { transcript = it },
            placeholder = { Text("Your local transcript will appear here") },
            minLines = 6,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))
        Text("AI notes · $providerName", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            notes,
            { notes = it },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onSummarize(meeting.copy(title = title, transcript = transcript, notes = notes)) }) {
            Text("Generate local notes")
        }
    }
}
