package com.opengranola.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opengranola.android.data.OpenGranolaDatabase
import com.opengranola.android.data.toEntity
import com.opengranola.android.data.toModel
import com.opengranola.android.model.Meeting
import com.opengranola.android.ai.GeneratedPlan
import com.opengranola.android.context.AssistantContext
import com.opengranola.android.context.ContextAssembler
import com.opengranola.android.data.ChatMessageEntity
import com.opengranola.android.data.ChatSessionEntity
import com.opengranola.android.data.ContextEventEntity
import com.opengranola.android.data.MemoryEntity
import com.opengranola.android.data.PlanEntity
import com.opengranola.android.data.PlanTaskEntity
import com.opengranola.android.usage.UsageSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = OpenGranolaDatabase.get(application)
    private val dao = database.meetingDao()
    private val assistantDao = database.assistantDao()
    private val contextAssembler = ContextAssembler(database)
    private val profilePreferences = application.getSharedPreferences("pa_profile", Application.MODE_PRIVATE)
    private val _userName = MutableStateFlow(profilePreferences.getString("user_name", "Friend") ?: "Friend")
    val userName: StateFlow<String> = _userName.asStateFlow()

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

    init {
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

    suspend fun saveChat(role: String, content: String) {
        assistantDao.saveMessage(
            ChatMessageEntity(UUID.randomUUID().toString(), ContextAssembler.DEFAULT_SESSION, role, content, System.currentTimeMillis())
        )
    }

    fun addMemory(text: String, source: String = "user") {
        if (text.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            assistantDao.saveMemory(MemoryEntity(UUID.randomUUID().toString(), text.trim(), source, .8f, "", now, now))
        }
    }

    fun archiveMemory(id: String) {
        viewModelScope.launch { assistantDao.archiveMemory(id) }
    }

    suspend fun saveGeneratedPlan(generated: GeneratedPlan) {
        val now = System.currentTimeMillis()
        val planId = UUID.randomUUID().toString()
        assistantDao.savePlan(PlanEntity(planId, generated.title, generated.objective, "active", now, now))
        assistantDao.saveTasks(generated.tasks.mapIndexed { index, task ->
            PlanTaskEntity(UUID.randomUUID().toString(), planId, task.title, task.details, "todo", task.priority, index)
        })
    }

    fun toggleTask(task: PlanTaskEntity) {
        viewModelScope.launch { assistantDao.updateTaskStatus(task.id, if (task.status == "done") "todo" else "done") }
    }

    fun deletePlan(id: String) {
        viewModelScope.launch { assistantDao.deletePlan(id) }
    }

    fun deleteMeetingSummary(id: String) {
        viewModelScope.launch {
            dao.clearSummary(id)
            assistantDao.deleteEvent("meeting:$id")
        }
    }

    fun rememberMeeting(meeting: Meeting) {
        val content = meeting.notes.ifBlank { meeting.transcript }.trim()
        if (content.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            assistantDao.saveEvent(ContextEventEntity("meeting:${meeting.id}", "meeting", "summary", meeting.title, content.take(3000), now, .9f))
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
}
