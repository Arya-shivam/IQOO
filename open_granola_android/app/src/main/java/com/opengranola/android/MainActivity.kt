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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.ModelTraining
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import com.opengranola.android.ai.GenieXLocalLlmProvider
import com.opengranola.android.ai.FrontierCoachWorker
import com.opengranola.android.ai.OpenRouterCoachClient
import com.opengranola.android.ai.LocalModelStore
import com.opengranola.android.ai.GenieXModelRepository
import com.opengranola.android.ai.GenieXCatalogModel
import com.opengranola.android.ai.ManagedModel
import com.opengranola.android.ai.AssistantTurn
import com.opengranola.android.ai.InferenceStats
import com.opengranola.android.data.NotificationEntity
import com.opengranola.android.data.MemoryEntity
import com.opengranola.android.data.PlanEntity
import com.opengranola.android.data.PlanTaskEntity
import com.opengranola.android.data.ChatMessageEntity
import com.opengranola.android.data.CommitmentEntity
import com.opengranola.android.data.DailyInsightEntity
import com.opengranola.android.data.GoalEntity
import com.opengranola.android.data.ActionEntity
import com.opengranola.android.data.GraphEdgeEntity
import com.opengranola.android.data.GraphNodeEntity
import com.opengranola.android.model.Meeting
import com.opengranola.android.notification.NotificationReadService
import com.opengranola.android.recording.RecordingService
import com.opengranola.android.recording.LiveTranscriber
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.opengranola.android.usage.AppUsage
import com.opengranola.android.usage.UsageSnapshot
import com.opengranola.android.usage.UsageStatsRepository
import com.opengranola.android.calendar.CalendarRepository
import com.opengranola.android.calendar.CalendarSnapshot
import com.opengranola.android.calendar.LocalCalendarEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

private val DISPLAY_THINK_BLOCK = Regex("<think>.*?</think>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
private val DISPLAY_LEADING_THINK_CLOSE = Regex("^\\s*</think>\\s*", RegexOption.IGNORE_CASE)
private val DISPLAY_SPECIAL_TOKEN = Regex("<\\|[^>]+\\|>|\\[/?INST\\]", RegexOption.IGNORE_CASE)
private val DISPLAY_ROLE_PREFIX = Regex(
    "^\\s*(?:#{1,6}\\s*)?(?:assistant|model|system|user|pa)\\b(?:\\s*[:\\-]\\s*|\\s*\\n+|\\s+)",
    RegexOption.IGNORE_CASE
)
private val CHAT_WRITE_INTENT = Regex(
    "^\\s*(?:please\\s+)?(?:add|create|set|make|build)\\s+(?:a\\s+|my\\s+)?(goal|plan)(?:\\s+(?:to|for))?\\s*[:\\-]?\\s+(.+)$",
    RegexOption.IGNORE_CASE
)

private fun chatWriteIntent(message: String): Pair<String, String>? =
    CHAT_WRITE_INTENT.matchEntire(message)?.destructured?.let { (type, objective) -> type.lowercase() to objective.trim() }
        ?.takeIf { it.second.isNotBlank() }

private fun sanitizeForDisplay(raw: String): String = raw
    .replace(DISPLAY_THINK_BLOCK, "")
    .replace(DISPLAY_LEADING_THINK_CLOSE, "")
    .replace(DISPLAY_SPECIAL_TOKEN, "")
    .replace(DISPLAY_ROLE_PREFIX, "")
    .trim()

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
        val memories by viewModel.memories.collectAsState()
        val plans by viewModel.plans.collectAsState()
        val planTasks by viewModel.planTasks.collectAsState()
        val chatMessages by viewModel.chatMessages.collectAsState()
        val userName by viewModel.userName.collectAsState()
        val commitments by viewModel.commitments.collectAsState()
        val dailyInsights by viewModel.dailyInsights.collectAsState()
        val goals by viewModel.goals.collectAsState()
        val actions by viewModel.actions.collectAsState()
        val graphEdges by viewModel.graphEdges.collectAsState()
        val graphNodes by viewModel.graphNodes.collectAsState()
        val demoNodeCount by viewModel.demoNodeCount.collectAsState()
        val demoState by viewModel.demoState.collectAsState()
        var selectedId by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val llm = remember { GenieXLocalLlmProvider(this@MainActivity) }
        val frontier = remember { OpenRouterCoachClient(this@MainActivity) }
        val modelRepository = remember { GenieXModelRepository(this@MainActivity) }
        val modelStore = remember { LocalModelStore(this@MainActivity) }
        var selectedModel by remember { mutableStateOf(modelStore.selected()?.name) }
        var isRecording by remember { mutableStateOf(false) }
        var liveTranscript by remember { mutableStateOf<String?>(null) }
        var transcriptionState by remember { mutableStateOf("Ready") }
        var summaryState by remember { mutableStateOf("Summary runs after stopping") }
        var modelState by remember { mutableStateOf("No model loaded") }
        var showModelPicker by remember { mutableStateOf(false) }
        var modelDownloadState by remember { mutableStateOf<String?>(null) }
        var downloadedModelIds by remember { mutableStateOf<Set<String>>(emptySet()) }
        var selectedComputeUnit by remember { mutableStateOf(modelRepository.selectedComputeUnit()) }
        var pendingComputeUnit by remember { mutableStateOf(selectedComputeUnit) }
        var frontierConfigured by remember { mutableStateOf(frontier.isConfigured) }
        var frontierModel by remember { mutableStateOf(frontier.model) }
        var frontierKeyDraft by remember { mutableStateOf("") }
        var frontierModelDraft by remember { mutableStateOf(frontier.model) }
        var showFrontierSettings by remember { mutableStateOf(false) }
        var chatState by remember { mutableStateOf("Ready · context stays on this device") }
        var chatBusy by remember { mutableStateOf(false) }
        var streamingResponse by remember { mutableStateOf("") }
        var lastInferenceStats by remember { mutableStateOf<InferenceStats?>(null) }
        var planState by remember { mutableStateOf("Describe an objective and pa will build a plan") }
        var briefingBusy by remember { mutableStateOf(false) }
        var briefingState by remember { mutableStateOf("Connect intention with how today is unfolding") }
        var calendarSnapshot by remember { mutableStateOf(CalendarSnapshot()) }
        var calendarRefresh by remember { mutableIntStateOf(0) }
        val transcriber = remember {
            LiveTranscriber(
                this@MainActivity,
                onTranscript = { liveTranscript = it },
                onState = { transcriptionState = it }
            )
        }
        DisposableEffect(Unit) { onDispose { transcriber.release() } }
        LaunchedEffect(Unit) {
            FrontierCoachWorker.schedule(this@MainActivity)
            modelRepository.selected()?.let { saved ->
                runCatching { modelRepository.paths(saved) }.getOrNull()?.let { paths ->
                    val runtimeId = paths.runtime_id.ifEmpty { "llama_cpp" }
                    val computeUnit = if (runtimeId == "qairt") "npu" else selectedComputeUnit
                    llm.useManagedModel(ManagedModel(paths.model_name, paths.model_path, paths.tokenizer_path, runtimeId, computeUnit))
                    selectedModel = saved.displayName
                    selectedComputeUnit = computeUnit
                    modelState = "Loading · ${computeUnit.uppercase()} · local model"
                    val loadResult = withContext(Dispatchers.IO) { llm.preload() }
                    modelState = loadResult.fold(
                        { "Ready · ${computeUnit.uppercase()} · warmed up" },
                        { "Model load failed · ${it.message ?: "try again"}" }
                    )
                }
            }
        }
        LaunchedEffect(showModelPicker) {
            if (showModelPicker) {
                downloadedModelIds = withContext(Dispatchers.IO) {
                    modelRepository.catalog.mapNotNull { model ->
                        if (runCatching { modelRepository.paths(model) }.getOrNull() != null) model.id else null
                    }.toSet()
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
        val calendarPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { calendarRefresh++ }
        LaunchedEffect(calendarRefresh) {
            val snapshot = withContext(Dispatchers.IO) { CalendarRepository(this@MainActivity).upcoming() }
            calendarSnapshot = snapshot
            viewModel.recordCalendar(snapshot)
        }

        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                if (showFrontierSettings) {
                    AlertDialog(
                        onDismissRequest = { showFrontierSettings = false },
                        title = { Text("Frontier coach") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Only curated summaries are sent to OpenRouter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(
                                    value = frontierKeyDraft,
                                    onValueChange = { frontierKeyDraft = it },
                                    label = { Text("OpenRouter API key") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = frontierModelDraft,
                                    onValueChange = { frontierModelDraft = it },
                                    label = { Text("Model ID") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (frontierKeyDraft.isBlank()) frontier.clearApiKey() else frontier.saveApiKey(frontierKeyDraft)
                                frontier.saveModel(frontierModelDraft)
                                frontierConfigured = frontier.isConfigured
                                frontierModel = frontier.model
                                FrontierCoachWorker.schedule(this@MainActivity)
                                showFrontierSettings = false
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showFrontierSettings = false }) { Text("Cancel") } }
                    )
                }
                if (showModelPicker) {
                    GenieXModelPicker(
                        models = modelRepository.catalog,
                        downloadedModelIds = downloadedModelIds,
                        status = modelDownloadState,
                        computeUnit = pendingComputeUnit,
                        onComputeUnit = { pendingComputeUnit = it },
                        onDismiss = { showModelPicker = false },
                        onUse = { model ->
                            scope.launch {
                                modelDownloadState = "Checking ${model.displayName}…"
                                val paths = modelRepository.paths(model)
                                if (paths == null) {
                                    modelDownloadState = "Download this model first"
                                } else {
                                    val computeUnit = if (paths.runtime_id == "qairt") "npu" else pendingComputeUnit
                                    llm.useManagedModel(ManagedModel(paths.model_name, paths.model_path, paths.tokenizer_path, paths.runtime_id.ifEmpty { model.runtimeId }, computeUnit))
                                    modelRepository.rememberSelected(model, computeUnit)
                                    selectedComputeUnit = computeUnit
                                    selectedModel = model.displayName
                                    modelState = "Loading · ${computeUnit.uppercase()} · local model"
                                    showModelPicker = false
                                    val loadResult = withContext(Dispatchers.IO) { llm.preload() }
                                    modelState = loadResult.fold(
                                        { "Ready · ${computeUnit.uppercase()} · warmed up" },
                                        { "Model load failed · ${it.message ?: "try again"}" }
                                    )
                                }
                            }
                        },
                        onDownload = { model ->
                            scope.launch(Dispatchers.IO) {
                                modelDownloadState = "Downloading ${model.displayName}…"
                                runCatching {
                                    modelRepository.download(model).collect { event ->
                                        when (event) {
                                            is com.geniex.sdk.ModelManagerWrapper.PullEvent.Progress -> {
                                                val total = event.files.sumOf { it.total_bytes.coerceAtLeast(0L) }
                                                val done = event.files.sumOf { it.downloaded_bytes }
                                                val percent = if (total > 0) (done * 100 / total).toInt() else 0
                                                withContext(Dispatchers.Main) { modelDownloadState = "Downloading… $percent%" }
                                            }
                                            is com.geniex.sdk.ModelManagerWrapper.PullEvent.Completed -> withContext(Dispatchers.Main) {
                                                downloadedModelIds = downloadedModelIds + model.id
                                                modelDownloadState = "Downloaded. Tap Use model."
                                            }
                                            is com.geniex.sdk.ModelManagerWrapper.PullEvent.Error -> error(event.message)
                                        }
                                    }
                                }.onFailure { error -> withContext(Dispatchers.Main) { modelDownloadState = "Download failed: ${error.message ?: "check connection"}" } }
                            }
                        }
                    )
                }
                if (selected == null) {
                    var usage by remember { mutableStateOf(UsageSnapshot()) }
                    var usageRefresh by remember { mutableIntStateOf(0) }
                    var notificationAccessEnabled by remember {
                        mutableStateOf(NotificationReadService.isEnabled(this@MainActivity))
                    }
                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        notificationAccessEnabled = NotificationReadService.isEnabled(this@MainActivity)
                        usageRefresh++
                        calendarRefresh++
                    }
                    LaunchedEffect(usageRefresh) {
                        val snapshot = withContext(Dispatchers.IO) { UsageStatsRepository(this@MainActivity).today() }
                        usage = snapshot
                        viewModel.recordUsage(snapshot)
                    }
                    AssistantDashboard(
                        meetings = meetings,
                        userName = userName,
                        usage = usage,
                        calendarSnapshot = calendarSnapshot,
                        modelName = selectedModel,
                        modelState = modelState,
                        recentNotifications = recentNotifications,
                        notificationsToday = notificationsToday,
                        memories = memories,
                        goals = goals,
                        actions = actions,
                        graphEdges = graphEdges,
                        graphNodes = graphNodes,
                        demoLoaded = demoNodeCount > 0,
                        demoState = demoState,
                        plans = plans,
                        planTasks = planTasks,
                        commitments = commitments,
                        dailyInsight = dailyInsights.firstOrNull(),
                        chatMessages = chatMessages,
                        chatState = chatState,
                        chatBusy = chatBusy,
                        streamingResponse = streamingResponse,
                        lastInferenceStats = lastInferenceStats,
                        planState = planState,
                        briefingState = briefingState,
                        briefingBusy = briefingBusy,
                        computeUnit = selectedComputeUnit,
                        frontierConfigured = frontierConfigured,
                        frontierModel = frontierModel,
                        notificationAccessEnabled = notificationAccessEnabled,
                        onUsagePermission = { startActivity(UsageStatsRepository(this@MainActivity).settingsIntent()) },
                        onNotificationPermission = { NotificationReadService.openSettings(this@MainActivity) },
                        onCalendarPermission = { calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR) },
                        onRefreshCalendar = { calendarRefresh++ },
                        onLoadModel = { pendingComputeUnit = selectedComputeUnit; showModelPicker = true },
                        onFrontierSettings = {
                            frontierKeyDraft = ""
                            frontierModelDraft = frontierModel
                            showFrontierSettings = true
                        },
                        onLoadDemo = viewModel::loadDemoData,
                        onClearDemo = viewModel::clearDemoData,
                        onRefreshUsage = { usageRefresh++ },
                        onSendChat = { message ->
                            val writeIntent = chatWriteIntent(message)
                            if (message.isNotBlank() && !frontierConfigured && writeIntent?.first != "goal") {
                                chatState = "Connect OpenRouter in Frontier settings before chatting"
                                frontierKeyDraft = ""
                                frontierModelDraft = frontierModel
                                showFrontierSettings = true
                            } else if (message.isNotBlank()) scope.launch {
                                chatBusy = true
                                streamingResponse = ""
                                try {
                                    viewModel.saveChat("user", message.trim())
                                    val response = when (writeIntent?.first) {
                                        "goal" -> {
                                            viewModel.saveStandaloneGoal(writeIntent.second)
                                            "Goal added: ${writeIntent.second}"
                                        }
                                        "plan" -> {
                                            chatState = "Building your plan…"
                                            val context = viewModel.buildContext("plan generation", writeIntent.second)
                                            val generated = frontier.generatePlan(writeIntent.second, context.text)
                                            viewModel.saveGeneratedPlan(generated)
                                            "Plan created: ${generated.title}. You can review its tasks in Plans."
                                        }
                                        else -> {
                                            chatState = "Retrieving local context…"
                                            val context = viewModel.buildContext("interactive chat", message.trim())
                                            val history = chatMessages.takeLast(4).map { AssistantTurn(it.role, it.content) }
                                            chatState = "Sending curated context to frontier…"
                                            frontier.chat(message.trim(), context.text, history)
                                        }
                                    }
                                    viewModel.saveChat("assistant", response)
                                    lastInferenceStats = InferenceStats(0, 0, "frontier", 0.0, 0.0)
                                    streamingResponse = ""
                                    chatState = "Context used · response stored locally"
                                } catch (error: Throwable) {
                                    streamingResponse = ""
                                    chatState = "Chat failed: ${error.message ?: "check the local model"}"
                                } finally {
                                    chatBusy = false
                                }
                            }
                        },
                        onGeneratePlan = { objective ->
                            if (objective.isNotBlank() && !frontierConfigured) {
                                planState = "Connect OpenRouter in Frontier settings first"
                            } else if (objective.isNotBlank()) scope.launch {
                                runCatching {
                                    planState = "Sending curated context to frontier…"
                                    val context = viewModel.buildContext("plan generation", objective)
                                    frontier.generatePlan(objective.trim(), context.text)
                                }.onSuccess { generated ->
                                    viewModel.saveGeneratedPlan(generated)
                                    planState = "Plan stored on this device"
                                }.onFailure { error -> planState = "Plan failed: ${error.message ?: "check the local model"}" }
                            }
                        },
                        onGenerateBriefing = {
                            if (!frontierConfigured) {
                                briefingState = "Connect OpenRouter in Frontier settings first"
                            } else if (!briefingBusy) scope.launch {
                                briefingBusy = true
                                    briefingState = "Sending curated context to frontier…"
                                    runCatching {
                                        val context = viewModel.buildContext("daily intent-reality briefing")
                                        viewModel.saveDailyInsight(frontier.generateDailyBriefing(context.text), context.snapshotId)
                                }.onSuccess {
                                    briefingState = "Daily briefing stored privately"
                                }.onFailure { error ->
                                    briefingState = "Briefing failed: ${error.message ?: "check the local model"}"
                                }
                                briefingBusy = false
                            }
                        },
                        onAddMemory = viewModel::addMemory,
                        onArchiveMemory = viewModel::archiveMemory,
                        onToggleTask = viewModel::toggleTask,
                        onToggleCommitment = viewModel::toggleCommitment,
                        onDeleteCommitment = viewModel::deleteCommitment,
                        onRateDailyInsight = viewModel::rateDailyInsight,
                        onDeletePlan = viewModel::deletePlan,
                        onDeleteMeetingSummary = viewModel::deleteMeetingSummary,
                        onUpdateUserName = viewModel::updateUserName,
                        onOpen = { selectedId = it.id },
                        onOpenCommitmentSource = { meetingId ->
                            if (meetings.any { it.id == meetingId }) selectedId = meetingId
                        },
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
                        latestSummary = latestSummary,
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
                            // The editor owns the latest text, including the
                            // final partial result. The parent liveTranscript
                            // callback can lag behind the Stop tap, so only
                            // use it when the submitted editor value is empty.
                            val transcriptForSummary = meeting.transcript
                                .takeIf { it.isNotBlank() }
                                ?: liveTranscript.orEmpty()
                            val completed = meeting.copy(transcript = transcriptForSummary)
                            viewModel.save(completed)
                            summaryState = "Summarizing locally…"
                            scope.launch {
                                runCatching {
                                    summarizeLongMeeting(llm, completed.transcript, completed.notes) { summaryState = it }
                                }.onSuccess { summary ->
                                    val summarized = completed.copy(notes = summary)
                                    viewModel.save(summarized)
                                    viewModel.rememberMeeting(summarized)
                                    latestSummary = summary
                                    summaryState = "Extracting commitments…"
                                    runCatching {
                                        llm.extractCommitments(summarized.title, summarized.transcript, summary)
                                    }.onSuccess { extracted ->
                                        viewModel.replaceMeetingCommitments(summarized, extracted)
                                        summaryState = "Summary ready · ${extracted.size} commitments found"
                                    }.onFailure {
                                        summaryState = "Summary ready · commitment extraction unavailable"
                                    }
                                }.onFailure { error ->
                                    summaryState = "Summary failed: ${error.message ?: "check model and device support"}"
                                }
                            }
                        },
                        onSummarize = { meeting ->
                            scope.launch {
                                summaryState = "Starting local model…"
                                runCatching {
                                    summarizeLongMeeting(llm, meeting.transcript, meeting.notes) { summaryState = it }
                                }.onSuccess { generated ->
                                    val summarized = meeting.copy(notes = generated)
                                    viewModel.save(summarized)
                                    viewModel.rememberMeeting(summarized)
                                    latestSummary = generated
                                    summaryState = "Extracting commitments…"
                                    runCatching {
                                        llm.extractCommitments(summarized.title, summarized.transcript, generated)
                                    }.onSuccess { extracted ->
                                        viewModel.replaceMeetingCommitments(summarized, extracted)
                                        summaryState = "Summary ready · ${extracted.size} commitments found"
                                    }.onFailure {
                                        summaryState = "Summary ready · commitment extraction unavailable"
                                    }
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
    userName: String,
    usage: UsageSnapshot,
    calendarSnapshot: CalendarSnapshot,
    modelName: String?,
    modelState: String,
    recentNotifications: List<NotificationEntity>,
    notificationsToday: Int,
    memories: List<MemoryEntity>,
    goals: List<GoalEntity>,
    actions: List<ActionEntity>,
    graphEdges: List<GraphEdgeEntity>,
    graphNodes: List<GraphNodeEntity>,
    demoLoaded: Boolean,
    demoState: String,
    plans: List<PlanEntity>,
    planTasks: List<PlanTaskEntity>,
    commitments: List<CommitmentEntity>,
    dailyInsight: DailyInsightEntity?,
    chatMessages: List<ChatMessageEntity>,
    chatState: String,
    chatBusy: Boolean,
    lastInferenceStats: InferenceStats?,
    streamingResponse: String,
    planState: String,
    briefingState: String,
    briefingBusy: Boolean,
    computeUnit: String,
    frontierConfigured: Boolean,
    frontierModel: String,
    notificationAccessEnabled: Boolean,
    onUsagePermission: () -> Unit,
    onNotificationPermission: () -> Unit,
    onCalendarPermission: () -> Unit,
    onRefreshCalendar: () -> Unit,
    onLoadModel: () -> Unit,
    onFrontierSettings: () -> Unit,
    onLoadDemo: () -> Unit,
    onClearDemo: () -> Unit,
    onRefreshUsage: () -> Unit,
    onSendChat: (String) -> Unit,
    onGeneratePlan: (String) -> Unit,
    onGenerateBriefing: () -> Unit,
    onAddMemory: (String) -> Unit,
    onArchiveMemory: (String) -> Unit,
    onToggleTask: (PlanTaskEntity) -> Unit,
    onToggleCommitment: (CommitmentEntity) -> Unit,
    onDeleteCommitment: (String) -> Unit,
    onRateDailyInsight: (String, Boolean) -> Unit,
    onDeletePlan: (String) -> Unit,
    onDeleteMeetingSummary: (String) -> Unit,
    onUpdateUserName: (String) -> Unit,
    onOpen: (Meeting) -> Unit,
    onOpenCommitmentSource: (String) -> Unit,
    onNew: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var chatDraft by remember { mutableStateOf("") }
    var objectiveDraft by remember { mutableStateOf("") }
    var memoryDraft by remember { mutableStateOf("") }
    var showNameEditor by remember { mutableStateOf(false) }
    var nameDraft by remember(userName) { mutableStateOf(userName) }
    var planPendingDelete by remember { mutableStateOf<PlanEntity?>(null) }
    var meetingPendingSummaryDelete by remember { mutableStateOf<Meeting?>(null) }
    var commitmentPendingDelete by remember { mutableStateOf<CommitmentEntity?>(null) }
    val dashboardScrollState = rememberScrollState()

    LaunchedEffect(tab, chatBusy, chatMessages.size) {
        if (tab == 1 && (chatBusy || chatMessages.isNotEmpty())) {
            delay(120)
            dashboardScrollState.animateScrollTo(dashboardScrollState.maxValue)
        } else if (tab != 1) {
            dashboardScrollState.scrollTo(0)
        }
    }

    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { showNameEditor = false },
            icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            title = { Text("Your name") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it.take(60) },
                    label = { Text("Preferred name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { onUpdateUserName(nameDraft); showNameEditor = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showNameEditor = false }) { Text("Cancel") } }
        )
    }
    planPendingDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planPendingDelete = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text("Delete plan?") },
            text = { Text("${plan.title} and all of its tasks will be removed from this device and from pa's context.") },
            confirmButton = {
                Button(onClick = { onDeletePlan(plan.id); planPendingDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { planPendingDelete = null }) { Text("Cancel") } }
        )
    }
    meetingPendingSummaryDelete?.let { meeting ->
        AlertDialog(
            onDismissRequest = { meetingPendingSummaryDelete = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text("Delete meeting summary?") },
            text = { Text("The transcript stays on your device. Only the saved summary and its chat context are removed.") },
            confirmButton = {
                Button(onClick = { onDeleteMeetingSummary(meeting.id); meetingPendingSummaryDelete = null }) { Text("Delete summary") }
            },
            dismissButton = { TextButton(onClick = { meetingPendingSummaryDelete = null }) { Text("Cancel") } }
        )
    }
    commitmentPendingDelete?.let { commitment ->
        AlertDialog(
            onDismissRequest = { commitmentPendingDelete = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text("Delete commitment?") },
            text = { Text("This removes the commitment from the ledger and from pa's future context. The source meeting remains unchanged.") },
            confirmButton = {
                Button(onClick = { onDeleteCommitment(commitment.id); commitmentPendingDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { commitmentPendingDelete = null }) { Text("Cancel") } }
        )
    }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Transparent) {
                val destinations = listOf(
                    Triple("Home", Icons.Rounded.Home, "Home"),
                    Triple("Chat", Icons.Rounded.ChatBubble, "Chat"),
                    Triple("Plans", Icons.Rounded.Checklist, "Plans"),
                    Triple("Settings", Icons.Rounded.Settings, "Settings")
                )
                destinations.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(item.second, contentDescription = item.third) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    ) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(dashboardScrollState).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("pa", style = MaterialTheme.typography.headlineSmall)
                    Text("Private and on-device", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { nameDraft = userName; showNameEditor = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit your name")
                    }
                }
            }
            if (tab == 0) {
                Text(SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()).uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                TimeOfDayHero(userName, usage)
                DayBriefCard(actions, planTasks)
                IntentRealityCard(
                    commitments = commitments,
                    planTasks = planTasks,
                    usage = usage,
                    notificationsToday = notificationsToday,
                    dailyInsight = dailyInsight,
                    state = briefingState,
                    busy = briefingBusy,
                    onGenerate = onGenerateBriefing,
                    onRate = onRateDailyInsight
                )
                TodayTaskList(planTasks, onToggleTask)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("FOCUS", if (usage.hasPermission) formatMinutes((usage.totalMinutes * .36f).toInt()) else "—", "estimated", Modifier.weight(1f))
                    StatCard("APPS USED", if (usage.hasPermission) usage.pickups.toString() else "—", "today", Modifier.weight(1f))
                    StatCard("ALERTS", notificationsToday.toString(), "today", Modifier.weight(1f))
                }
                if (usage.hasPermission) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Most used today", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = onRefreshUsage) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Refresh")
                        }
                    }
                    usage.apps.forEach { UsageRow(it) }
                }
                if (notificationAccessEnabled) {
                    Text("Recent signals", style = MaterialTheme.typography.titleLarge)
                    if (recentNotifications.isEmpty()) {
                        Text("No notification signals captured yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        recentNotifications.take(3).forEach { notification -> NotificationRow(notification) }
                    }
                }
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Next best action", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Capture what is on your mind before the day gets noisy.", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = onNew) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start a private note")
                        }
                    }
                }
            } else if (tab == 1) {
                Text("PRIVATE CHAT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Frontier coach over local context", style = MaterialTheme.typography.headlineSmall)
                Text(chatState, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                lastInferenceStats?.let { stats -> InferenceStatsCard(stats) }
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF10192B))) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("CONTEXT ENGINE", color = Color(0xFF85F5CB), style = MaterialTheme.typography.labelMedium)
                        Text("Local GenieX curates context; ${frontierModel} writes the reply.", color = Color.White.copy(alpha = .8f))
                    }
                }
                if (chatMessages.isEmpty()) Text("Start a conversation. Messages remain on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                chatMessages.takeLast(20).forEach { ChatBubble(it) }
                if (chatBusy) {
                    ChatLoadingBubble(frontierModel)
                }
                OutlinedTextField(
                    value = chatDraft,
                    onValueChange = { chatDraft = it },
                    placeholder = { Text("Ask pa, add a goal, or create a plan…") },
                    minLines = 2,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { val message = chatDraft; chatDraft = ""; onSendChat(message) },
                    enabled = chatDraft.isNotBlank() && !chatBusy,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (chatBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("pa is responding with $frontierModel")
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Send to frontier")
                    }
                }
            } else if (tab == 2) {
                Text("PLANS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Intent and follow-through", style = MaterialTheme.typography.headlineSmall)
                val standaloneGoals = goals.filter { goal -> goal.status == "active" && plans.none { goal.id == "goal:${it.id}" } }
                Text("Active goals", style = MaterialTheme.typography.titleLarge)
                if (standaloneGoals.isEmpty()) Text("Tell Chat: “Add a goal to …”", color = MaterialTheme.colorScheme.onSurfaceVariant)
                standaloneGoals.forEach { goal ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium)
                            Text("Added from Chat", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Commitment ledger", style = MaterialTheme.typography.titleLarge)
                        Text("Source-linked promises from your meetings", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                        Text(
                            "${commitments.count { it.status == "open" }} OPEN",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (commitments.isEmpty()) {
                    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Summarize a meeting and pa will extract explicit commitments with supporting evidence.")
                        }
                    }
                } else {
                    commitments.forEach { commitment ->
                        CommitmentCard(
                            commitment = commitment,
                            onToggle = { onToggleCommitment(commitment) },
                            onOpenSource = { onOpenCommitmentSource(commitment.meetingId) },
                            onDelete = { commitmentPendingDelete = commitment }
                        )
                    }
                }
                Divider()
                Text("Generated plans", style = MaterialTheme.typography.titleLarge)
                Text(planState, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    objectiveDraft,
                    { objectiveDraft = it },
                    label = { Text("What do you want to accomplish?") },
                    minLines = 2,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { val objective = objectiveDraft; objectiveDraft = ""; onGeneratePlan(objective) },
                    enabled = objectiveDraft.isNotBlank() && !planState.startsWith("Building"),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) { Text("Generate with pa") }
                if (plans.isEmpty()) Text("No plans yet. pa will store generated plans here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                plans.forEach { plan ->
                    PlanCard(plan, planTasks.filter { it.planId == plan.id }, graphEdges, graphNodes, onToggleTask) {
                        planPendingDelete = plan
                    }
                }
            } else {
                Text("SETTINGS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Models, data and permissions", style = MaterialTheme.typography.headlineSmall)
                Text("AI MODELS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                ModelCard(modelName = modelName, modelState = modelState, computeUnit = computeUnit, onLoadModel = onLoadModel)
                FrontierCard(configured = frontierConfigured, model = frontierModel, onClick = onFrontierSettings)
                Text("DATA ACCESS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                SettingsAccessCard(
                    title = "Usage insights",
                    detail = if (usage.hasPermission) "Enabled · ${usage.apps.size} apps available today" else "Allow read-only Usage Access for app patterns",
                    enabled = usage.hasPermission,
                    icon = Icons.Rounded.Insights,
                    action = if (usage.hasPermission) "Refresh" else "Open",
                    onClick = if (usage.hasPermission) onRefreshUsage else onUsagePermission
                )
                SettingsAccessCard(
                    title = "Notification context",
                    detail = if (notificationAccessEnabled) "Enabled · notification text is filtered before frontier use" else "Enable read-only notification signals",
                    enabled = notificationAccessEnabled,
                    icon = Icons.Rounded.Notifications,
                    action = if (notificationAccessEnabled) "Settings" else "Enable",
                    onClick = onNotificationPermission
                )
                CalendarSection(calendarSnapshot, onCalendarPermission, onRefreshCalendar)
                Text("SHOWCASE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                ShowcaseCard(demoLoaded, demoState, onLoadDemo, onClearDemo)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun GenieXModelPicker(
    models: List<GenieXCatalogModel>,
    downloadedModelIds: Set<String>,
    status: String?,
    computeUnit: String,
    onComputeUnit: (String) -> Unit,
    onDismiss: () -> Unit,
    onUse: (GenieXCatalogModel) -> Unit,
    onDownload: (GenieXCatalogModel) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("GenieX local models") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Download once, then run meeting summaries privately on-device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("COMPUTE TARGET", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("cpu", "gpu", "npu").forEach { unit ->
                        if (computeUnit == unit) {
                            Button(onClick = { onComputeUnit(unit) }, modifier = Modifier.weight(1f).height(42.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text(unit.uppercase()) }
                        } else {
                            OutlinedButton(onClick = { onComputeUnit(unit) }, modifier = Modifier.weight(1f).height(42.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text(unit.uppercase()) }
                        }
                    }
                }
                Text("CPU is most compatible. GPU offloads model layers. NPU requires supported Snapdragon scheduling.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                models.forEach { model ->
                    val downloaded = model.id in downloadedModelIds
                    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(model.displayName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                if (downloaded) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(model.modelName, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!downloaded) {
                                    OutlinedButton(onClick = { onDownload(model) }, modifier = Modifier.height(42.dp)) { Text("Download") }
                                }
                                Button(
                                    onClick = { onUse(model) },
                                    enabled = downloaded,
                                    modifier = Modifier.height(42.dp)
                                ) { Text("Use model") }
                            }
                        }
                    }
                }
                if (status != null) Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@androidx.compose.runtime.Composable
private fun TimeOfDayHero(userName: String, usage: UsageSnapshot) {
    val now = remember { Calendar.getInstance() }
    val hour = now.get(Calendar.HOUR_OF_DAY)
    val minuteOfDay = hour * 60 + now.get(Calendar.MINUTE)
    val isDay = minuteOfDay in 360 until 1080
    val orbitProgress = if (isDay) {
        ((minuteOfDay - 360) / 720f).coerceIn(0f, 1f)
    } else {
        val nightMinute = if (minuteOfDay >= 1080) minuteOfDay - 1080 else minuteOfDay + 360
        (nightMinute / 720f).coerceIn(0f, 1f)
    }
    val colors = if (isDay) {
        listOf(Color(0xFF3E67E8), Color(0xFF6A8BF2), Color(0xFFFFB866))
    } else {
        listOf(Color(0xFF111A3A), Color(0xFF27366E), Color(0xFF6A568E))
    }
    val message = remember(userName) { positiveMessage(userName, now.get(Calendar.DAY_OF_YEAR)) }

    Card(shape = RoundedCornerShape(28.dp)) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(228.dp).background(Brush.linearGradient(colors))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = .22f),
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(18.dp.toPx(), 44.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(size.width - 36.dp.toPx(), size.height * 1.05f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
                )
            }
            val x = (maxWidth - 56.dp) * orbitProgress
            val y = (72f - sin(PI * orbitProgress).toFloat() * 48f).dp
            Icon(
                imageVector = if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay,
                contentDescription = if (isDay) "Sun position" else "Moon position",
                tint = if (isDay) Color(0xFFFFE082) else Color(0xFFE7E9FF),
                modifier = Modifier.offset(x = x, y = y).size(48.dp)
            )
            Column(
                Modifier.align(Alignment.BottomStart).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(timeGreeting(hour, userName), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(message, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (usage.hasPermission) "${formatMinutes(usage.totalMinutes)} screen time today" else "Your day is ready when you are",
                    color = Color.White.copy(alpha = .72f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun timeGreeting(hour: Int, userName: String): String = when (hour) {
    in 5..11 -> "Good morning, $userName"
    in 12..16 -> "Good afternoon, $userName"
    in 17..21 -> "Good evening, $userName"
    else -> "A quiet moment, $userName"
}

private fun positiveMessage(userName: String, day: Int): String {
    val messages = listOf(
        "You are building momentum, one thoughtful choice at a time.",
        "Make room for one thing that matters today.",
        "Small progress still counts. Keep going.",
        "Your attention is valuable - spend it with intention.",
        "You have handled difficult days before. Today is yours too."
    )
    return messages[(day + userName.hashCode()).mod(messages.size)]
}

@androidx.compose.runtime.Composable
private fun CalendarSection(
    snapshot: CalendarSnapshot,
    onPermission: () -> Unit,
    onRefresh: () -> Unit
) {
    if (!snapshot.hasPermission) {
        Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            onClick = onPermission
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                Column(Modifier.weight(1f)) {
                    Text("Connect your calendars", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Read upcoming local and synced Google Calendar events and reminders. Access stays on this device.",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text("Allow", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        }
        return
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Upcoming calendar", style = MaterialTheme.typography.titleLarge)
            Text("Local and device-synced calendars", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Sync, contentDescription = "Refresh calendars") }
    }
    if (snapshot.events.isEmpty()) {
        Text("No events found in the next seven days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        snapshot.events.take(5).forEach { CalendarEventCard(it) }
    }
}

@androidx.compose.runtime.Composable
private fun CalendarEventCard(event: LocalCalendarEvent) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.Event, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (event.allDay) "${dateFormat.format(Date(event.startsAt))} · all day"
                    else "${dateFormat.format(Date(event.startsAt))} · ${timeFormat.format(Date(event.startsAt))}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                val details = buildList {
                    if (event.calendarName.isNotBlank()) add(event.calendarName)
                    event.reminderMinutes?.let { add("Reminder ${it}m before") }
                }.joinToString(" · ")
                if (details.isNotBlank()) Text(details, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun DayBriefCard(actions: List<ActionEntity>, tasks: List<PlanTaskEntity>) {
    val now = Calendar.getInstance()
    val night = now.get(Calendar.HOUR_OF_DAY) >= 18
    val start = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        if (!night) add(Calendar.DAY_OF_YEAR, -1)
    }
    val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val completed = actions.filter { it.occurredAt in start.timeInMillis until end.timeInMillis }
        .sortedByDescending { it.occurredAt }.take(4)
    val next = tasks.filter { it.status != "done" && it.status != "blocked" }
        .sortedWith(compareBy<PlanTaskEntity> { it.priority }.thenBy { it.position }).take(4)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (night) "EVENING RECAP" else "MORNING BRIEF", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            BriefList(if (night) "Done today" else "Yesterday", completed.map { it.title }, "No linked activity recorded.")
            Divider()
            BriefList(if (night) "Tomorrow" else "Needs attention today", next.map { it.title }, "Nothing pending — choose a new priority.")
        }
    }
}

@androidx.compose.runtime.Composable
private fun BriefList(title: String, items: List<String>, emptyText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (items.isEmpty()) Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        else items.forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@androidx.compose.runtime.Composable
private fun TodayTaskList(tasks: List<PlanTaskEntity>, onToggle: (PlanTaskEntity) -> Unit) {
    val today = tasks.filter { it.status != "done" && it.status != "blocked" }
        .sortedWith(compareBy<PlanTaskEntity> { it.priority }.thenBy { it.position }).take(6)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Today's tasks", style = MaterialTheme.typography.titleLarge)
        if (today.isEmpty()) Text("No ready tasks today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        today.forEach { task ->
            Surface(onClick = { onToggle(task) }, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = "Complete ${task.title}", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.titleSmall)
                        if (task.details.isNotBlank()) Text(task.details, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun IntentRealityCard(
    commitments: List<CommitmentEntity>,
    planTasks: List<PlanTaskEntity>,
    usage: UsageSnapshot,
    notificationsToday: Int,
    dailyInsight: DailyInsightEntity?,
    state: String,
    busy: Boolean,
    onGenerate: () -> Unit,
    onRate: (String, Boolean) -> Unit
) {
    val openCommitments = commitments.count { it.status == "open" }
    val openTasks = planTasks.count { it.status != "done" }
    val todayInsight = dailyInsight?.takeIf {
        it.date == currentDateKey() && it.briefing.isNotBlank() && !it.briefing.equals("null", ignoreCase = true)
    }
    val topApp = usage.apps.firstOrNull()
    Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF10192B)),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = Color(0xFF85F5CB))
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("INTENT–REALITY", color = Color(0xFF85F5CB), style = MaterialTheme.typography.labelMedium)
                    Text("What matters versus where attention went", color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DarkMetric("COMMITMENTS", openCommitments.toString(), Modifier.weight(1f))
                DarkMetric("PLAN STEPS", openTasks.toString(), Modifier.weight(1f))
                DarkMetric("ALERTS", notificationsToday.toString(), Modifier.weight(1f))
            }
            Text(
                when {
                    openCommitments == 0 && openTasks == 0 -> "No active commitments yet. Summarize a meeting or create a plan to start the loop."
                    usage.hasPermission && topApp != null -> "$openCommitments commitments and $openTasks plan steps are open. ${topApp.label} has used ${formatMinutes(topApp.minutes)} today."
                    else -> "$openCommitments commitments and $openTasks plan steps are open. Enable usage insights to compare them with today's attention."
                },
                color = Color.White.copy(alpha = .86f)
            )
            if (todayInsight != null) {
                Surface(color = Color.White.copy(alpha = .08f), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TODAY'S PRIVATE BRIEFING", color = Color(0xFFFFCE73), style = MaterialTheme.typography.labelMedium)
                        Text(todayInsight.briefing, color = Color.White)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            Text("Useful?", color = Color.White.copy(alpha = .65f), style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = { onRate(todayInsight.date, true) }) {
                                Icon(Icons.Rounded.ThumbUp, contentDescription = "Helpful", tint = if (todayInsight.feedback == 1) Color(0xFF85F5CB) else Color.White.copy(alpha = .7f))
                            }
                            IconButton(onClick = { onRate(todayInsight.date, false) }) {
                                Icon(Icons.Rounded.ThumbDown, contentDescription = "Not helpful", tint = if (todayInsight.feedback == -1) Color(0xFFFF9AAD) else Color.White.copy(alpha = .7f))
                            }
                        }
                    }
                }
            }
            Text(state, color = Color.White.copy(alpha = .62f), style = MaterialTheme.typography.bodySmall)
            Button(onClick = onGenerate, enabled = !busy, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(21.dp), strokeWidth = 2.5.dp)
                    Spacer(Modifier.width(9.dp))
                    Text("Reading today's local signals")
                } else {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (todayInsight == null) "Generate daily briefing" else "Refresh daily briefing")
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun DarkMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(alpha = .08f), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(label, color = Color.White.copy(alpha = .56f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@androidx.compose.runtime.Composable
private fun CommitmentCard(
    commitment: CommitmentEntity,
    onToggle: () -> Unit,
    onOpenSource: () -> Unit,
    onDelete: () -> Unit
) {
    val done = commitment.status == "done"
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (done) "Mark open" else "Mark completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(commitment.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Delete commitment") }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (commitment.owner.isNotBlank()) CommitmentTag("Owner: ${commitment.owner}")
                if (commitment.dueText.isNotBlank()) CommitmentTag("Due: ${commitment.dueText}")
                CommitmentTag("${(commitment.confidence * 100).toInt()}% confidence")
            }
            if (commitment.evidence.isNotBlank()) {
                Text("Evidence", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text("“${commitment.evidence}”", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onOpenSource) {
                Icon(Icons.Rounded.Summarize, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text("Source: ${commitment.sourceTitle}")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CommitmentTag(text: String) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) {
        Text(text, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
    }
}

private fun currentDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

@androidx.compose.runtime.Composable
private fun StatCard(label: String, value: String, caption: String, modifier: Modifier) {
    Card(modifier = modifier) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text(value, style = MaterialTheme.typography.titleLarge); Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@androidx.compose.runtime.Composable
private fun ModelCard(modelName: String?, modelState: String, computeUnit: String, onLoadModel: () -> Unit) {
    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Rounded.ModelTraining, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Local intelligence", style = MaterialTheme.typography.titleMedium)
                Text(modelName ?: "No model selected", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                Text("Compute · ${computeUnit.uppercase()}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                if (modelState != "No model loaded") Text(modelState, color = MaterialTheme.colorScheme.primary, maxLines = 1, style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onLoadModel, modifier = Modifier.height(44.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)) {
                Text(if (modelName == null) "Load" else "Replace")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun FrontierCard(configured: Boolean, model: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFE9EEFF))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = Color(0xFF5367DB), shape = MaterialTheme.shapes.medium, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Frontier coach", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (configured) "Connected · $model" else "Not connected",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Curated context only", color = Color(0xFF5367DB), style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onClick, modifier = Modifier.height(44.dp)) { Text(if (configured) "Settings" else "Connect") }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ShowcaseCard(loaded: Boolean, state: String, onLoad: () -> Unit, onClear: () -> Unit) {
    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFFFF2CC))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (loaded) "SHOWCASE DATA ACTIVE" else "DEVELOPER SHOWCASE", color = Color(0xFF765800), style = MaterialTheme.typography.labelMedium)
                    Text(if (loaded) "Aarav's detailed memory graph is loaded" else "Load a realistic developer profile", style = MaterialTheme.typography.titleMedium)
                    Text(state, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = Color(0xFF765800))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onLoad, modifier = Modifier.weight(1f)) { Text(if (loaded) "Refresh timeline" else "Load showcase") }
                if (loaded) OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Remove") }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SettingsAccessCard(
    title: String,
    detail: String,
    enabled: Boolean,
    icon: ImageVector,
    action: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Text(action, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
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
private fun InferenceStatsCard(stats: InferenceStats) {
    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("LAST INFERENCE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text(stats.computeUnit.uppercase(), style = MaterialTheme.typography.labelMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("INPUT", stats.promptTokens.toString(), "tokens", Modifier.weight(1f))
                StatCard("OUTPUT", stats.generatedTokens.toString(), "tokens", Modifier.weight(1f))
                StatCard("TOTAL", stats.totalTokens.toString(), "tokens", Modifier.weight(1f))
            }
            Text("Prefill ${"%.1f".format(stats.prefillTokensPerSecond)} tok/s · Decode ${"%.1f".format(stats.decodeTokensPerSecond)} tok/s", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@androidx.compose.runtime.Composable
private fun ChatLoadingBubble(frontierModel: String) {
    Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFE9EEFF)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("pa is responding", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Retrieving local context and sending to $frontierModel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ChatBubble(message: ChatMessageEntity) {
    val assistant = message.role == "assistant"
    Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (assistant) Color(0xFFE9EEFF) else MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(if (assistant) "PA" else "YOU", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            Text(message.content)
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlanCard(
    plan: PlanEntity,
    tasks: List<PlanTaskEntity>,
    edges: List<GraphEdgeEntity>,
    nodes: List<GraphNodeEntity>,
    onToggleTask: (PlanTaskEntity) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(plan.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete plan")
                }
            }
            Text(plan.objective, color = MaterialTheme.colorScheme.onSurfaceVariant)
            tasks.forEach { task ->
                val prerequisites = edges.filter { it.type == "requires" && it.fromId == task.id }
                Surface(onClick = { onToggleTask(task) }, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (task.status == "done") Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = if (task.status == "done") "Completed" else "Not completed",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall)
                            if (task.details.isNotBlank()) Text(task.details, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            if (prerequisites.isNotEmpty()) {
                                Text(
                                    "Requires: " + prerequisites.joinToString { dependency ->
                                        val prerequisite = nodes.firstOrNull { it.id == dependency.toId }
                                        (prerequisite?.title ?: tasks.firstOrNull { it.id == dependency.toId }?.title ?: "Missing prerequisite") +
                                            if (prerequisite?.status == "missing") " [MISSING]" else ""
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun GraphMemoryScreen(
    goals: List<GoalEntity>,
    plans: List<PlanEntity>,
    tasks: List<PlanTaskEntity>,
    actions: List<ActionEntity>,
    edges: List<GraphEdgeEntity>,
    nodes: List<GraphNodeEntity>,
    memories: List<MemoryEntity>
) {
    var typeFilter by remember { mutableStateOf("all") }
    val visibleNodes = nodes.filter { typeFilter == "all" || it.type == typeFilter }
    val labels = nodes.associateBy { it.id }
    Text("MEMORY GRAPH", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Text("How pa connects your context", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Plans become goals. Local GenieX links ingested actions to goals. Patterns are computed from those links; missing prerequisites stay visible.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("all", "goal", "plan", "task", "fact", "pattern", "event", "action", "prerequisite").forEach { type ->
            if (typeFilter == type) Button(onClick = { typeFilter = type }) { Text(type.uppercase()) }
            else OutlinedButton(onClick = { typeFilter = type }) { Text(type.uppercase()) }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("NODES", nodes.size.toString(), "typed", Modifier.weight(1f))
        StatCard("LINKS", edges.size.toString(), "evidence", Modifier.weight(1f))
        StatCard("MISSING", nodes.count { it.status == "missing" }.toString(), "prereqs", Modifier.weight(1f))
    }
    visibleNodes.take(60).forEach { graphNode ->
        val nodeEdges = edges.filter { it.fromId == graphNode.id || it.toId == graphNode.id }.take(8)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (graphNode.status == "missing") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(graphNode.type.uppercase(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    Text(graphNode.status.uppercase(), style = MaterialTheme.typography.labelSmall)
                }
                Text(graphNode.title, style = MaterialTheme.typography.titleMedium)
                if (graphNode.details.isNotBlank()) Text(graphNode.details.take(300), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                nodeEdges.forEach { relation ->
                    val outgoing = relation.fromId == graphNode.id
                    val other = labels[if (outgoing) relation.toId else relation.fromId]?.title ?: if (outgoing) relation.toId else relation.fromId
                    Text(if (outgoing) "→ ${relation.type} · $other" else "← ${relation.type} · $other", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    if (nodes.isEmpty()) {
    if (edges.isEmpty() && goals.isEmpty()) {
        Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text("No graph links yet. Generate a plan or let local curation process notifications, calendar and usage signals.", modifier = Modifier.padding(16.dp))
        }
    }
    goals.forEach { goal ->
        val goalEdges = edges.filter { it.toId == goal.id || it.toId == "goal:${goal.id}" || it.fromId == goal.id || it.fromId == "goal:${goal.id}" }
        val goalActions = actions.filter { action -> goalEdges.any { it.fromId == action.id || it.fromId == "action:${action.id}" } }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GOAL · ${goal.title}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text(goal.description, style = MaterialTheme.typography.titleMedium)
                Text(
                    "PATTERN · ${if (goalActions.isEmpty()) "stale — no linked actions yet" else "active — ${goalActions.size} linked action(s)"}",
                    color = if (goalActions.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall
                )
                goalEdges.take(12).forEach { edge ->
                    Text(
                        "${graphNodeLabel(edge.fromType, edge.fromId, goals, plans, tasks, actions)}  — ${edge.type} →  ${graphNodeLabel(edge.toType, edge.toId, goals, plans, tasks, actions)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    val prerequisiteEdges = edges.filter { it.type == "requires" }
    if (prerequisiteEdges.isNotEmpty()) {
        Text("PREREQUISITES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        prerequisiteEdges.forEach { edge ->
            Card(modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "${graphNodeLabel(edge.fromType, edge.fromId, goals, plans, tasks, actions)} requires ${graphNodeLabel(edge.toType, edge.toId, goals, plans, tasks, actions)}",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (edge.toId.startsWith("missing:")) Text("Not present in memory or this plan", modifier = Modifier.padding(start = 14.dp, bottom = 12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (memories.isNotEmpty()) {
        Text("FACTS / INFO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text("Saved facts are local nodes. They will be linked when curation has evidence for a goal; they are not guessed into a relationship.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        memories.take(8).forEach { memory ->
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text("• ${memory.text.take(180)}", modifier = Modifier.padding(12.dp))
            }
        }
    }
    }
}

private fun graphNodeLabel(
    type: String,
    id: String,
    goals: List<GoalEntity>,
    plans: List<PlanEntity>,
    tasks: List<PlanTaskEntity>,
    actions: List<ActionEntity>
): String {
    if (id.startsWith("missing:")) return "Missing prerequisite"
    return when (type) {
        "goal" -> goals.firstOrNull { it.id == id || "goal:${it.id}" == id }?.title ?: id
        "plan" -> plans.firstOrNull { it.id == id || "plan:${it.id}" == id }?.title ?: id
        "task" -> tasks.firstOrNull { it.id == id }?.title ?: id
        "action" -> actions.firstOrNull { it.id == id || "action:${it.id}" == id }?.title ?: id
        else -> id
    }
}

@androidx.compose.runtime.Composable
private fun MemoryCard(memory: MemoryEntity, onArchive: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Rounded.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(memory.text)
                Text(memory.source, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = { onArchive(memory.id) }) { Text("Forget") }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlanRow(title: String, detail: String, done: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private fun formatMinutes(minutes: Int): String = if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"

/** Map-reduce summarization keeps long meetings within the local model context window. */
private suspend fun summarizeLongMeeting(
    provider: com.opengranola.android.ai.LocalLlmProvider,
    transcript: String,
    userNotes: String,
    onProgress: (String) -> Unit = {}
): String {
    if (transcript.isBlank()) return provider.summarize(transcript, userNotes, context = "")

    val chunks = transcript.chunked(8_000)
    val summaries = mutableListOf<String>()
    for (index in chunks.indices) {
        onProgress("Summarizing section ${index + 1} of ${chunks.size}…")
        summaries += provider.summarize(chunks[index], "Meeting section ${index + 1} of ${chunks.size}. $userNotes", context = "")
    }
    var combined = summaries.joinToString("\n\n") { it.trim() }
    while (combined.length > 8_000) {
        val reduced = mutableListOf<String>()
        val groups = combined.chunked(8_000)
        for ((index, group) in groups.withIndex()) {
            onProgress("Combining summary pass · section ${index + 1} of ${groups.size}…")
            reduced += provider.summarize(group, "Combine these meeting section summaries into a faithful summary. $userNotes", context = "")
        }
        combined = reduced.joinToString("\n\n")
    }
    onProgress("Writing final summary…")
    return provider.summarize(combined, "Create the final meeting summary with decisions, action items, and open questions. $userNotes", context = "")
}

@androidx.compose.runtime.Composable
private fun MeetingList(meetings: List<Meeting>, onOpen: (Meeting) -> Unit, onNew: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("pa", style = MaterialTheme.typography.headlineMedium)
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
    latestSummary: String?,
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

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF5F7FF), Color(0xFFEDF7F4), Color(0xFFF8F9FC)))
        )
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                TextButton(
                    onClick = { onBack(meeting.copy(title = title, transcript = transcript, notes = notes)) },
                    modifier = Modifier.height(44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Meetings")
                }
                Spacer(Modifier.weight(1f))
                SessionPill(if (isRecording) "LIVE" else "LOCAL", isRecording)
            }

            Surface(
                color = Color(0xFF10192B),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PRIVATE MEETING NODE", color = Color(0xFF85F5CB), style = MaterialTheme.typography.labelMedium)
                    Text("Capture the signal.\nKeep the context.", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        if (isRecording) "Listening securely on this device" else "Ready for an on-device session",
                        color = Color.White.copy(alpha = .68f)
                    )
                }
            }

            OutlinedTextField(
                title,
                { title = it },
                label = { Text("Session title") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRecord,
                    enabled = !isRecording,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Start capture")
                }
                OutlinedButton(
                    onClick = { onStopRecording(meeting.copy(title = title, transcript = transcript, notes = notes)) },
                    enabled = isRecording,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Rounded.StopCircle, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Stop")
                }
            }

            Surface(
                color = Color.White.copy(alpha = .9f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFDDE4F2), RoundedCornerShape(24.dp))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("LIVE TRANSCRIPT", color = Color(0xFF4968D8), style = MaterialTheme.typography.labelMedium)
                            Text(transcriptionState, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(
                            Icons.Rounded.Mic,
                            contentDescription = if (isRecording) "Recording" else "Recorder idle",
                            tint = if (isRecording) Color(0xFFFF4E6A) else Color(0xFF97A1B5)
                        )
                    }
                    OutlinedTextField(
                        transcript,
                        { transcript = it },
                        placeholder = { Text("Speech will materialize here…") },
                        minLines = 7,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Surface(
                color = Color(0xFFE9EEFF),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFC9D5FF), RoundedCornerShape(24.dp))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("AI SYNTHESIS", color = Color(0xFF4A5FD3), style = MaterialTheme.typography.labelMedium)
                            Text("$providerName · ${modelName ?: "configure model on Home"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF5367DB))
                    }
                    Text(summaryState, color = Color(0xFF5367DB), style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        notes,
                        { notes = it },
                        placeholder = { Text("Structured decisions, actions and open questions will appear here.") },
                        minLines = 6,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSummarize(meeting.copy(title = title, transcript = transcript, notes = notes)) },
                        enabled = transcript.isNotBlank(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Rounded.Summarize, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Synthesize meeting")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@androidx.compose.runtime.Composable
private fun SessionPill(label: String, active: Boolean) {
    Surface(
        color = if (active) Color(0xFFFFE8EC) else Color(0xFFE5F5EF),
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(
            1.dp,
            if (active) Color(0xFFFF9AAD) else Color(0xFF9BDDC6),
            RoundedCornerShape(50)
        )
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            color = if (active) Color(0xFFD93353) else Color(0xFF24795C),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
