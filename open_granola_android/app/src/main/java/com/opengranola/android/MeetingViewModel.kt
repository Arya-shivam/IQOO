package com.opengranola.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opengranola.android.data.OpenGranolaDatabase
import com.opengranola.android.data.toEntity
import com.opengranola.android.data.toModel
import com.opengranola.android.model.Meeting
import com.opengranola.android.ai.GeneratedPlan
import com.opengranola.android.ai.GeneratedCommitment
import com.opengranola.android.context.AssistantContext
import com.opengranola.android.context.ContextAssembler
import com.opengranola.android.data.ChatMessageEntity
import com.opengranola.android.data.ChatSessionEntity
import com.opengranola.android.data.ContextEventEntity
import com.opengranola.android.data.MemoryEntity
import com.opengranola.android.data.PlanEntity
import com.opengranola.android.data.PlanTaskEntity
import com.opengranola.android.data.CommitmentEntity
import com.opengranola.android.data.DailyInsightEntity
import com.opengranola.android.data.GoalEntity
import com.opengranola.android.data.GraphEdgeEntity
import com.opengranola.android.data.GraphNodeEntity
import com.opengranola.android.data.CurationQueueEntity
import com.opengranola.android.data.DemoDataSeeder
import com.opengranola.android.ai.LocalCurationWorker
import com.opengranola.android.usage.UsageSnapshot
import com.opengranola.android.calendar.CalendarSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class TaskCompletionChallenge(
    val task: PlanTaskEntity,
    val reasons: List<String>,
    val trackedSeconds: Long,
    val completedToday: Int
)

sealed class ChatTaskAddResult {
    data class Added(val task: PlanTaskEntity, val plan: PlanEntity) : ChatTaskAddResult()
    data class ChoosePlan(val plans: List<PlanEntity>) : ChatTaskAddResult()
    data object NoPlans : ChatTaskAddResult()
}

class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = OpenGranolaDatabase.get(application)
    private val dao = database.meetingDao()
    private val assistantDao = database.assistantDao()
    private val contextAssembler = ContextAssembler(database)
    private val profilePreferences = application.getSharedPreferences("pa_profile", Application.MODE_PRIVATE)
    private val _userName = MutableStateFlow(profilePreferences.getString("user_name", "Friend") ?: "Friend")
    val userName: StateFlow<String> = _userName.asStateFlow()
    private val _taskCompletionChallenge = MutableStateFlow<TaskCompletionChallenge?>(null)
    val taskCompletionChallenge: StateFlow<TaskCompletionChallenge?> = _taskCompletionChallenge.asStateFlow()

    val meetings: StateFlow<List<Meeting>> = dao.observeAll()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(meeting: Meeting) {
        viewModelScope.launch { dao.save(meeting.toEntity()) }
    }

    val memories = assistantDao.observeMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val plans = assistantDao.observePlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val planTasks = assistantDao.observePlanTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val chatMessages = assistantDao.observeMessages(ContextAssembler.DEFAULT_SESSION)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val commitments = assistantDao.observeCommitments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val dailyInsights = assistantDao.observeDailyInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val goals = assistantDao.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val actions = assistantDao.observeActions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val graphEdges = assistantDao.observeEdges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val graphNodes = assistantDao.observeGraphNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val demoNodeCount = assistantDao.observeDemoNodeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    private val _demoState = MutableStateFlow("Load detailed showcase data")
    val demoState: StateFlow<String> = _demoState.asStateFlow()

    init {
        LocalCurationWorker.schedule(application)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            assistantDao.saveSession(ChatSessionEntity(ContextAssembler.DEFAULT_SESSION, "pa", now, now))
        }
    }

    suspend fun buildContext(purpose: String, extra: String = ""): AssistantContext =
        contextAssembler.build(purpose, extra, _userName.value)

    fun updateUserName(value: String) {
        val clean = value.trim().take(60).ifBlank { "Friend" }
        _userName.value = clean
        profilePreferences.edit().putString("user_name", clean).apply()
    }

    fun loadDemoData() {
        viewModelScope.launch {
            _demoState.value = "Building showcase graph…"
            runCatching {
                if (!profilePreferences.contains("pre_demo_user_name")) {
                    profilePreferences.edit().putString("pre_demo_user_name", _userName.value).apply()
                }
                DemoDataSeeder(database).load()
                updateUserName("Aarav")
            }.onSuccess {
                _demoState.value = "Showcase ready · ask pa about work, learning, deadlines or patterns"
            }.onFailure { error ->
                _demoState.value = "Showcase failed: ${error.message ?: "database error"}"
            }
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            _demoState.value = "Removing showcase data…"
            runCatching {
                DemoDataSeeder(database).clear()
                profilePreferences.getString("pre_demo_user_name", null)?.let(::updateUserName)
                profilePreferences.edit().remove("pre_demo_user_name").apply()
            }.onSuccess { _demoState.value = "Showcase removed" }
                .onFailure { _demoState.value = "Remove failed: ${it.message ?: "database error"}" }
        }
    }

    suspend fun saveChat(role: String, content: String) {
        assistantDao.saveMessage(
            ChatMessageEntity(UUID.randomUUID().toString(), ContextAssembler.DEFAULT_SESSION, role, content, System.currentTimeMillis())
        )
    }

    fun addMemory(text: String, source: String = "user") {
        if (text.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID().toString()
            assistantDao.saveMemory(MemoryEntity(id, text.trim(), source, .8f, "", now, now))
            assistantDao.saveGraphNodes(listOf(GraphNodeEntity("memory:$id", "fact", text.trim().take(100), text.trim(), "", "active", id, now, now)))
        }
    }

    fun archiveMemory(id: String) {
        viewModelScope.launch { assistantDao.archiveMemory(id) }
    }

    suspend fun saveGeneratedPlan(generated: GeneratedPlan) {
        val now = System.currentTimeMillis()
        val planId = UUID.randomUUID().toString()
        assistantDao.savePlan(PlanEntity(planId, generated.title, generated.objective, "active", now, now))
        assistantDao.saveGoal(GoalEntity("goal:$planId", generated.title, generated.objective, "active", now, now))
        assistantDao.saveEdges(listOf(GraphEdgeEntity(UUID.randomUUID().toString(), "plan", planId, "goal", "goal:$planId", "derived_from", 1f, generated.objective.take(300), now)))
        val tasks = generated.tasks.mapIndexed { index, task ->
            PlanTaskEntity(
                id = UUID.randomUUID().toString(),
                planId = planId,
                title = task.title,
                details = task.details,
                status = "todo",
                priority = task.priority,
                position = index,
                estimatedMinutes = task.estimatedMinutes.coerceIn(1, 480)
            )
        }
        assistantDao.saveTasks(tasks)
        assistantDao.saveGraphNodes(buildList {
            add(GraphNodeEntity(planId, "plan", generated.title, generated.objective, "", "active", planId, now, now))
            add(GraphNodeEntity("goal:$planId", "goal", generated.title, generated.objective, "", "active", "goal:$planId", now, now))
            tasks.forEach { add(GraphNodeEntity(it.id, "task", it.title, it.details, "", it.status, it.id, now, now)) }
        })
        assistantDao.saveEdges(tasks.map { task ->
            GraphEdgeEntity(UUID.randomUUID().toString(), "plan", planId, "task", task.id, "contains", 1f, "Generated plan task", now)
        })
        val prerequisiteEdges = generated.tasks.flatMapIndexed { index, task ->
            task.dependsOn.mapNotNull { dependencyIndex ->
                val dependency = tasks.getOrNull(dependencyIndex)
                GraphEdgeEntity(
                    UUID.randomUUID().toString(), "task", tasks[index].id, "task",
                    dependency?.id ?: "missing:task:$planId:$dependencyIndex", "requires", 1f,
                    if (dependency == null) "Prerequisite was requested but is not in this plan" else "Generated plan dependency",
                    now
                )
            }
        }
        assistantDao.saveEdges(prerequisiteEdges)
    }

    suspend fun saveStandaloneGoal(title: String) {
        val now = System.currentTimeMillis()
        val id = "goal:${UUID.randomUUID()}"
        assistantDao.saveGoal(GoalEntity(id, title.trim(), title.trim(), "active", now, now))
        assistantDao.saveGraphNodes(listOf(GraphNodeEntity(id, "goal", title.trim(), title.trim(), "", "active", id, now, now)))
    }

    suspend fun addChatTask(title: String, planHint: String? = null): ChatTaskAddResult {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return ChatTaskAddResult.NoPlans

        val activePlans = assistantDao.activePlans(100)
        val matchingPlans = if (planHint.isNullOrBlank()) {
            activePlans
        } else {
            val hint = planHint.trim()
            val exact = activePlans.filter { it.title.equals(hint, ignoreCase = true) }
            if (exact.isNotEmpty()) exact else activePlans.filter {
                it.title.contains(hint, ignoreCase = true) || hint.contains(it.title, ignoreCase = true)
            }
        }
        val plan = when {
            matchingPlans.isEmpty() -> return if (activePlans.isEmpty()) ChatTaskAddResult.NoPlans else ChatTaskAddResult.ChoosePlan(emptyList())
            matchingPlans.size > 1 -> return ChatTaskAddResult.ChoosePlan(matchingPlans.take(5))
            else -> matchingPlans.single()
        }

        val now = System.currentTimeMillis()
        val existingTasks = assistantDao.tasksForPlan(plan.id)
        val task = PlanTaskEntity(
            id = UUID.randomUUID().toString(),
            planId = plan.id,
            title = cleanTitle,
            details = "Added from chat",
            status = "todo",
            priority = 2,
            position = (existingTasks.maxOfOrNull { it.position } ?: -1) + 1,
            estimatedMinutes = 15
        )
        val updatedPlan = plan.copy(updatedAt = now)
        val node = GraphNodeEntity(task.id, "task", task.title, task.details, "", task.status, task.id, now, now)
        val edge = GraphEdgeEntity(
            UUID.randomUUID().toString(), "plan", plan.id, "task", task.id,
            "contains", 1f, "Added from chat", now
        )
        assistantDao.saveTaskToPlan(updatedPlan, task, node, edge)
        return ChatTaskAddResult.Added(task, updatedPlan)
    }

    fun toggleTask(task: PlanTaskEntity) {
        if (task.status == "done") {
            val reopenedStatus = if (task.startedAt > 0) "in_progress" else "todo"
            viewModelScope.launch { assistantDao.reopenTaskAndGraph(task.id, reopenedStatus) }
            return
        }

        val now = System.currentTimeMillis()
        val completedToday = planTasks.value.filter { it.completedAt >= startOfToday() }
        val dependencyIds = graphEdges.value.filter { it.type == "requires" && it.fromId == task.id }.map { it.toId }
        val taskById = planTasks.value.associateBy { it.id }
        val unfinishedDependencies = dependencyIds.filter { dependencyId -> taskById[dependencyId]?.status != "done" }
        val trackedSeconds = if (task.startedAt > 0) ((now - task.startedAt).coerceAtLeast(0L) / 1_000L) else 0L
        val minimumCredibleSeconds = maxOf(30L, task.estimatedMinutes * 60L / 4L)
        val rapidCompletions = completedToday.count { now - it.completedAt <= COMPLETION_BURST_WINDOW_MS }
        val trackedTasks = (completedToday + task).distinctBy { it.id }.filter { it.startedAt > 0 }
        val earliestStart = trackedTasks.minOfOrNull { it.startedAt }
        val claimedMinutes = trackedTasks.sumOf { it.estimatedMinutes }
        val activeWindowMinutes = earliestStart?.let { ((now - it).coerceAtLeast(60_000L) / 60_000L).toInt() } ?: 0
        val reasons = buildList {
            if (unfinishedDependencies.isNotEmpty()) add("${unfinishedDependencies.size} prerequisite${if (unfinishedDependencies.size == 1) " is" else "s are"} still incomplete.")
            if (task.startedAt == 0L) add("You never started tracking this task, so pa has no elapsed-work signal.")
            else if (trackedSeconds < minimumCredibleSeconds) add("Only ${formatDuration(trackedSeconds)} was tracked against a ${task.estimatedMinutes}-minute estimate.")
            if (rapidCompletions >= MAX_RAPID_COMPLETIONS) add("You already checked off $rapidCompletions tasks in the last minute.")
            if (trackedTasks.size >= 2 && activeWindowMinutes > 0 && claimedMinutes > activeWindowMinutes * 2) {
                add("The completed tasks claim $claimedMinutes minutes of work inside a $activeWindowMinutes-minute tracking window.")
            }
        }
        if (reasons.isNotEmpty()) {
            _taskCompletionChallenge.value = TaskCompletionChallenge(task, reasons, trackedSeconds, completedToday.size)
            return
        }

        viewModelScope.launch { assistantDao.completeTaskAndGraph(task.id, now, "", "credible") }
    }

    fun startTask(task: PlanTaskEntity) {
        if (task.status == "done" || task.status == "blocked") return
        viewModelScope.launch { assistantDao.startTaskAndGraph(task.id, System.currentTimeMillis()) }
    }

    fun updateTaskEstimate(taskId: String, minutes: Int) {
        viewModelScope.launch { assistantDao.updateTaskEstimate(taskId, minutes.coerceIn(1, 480)) }
    }

    fun completeChallengedTask(note: String, completedBeforeTracking: Boolean) {
        val challenge = _taskCompletionChallenge.value ?: return
        val cleanNote = note.trim().take(500).ifBlank {
            if (completedBeforeTracking) "Completed before tracking in pa" else "User confirmed completion"
        }
        val credibility = if (note.isNotBlank()) "self_reported" else "unverified"
        viewModelScope.launch {
            assistantDao.completeTaskAndGraph(challenge.task.id, System.currentTimeMillis(), cleanNote, credibility)
        }
        _taskCompletionChallenge.value = null
    }

    fun dismissTaskCompletionChallenge() {
        _taskCompletionChallenge.value = null
    }

    fun deletePlan(id: String) {
        viewModelScope.launch { assistantDao.deletePlan(id) }
    }

    fun deleteMeetingSummary(id: String) {
        viewModelScope.launch {
            dao.clearSummary(id)
            assistantDao.deleteEvent("meeting:$id")
            assistantDao.deleteCommitmentsForMeeting(id)
        }
    }

    suspend fun replaceMeetingCommitments(meeting: Meeting, generated: List<GeneratedCommitment>) {
        val now = System.currentTimeMillis()
        val entities = generated.map { item ->
            CommitmentEntity(
                id = UUID.randomUUID().toString(),
                meetingId = meeting.id,
                sourceTitle = meeting.title,
                title = item.title,
                owner = item.owner,
                dueText = item.dueText,
                evidence = item.evidence,
                confidence = item.confidence,
                status = "open",
                createdAt = now,
                updatedAt = now
            )
        }
        assistantDao.replaceMeetingCommitments(meeting.id, entities)
    }

    fun toggleCommitment(commitment: CommitmentEntity) {
        viewModelScope.launch {
            assistantDao.updateCommitmentStatus(
                commitment.id,
                if (commitment.status == "done") "open" else "done",
                System.currentTimeMillis()
            )
        }
    }

    fun deleteCommitment(id: String) {
        viewModelScope.launch { assistantDao.deleteCommitment(id) }
    }

    suspend fun saveDailyInsight(briefing: String, snapshotId: String) {
        assistantDao.saveDailyInsight(
            DailyInsightEntity(todayKey(), briefing.trim(), snapshotId, 0, System.currentTimeMillis())
        )
    }

    fun rateDailyInsight(date: String, helpful: Boolean) {
        viewModelScope.launch { assistantDao.updateInsightFeedback(date, if (helpful) 1 else -1) }
    }

    fun rememberMeeting(meeting: Meeting) {
        val content = meeting.notes.ifBlank { meeting.transcript }.trim()
        if (content.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            assistantDao.saveEvent(ContextEventEntity("meeting:${meeting.id}", "meeting", "summary", meeting.title, content.take(3000), now, .9f))
            assistantDao.enqueue(CurationQueueEntity("meeting:${meeting.id}", "meeting", meeting.id, meeting.title, content.take(3000), meeting.startedAt, "pending", 0, "", now))
        }
    }

    fun recordUsage(snapshot: UsageSnapshot) {
        if (!snapshot.hasPermission) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val apps = snapshot.apps.joinToString { "${it.label} ${it.minutes}m" }
            val day = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now))
            assistantDao.saveEvent(
                ContextEventEntity("usage:$day", "device", "usage", "Phone usage for $day", "Total ${snapshot.totalMinutes} minutes. Apps: $apps", now, .55f)
            )
            assistantDao.enqueue(CurationQueueEntity("usage:$day", "usage", day, "Phone usage for $day", "Total ${snapshot.totalMinutes} minutes. Apps: $apps", now, "pending", 0, "", now))
        }
    }

    fun recordCalendar(snapshot: CalendarSnapshot) {
        if (!snapshot.hasPermission) return
        viewModelScope.launch {
            val formatter = java.text.SimpleDateFormat("EEE, MMM d h:mm a", java.util.Locale.getDefault())
            val events = snapshot.events.map { event ->
                val reminder = event.reminderMinutes?.let { " Reminder ${it} minutes before." }.orEmpty()
                val location = event.location.takeIf { it.isNotBlank() }?.let { " Location: $it." }.orEmpty()
                ContextEventEntity(
                    id = "calendar:${event.id}:${event.startsAt}",
                    source = "calendar",
                    type = "calendar",
                    title = event.title,
                    content = "Starts ${formatter.format(java.util.Date(event.startsAt))}; calendar ${event.calendarName}.$location$reminder",
                    timestamp = event.startsAt,
                    importance = .75f
                )
            }
            assistantDao.replaceCalendarEvents(events)
            events.forEach { event ->
                assistantDao.enqueue(CurationQueueEntity(event.id, "calendar", event.id, event.title, event.content, event.timestamp, "pending", 0, "", System.currentTimeMillis()))
            }
        }
    }

    private val notificationDao = OpenGranolaDatabase.get(application).notificationDao()
    val recentNotifications = notificationDao.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val notificationsToday = notificationDao.observeCountSince(startOfToday())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun clearNotifications() {
        viewModelScope.launch { notificationDao.clear() }
    }

    private fun startOfToday(): Long = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayKey(): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        .format(java.util.Date())

    private fun formatDuration(seconds: Long): String = when {
        seconds < 60 -> "$seconds seconds"
        else -> "${seconds / 60} minutes"
    }

    private companion object {
        const val COMPLETION_BURST_WINDOW_MS = 60_000L
        const val MAX_RAPID_COMPLETIONS = 3
    }
}
