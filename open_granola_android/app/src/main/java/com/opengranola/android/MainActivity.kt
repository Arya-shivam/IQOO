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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.opengranola.android.ai.GenieXLocalLlmProvider
import com.opengranola.android.ai.LocalModelStore
import com.opengranola.android.data.NotificationEntity
import com.opengranola.android.model.Meeting
import com.opengranola.android.notification.NotificationReadService
import com.opengranola.android.recording.RecordingService
import com.opengranola.android.recording.LiveTranscriber
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.opengranola.android.usage.AppUsage
import com.opengranola.android.usage.UsageSnapshot
import com.opengranola.android.usage.UsageStatsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OpenGranolaApp() }
    }

    private fun recordingIntent() = Intent(this, RecordingService::class.java)

    @androidx.compose.runtime.Composable
    private fun OpenGranolaApp() {
        val viewModel: MeetingViewModel = viewModel()
        val meetings by viewModel.meetings.collectAsState()
        val recentNotifications by viewModel.recentNotifications.collectAsState()
        val notificationsToday by viewModel.notificationsToday.collectAsState()
        var selectedId by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val llm = remember { GenieXLocalLlmProvider(this@MainActivity) }
        val modelStore = remember { LocalModelStore(this@MainActivity) }
        var selectedModel by remember { mutableStateOf(modelStore.selected()?.name) }
        var isRecording by remember { mutableStateOf(false) }
        var liveTranscript by remember { mutableStateOf<String?>(null) }
        var transcriptionState by remember { mutableStateOf("Ready") }
        var summaryState by remember { mutableStateOf("Summary runs after stopping") }
        var modelState by remember { mutableStateOf("No model loaded") }
        val transcriber = remember {
            LiveTranscriber(
                this@MainActivity,
                onTranscript = { liveTranscript = it },
                onState = { transcriptionState = it }
            )
        }
        DisposableEffect(Unit) { onDispose { transcriber.release() } }
        val modelLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                modelState = "Loading model… copying it into private storage"
                scope.launch(Dispatchers.IO) {
                    runCatching { modelStore.import(uri, uri.lastPathSegment, contentResolver) }
                        .onSuccess { model -> withContext(Dispatchers.Main) {
                            selectedModel = model.name
                            modelState = "Loaded · ${model.length() / (1024 * 1024)} MB · stored privately"
                        }}
                        .onFailure { error -> withContext(Dispatchers.Main) {
                            modelState = "Model load failed: ${error.message ?: "unknown error"}"
                        }}
                }
            }
        }
        val selected = meetings.firstOrNull { it.id == selectedId }
        var latestSummary by remember(selectedId) { mutableStateOf<String?>(null) }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startForegroundService(recordingIntent())
                isRecording = true
                liveTranscript = ""
                transcriber.start()
            }
        }

        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                if (selected == null) {
                    var usage by remember { mutableStateOf(UsageSnapshot()) }
                    var usageRefresh by remember { mutableIntStateOf(0) }
                    var notificationAccessEnabled by remember {
                        mutableStateOf(NotificationReadService.isEnabled(this@MainActivity))
                    }
                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        notificationAccessEnabled = NotificationReadService.isEnabled(this@MainActivity)
                        usageRefresh++
                    }
                    LaunchedEffect(usageRefresh) {
                        withContext(Dispatchers.IO) { usage = UsageStatsRepository(this@MainActivity).today() }
                    }
                    AssistantDashboard(
                        meetings = meetings,
                        usage = usage,
                        modelName = selectedModel,
                        modelState = modelState,
                        recentNotifications = recentNotifications,
                        notificationsToday = notificationsToday,
                        notificationAccessEnabled = notificationAccessEnabled,
                        onUsagePermission = { startActivity(UsageStatsRepository(this@MainActivity).settingsIntent()) },
                        onNotificationPermission = { NotificationReadService.openSettings(this@MainActivity) },
                        onLoadModel = { modelLauncher.launch(arrayOf("*/*")) },
                        onRefreshUsage = { usageRefresh++ },
                        onOpen = { selectedId = it.id },
                        onNew = {
                            val meeting = Meeting(title = "New meeting")
                            viewModel.save(meeting)
                            selectedId = meeting.id
                        }
                    )
                } else {
                    MeetingEditor(
                        meeting = selected,
                        providerName = llm.name,
                        modelName = selectedModel,
                        isRecording = isRecording,
                        liveTranscript = liveTranscript,
                        transcriptionState = transcriptionState,
                        summaryState = summaryState,
                        modelState = modelState,
                        latestSummary = latestSummary,
                        onLoadModel = { modelLauncher.launch(arrayOf("*/*")) },
                        onBack = { meeting ->
                            viewModel.save(meeting)
                            selectedId = null
                        },
                        onRecord = {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                startForegroundService(recordingIntent())
                                isRecording = true
                                liveTranscript = ""
                                transcriber.start()
                            } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onStopRecording = { meeting ->
                            startService(Intent(this@MainActivity, RecordingService::class.java).apply {
                                action = RecordingService.ACTION_STOP
                            })
                            isRecording = false
                            transcriber.stop()
                            val completed = meeting.copy(
                                transcript = liveTranscript?.takeIf { it.isNotBlank() } ?: meeting.transcript
                            )
                            viewModel.save(completed)
                            summaryState = "Summarizing locally…"
                            scope.launch {
                                runCatching {
                                    summarizeLongMeeting(llm, completed.transcript, completed.notes, notificationContext(recentNotifications)) { summaryState = it }
                                }.onSuccess { summary ->
                                    viewModel.save(completed.copy(notes = summary))
                                    latestSummary = summary
                                    summaryState = "Summary ready"
                                }.onFailure { error ->
                                    summaryState = "Summary failed: ${error.message ?: "check model and device support"}"
                                }
                            }
                        },
                        onSummarize = { meeting ->
                            scope.launch {
                                summaryState = "Starting local model…"
                                runCatching {
                                    summarizeLongMeeting(llm, meeting.transcript, meeting.notes, notificationContext(recentNotifications)) { summaryState = it }
                                }.onSuccess { generated ->
                                    viewModel.save(meeting.copy(notes = generated))
                                    latestSummary = generated
                                    summaryState = "Summary ready"
                                }.onFailure { error ->
                                    summaryState = "Summary failed: ${error.message ?: "check model and device support"}"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AssistantDashboard(
    meetings: List<Meeting>,
    usage: UsageSnapshot,
    modelName: String?,
    modelState: String,
    recentNotifications: List<NotificationEntity>,
    notificationsToday: Int,
    notificationAccessEnabled: Boolean,
    onUsagePermission: () -> Unit,
    onNotificationPermission: () -> Unit,
    onLoadModel: () -> Unit,
    onRefreshUsage: () -> Unit,
    onOpen: (Meeting) -> Unit,
    onNew: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Transparent) {
                listOf("Pulse" to "⌁", "Memory" to "◌", "Plans" to "✓").forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Text(item.second, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    ) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Good evening, Arya", style = MaterialTheme.typography.headlineSmall)
                    Text("Your private command center", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                    Text("LOCAL", modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            if (tab == 0) {
                Text("TODAY · AUG 29", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Your day at a glance", color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelLarge)
                                Text(if (usage.hasPermission) formatMinutes(usage.totalMinutes) else "—", color = Color.White, style = MaterialTheme.typography.displaySmall)
                                Text("screen time today", color = Color.White.copy(alpha = .8f))
                            }
                            Text("✦", color = Color.White, style = MaterialTheme.typography.displayMedium)
                        }
                        Text("A calmer day starts with noticing where your attention goes.", color = Color.White.copy(alpha = .9f))
                    }
                }
                if (!usage.hasPermission) {
                    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), onClick = onUsagePermission) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("◉", style = MaterialTheme.typography.titleLarge)
                            Column(Modifier.weight(1f)) {
                                Text("Turn on usage insights", style = MaterialTheme.typography.titleMedium)
                                Text("Allow Usage Access to see your real phone patterns. Nothing leaves this device.", color = MaterialTheme.colorScheme.onTertiaryContainer, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Open", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                ModelCard(modelName = modelName, modelState = modelState, onLoadModel = onLoadModel)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("FOCUS", if (usage.hasPermission) formatMinutes((usage.totalMinutes * .36f).toInt()) else "—", "estimated", Modifier.weight(1f))
                    StatCard("APPS USED", if (usage.hasPermission) usage.pickups.toString() else "—", "today", Modifier.weight(1f))
                    StatCard("ALERTS", notificationsToday.toString(), "today", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Most used today", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onRefreshUsage) { Text("Refresh") }
                }
                if (usage.apps.isEmpty()) {
                    Text("Your app patterns will appear here once Usage Access is enabled.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    usage.apps.forEach { UsageRow(it) }
                }
                Text("Recent signals", style = MaterialTheme.typography.titleLarge)
                if (!notificationAccessEnabled) {
                    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), onClick = onNotificationPermission) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("✦", style = MaterialTheme.typography.titleLarge)
                            Column(Modifier.weight(1f)) {
                                Text("Connect notifications", style = MaterialTheme.typography.titleMedium)
                                Text("Read-only, redacted notification context helps your assistant understand what needs attention.", color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Enable", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else if (recentNotifications.isEmpty()) {
                    Text("No notification signals captured yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    recentNotifications.take(3).forEach { notification ->
                        NotificationRow(notification)
                    }
                }
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Next best action", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Capture what is on your mind before the day gets noisy.", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = onNew) { Text("Start a private note") }
                    }
                }
            } else if (tab == 1) {
                Text("MEMORY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("A quiet record of what matters", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Text("Add memory") }
                meetings.forEach { meeting ->
                    Card(onClick = { onOpen(meeting) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                            Text(if (meeting.notes.isBlank()) "No summary yet · tap to open" else meeting.notes.take(110), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (meetings.isEmpty()) Text("Your saved notes and local summaries will live here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("PLANS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Make space for the next right thing", style = MaterialTheme.typography.headlineSmall)
                PlanRow("Review phone patterns", "Understand yesterday's attention budget", true)
                PlanRow("Build the assistant core", "Context → memory → decide → act", false)
                PlanRow("Create a focus block", "Suggested after your morning review", false)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun StatCard(label: String, value: String, caption: String, modifier: Modifier) {
    Card(modifier = modifier) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text(value, style = MaterialTheme.typography.titleLarge); Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@androidx.compose.runtime.Composable
private fun ModelCard(modelName: String?, modelState: String, onLoadModel: () -> Unit) {
    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium, modifier = Modifier.size(44.dp)) {
                Text("✦", color = Color.White, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text("Local intelligence", style = MaterialTheme.typography.titleMedium)
                Text(modelName ?: "No model selected", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                if (modelState != "No model loaded") Text(modelState, color = MaterialTheme.colorScheme.primary, maxLines = 1, style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onLoadModel, modifier = Modifier.height(44.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)) {
                Text(if (modelName == null) "Load" else "Replace")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun UsageRow(app: AppUsage) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(app.label, style = MaterialTheme.typography.titleMedium); Text(formatMinutes(app.minutes), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        LinearProgressIndicator(progress = { app.share }, modifier = Modifier.fillMaxWidth().height(7.dp), trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@androidx.compose.runtime.Composable
private fun NotificationRow(notification: NotificationEntity) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.size(38.dp)) {
            Text(notification.appLabel.take(1).uppercase(), modifier = Modifier.padding(10.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(Modifier.weight(1f)) {
            Text(notification.title, style = MaterialTheme.typography.titleSmall)
            if (notification.body.isNotBlank()) Text(notification.body, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlanRow(title: String, detail: String, done: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text(if (done) "✓" else "○", color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleLarge); Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
}

private fun formatMinutes(minutes: Int): String = if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"

/** Map-reduce summarization keeps long meetings within the local model context window. */
private suspend fun summarizeLongMeeting(
    provider: com.opengranola.android.ai.LocalLlmProvider,
    transcript: String,
    userNotes: String,
    phoneContext: String = "",
    onProgress: (String) -> Unit = {}
): String {
    if (transcript.isBlank()) return provider.summarize(transcript, userNotes, phoneContext)

    val chunks = transcript.chunked(8_000)
    val summaries = mutableListOf<String>()
    for (index in chunks.indices) {
        onProgress("Summarizing section ${index + 1} of ${chunks.size}…")
        summaries += provider.summarize(chunks[index], "Meeting section ${index + 1} of ${chunks.size}. $userNotes", phoneContext)
    }
    var combined = summaries.joinToString("\n\n") { it.trim() }
    while (combined.length > 8_000) {
        val reduced = mutableListOf<String>()
        val groups = combined.chunked(8_000)
        for ((index, group) in groups.withIndex()) {
            onProgress("Combining summary pass · section ${index + 1} of ${groups.size}…")
            reduced += provider.summarize(group, "Combine these meeting section summaries into a faithful summary. $userNotes", phoneContext)
        }
        combined = reduced.joinToString("\n\n")
    }
    onProgress("Writing final summary…")
    return provider.summarize(combined, "Create the final meeting summary with decisions, action items, and open questions. $userNotes", phoneContext)
}

private fun notificationContext(notifications: List<NotificationEntity>): String =
    notifications.take(5).joinToString("\n") { "${it.appLabel}: ${it.title}. ${it.body}" }

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
    modelName: String?,
    isRecording: Boolean,
    liveTranscript: String?,
    transcriptionState: String,
    summaryState: String,
    modelState: String,
    latestSummary: String?,
    onLoadModel: () -> Unit,
    onBack: (Meeting) -> Unit,
    onRecord: () -> Unit,
    onStopRecording: (Meeting) -> Unit,
    onSummarize: (Meeting) -> Unit
) {
    var title by remember(meeting.id) { mutableStateOf(meeting.title) }
    var transcript by remember(meeting.id) { mutableStateOf(meeting.transcript) }
    var notes by remember(meeting.id) { mutableStateOf(meeting.notes) }
    LaunchedEffect(liveTranscript) {
        if (liveTranscript != null && liveTranscript.isNotBlank()) transcript = liveTranscript
    }
    LaunchedEffect(latestSummary) {
        if (!latestSummary.isNullOrBlank()) notes = latestSummary
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextButton(onClick = {
                onBack(meeting.copy(title = title, transcript = transcript, notes = notes))
            }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)) { Text("‹  Meetings") }
            Spacer(Modifier.width(12.dp))
            Text("Meeting workspace", style = MaterialTheme.typography.titleMedium)
        }
        Text("CAPTURE & REFLECT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(title, { title = it }, label = { Text("Meeting title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    if (isRecording) "● Recording in progress" else "○ Not recording",
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    if (isRecording) "Audio is being saved privately on this device" else "Start recording when everyone has consented",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onRecord, enabled = !isRecording, modifier = Modifier.weight(1f).height(50.dp)) { Text("Start recording") }
            TextButton(
                onClick = { onStopRecording(meeting.copy(title = title, transcript = transcript, notes = notes)) },
                enabled = isRecording,
                modifier = Modifier.weight(1f).height(50.dp)
            ) { Text("Stop") }
        }
        Spacer(Modifier.height(12.dp))
        Text("Transcript", style = MaterialTheme.typography.titleMedium)
        Text(
            "Live transcription: $transcriptionState · Speaker labels require a diarization model",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Text(summaryState, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
        Text(
            if (modelName == null) "No local model selected" else "Model: $modelName",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(modelState, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = onLoadModel) { Text("Load local model") }
        OutlinedTextField(
            notes,
            { notes = it },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onSummarize(meeting.copy(title = title, transcript = transcript, notes = notes)) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Generate local notes")
        }
    }
}
