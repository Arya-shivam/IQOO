package com.geniex.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geniex.assistant.AppContainer
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.llm.ModelConfig
import com.geniex.assistant.model.GoalInput
import com.geniex.assistant.model.GoalStatus
import com.geniex.assistant.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import java.io.File
import java.time.LocalDate

class AssistantViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        observeData()
        ensureDefaultSettings()
    }

    fun createGoal(title: String, why: String, deadline: LocalDate) {
        if (title.isBlank()) {
            postMessage("Goal title is required")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            container.coordinator.createGoalAndPlan(GoalInput(title, why, deadline))
            refreshBriefingAndNudges(_uiState.value.tasks)
            _uiState.update { it.copy(loading = false, message = "Goal + plan created") }
        }
    }

    fun completeTask(taskId: Long, goalId: Long) {
        viewModelScope.launch {
            container.coordinator.markTaskComplete(taskId, goalId)
            refreshBriefingAndNudges(_uiState.value.tasks)
        }
    }

    fun processMeeting(title: String, transcript: String) {
        if (transcript.isBlank()) {
            postMessage("Transcript cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val summary = container.coordinator.processMeetingTranscript(title.ifBlank { "Meeting" }, transcript)
            refreshBriefingAndNudges(_uiState.value.tasks)
            _uiState.update { it.copy(loading = false, message = summary) }
        }
    }

    fun analyzeCapturedMeeting(title: String, audioPath: String?, capturedNotes: String) {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            val modelError = modelMissingMessage()
            _uiState.update {
                it.copy(
                    loading = true,
                    modelError = modelError,
                    assistantSummary = "I am reviewing what I heard and turning it into a plan..."
                )
            }
            yield()

            try {
                val analysis = withTimeout(180_000) {
                    withContext(Dispatchers.Default) {
                        container.coordinator.processCapturedMeeting(
                            title = title.ifBlank { "Recorded meeting" },
                            capturedNotes = capturedNotes,
                            audioPath = audioPath
                        )
                    }
                }

                val openTasks = withContext(Dispatchers.Default) {
                    container.repository.getOpenTasks()
                }
                refreshBriefingAndNudges(openTasks)
                _uiState.update {
                    it.copy(
                        loading = false,
                        timetable = analysis.timetable,
                        assistantSummary = analysis.summary,
                        recommendation = analysis.summary,
                        modelError = modelMissingMessage()
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        assistantSummary = "I could not finish analyzing that update. Your captured words are still on screen, so please try once more.",
                        modelError = modelMissingMessage(),
                        message = throwable.message?.take(160) ?: "Analysis could not be completed"
                    )
                }
            }
        }
    }

    fun submitChatInput(input: String) {
        if (input.isBlank()) {
            postMessage("Tell me what changed first.")
            return
        }
        analyzeCapturedMeeting("Chat input", null, input.trim())
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            container.repository.updateTask(task.copy(updatedAtEpochMs = System.currentTimeMillis()))
            val openTasks = container.repository.getOpenTasks()
            _uiState.update {
                it.copy(
                    timetable = container.coordinator.buildCurrentTimetable(openTasks),
                    assistantSummary = "Updated. I have adjusted the plan around that task."
                )
            }
        }
    }

    fun saveModelConfig(path: String, runtime: String) {
        viewModelScope.launch {
            container.repository.upsertSetting(ModelConfig.KEY_MODEL_DIRECTORY, path)
            container.repository.upsertSetting(ModelConfig.KEY_ACTIVE_RUNTIME, runtime)
            _uiState.update {
                it.copy(
                    modelPath = path,
                    runtimeName = runtime,
                    message = "Runtime settings saved"
                )
            }
        }
    }

    fun requestRecommendation() {
        viewModelScope.launch {
            val tasks = _uiState.value.tasks
            if (tasks.isEmpty()) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        recommendation = "No data yet",
                        timetable = emptyList(),
                        assistantSummary = "I do not see any open work yet. Record a meeting or add a quick voice note and I will turn it into a plan."
                    )
                }
                return@launch
            }

            val context = buildRecommendationContext(tasks)

            _uiState.update {
                it.copy(
                    loading = true,
                    recommendation = "Running local model..."
                )
            }

            val recommendation = runCatching {
                withTimeout(120_000) {
                    container.coordinator.runtimeRecommendation(context)
                }
            }.getOrElse { throwable ->
                "Could not get model response: ${throwable.message ?: "timeout"}. Try again."
            }

            _uiState.update {
                it.copy(
                    loading = false,
                    recommendation = recommendation.ifBlank {
                        "Model returned an empty response. Try again."
                    },
                    assistantSummary = recommendation.ifBlank {
                        "Model returned an empty response. Try again."
                    }
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            container.repository.clearAllData()
            ensureDefaultSettings()
            _uiState.update {
                it.copy(
                    goals = emptyList(),
                    tasks = emptyList(),
                    meetings = emptyList(),
                    memories = emptyList(),
                    timetable = emptyList(),
                    assistantSummary = "",
                    recommendation = "",
                    morningBriefing = "No briefing yet.",
                    proactiveNudges = emptyList(),
                    message = "All local assistant data cleared"
                )
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            container.repository.observeGoals().collectLatest { goals ->
                _uiState.update { it.copy(goals = goals) }
            }
        }
        viewModelScope.launch {
            container.repository.observeTasks().collectLatest { tasks ->
                _uiState.update {
                    it.copy(
                        tasks = tasks,
                        timetable = container.coordinator.buildCurrentTimetable(tasks)
                    )
                }
                refreshBriefingAndNudges(tasks)
            }
        }
        viewModelScope.launch {
            container.repository.observeMeetings().collectLatest { meetings ->
                _uiState.update { current ->
                    val latestReply = meetings.firstOrNull()?.assistantReply.orEmpty()
                    current.copy(
                        meetings = meetings,
                        assistantSummary = current.assistantSummary.ifBlank { latestReply },
                        recommendation = current.recommendation.ifBlank { latestReply }
                    )
                }
            }
        }
        viewModelScope.launch {
            container.repository.observeMemories().collectLatest { memories ->
                _uiState.update { it.copy(memories = memories) }
            }
        }
        viewModelScope.launch {
            container.repository.observeSettings().collectLatest { settings ->
                val modelPath = settings.firstOrNull { it.key == ModelConfig.KEY_MODEL_DIRECTORY }?.value
                    ?: container.defaultModelDirectoryPath
                val runtimeName = settings.firstOrNull { it.key == ModelConfig.KEY_ACTIVE_RUNTIME }?.value
                    ?: ModelConfig.DEFAULT_RUNTIME
                _uiState.update {
                    it.copy(
                        modelPath = modelPath,
                        runtimeName = runtimeName,
                        modelError = modelMissingMessage(modelPath)
                    )
                }
            }
        }
    }

    private fun ensureDefaultSettings() {
        viewModelScope.launch {
            if (container.repository.getSetting(ModelConfig.KEY_MODEL_DIRECTORY) == null) {
                container.repository.upsertSetting(
                    ModelConfig.KEY_MODEL_DIRECTORY,
                    container.defaultModelDirectoryPath
                )
            }
            if (container.repository.getSetting(ModelConfig.KEY_ACTIVE_RUNTIME) == null) {
                container.repository.upsertSetting(
                    ModelConfig.KEY_ACTIVE_RUNTIME,
                    ModelConfig.DEFAULT_RUNTIME
                )
            }
        }
    }

    private fun refreshBriefingAndNudges(tasks: List<TaskEntity>) {
        val briefing = container.coordinator.generateMorningBriefing(tasks)
        val nudges = container.coordinator.generateProactiveNudges(tasks)
        _uiState.update {
            it.copy(
                morningBriefing = briefing,
                proactiveNudges = nudges
            )
        }
    }

    private fun buildRecommendationContext(tasks: List<TaskEntity>): String {
        val today = LocalDate.now()
        val activeGoals = _uiState.value.goals
            .filter { it.status == GoalStatus.ACTIVE }
            .take(3)
            .joinToString("; ") { goal ->
                val daysLeft = goal.deadlineEpochDay - today.toEpochDay()
                val deadlineText = when {
                    daysLeft < 0 -> "missed deadline ${-daysLeft} day(s) ago"
                    daysLeft == 0L -> "deadline today"
                    daysLeft == 1L -> "deadline tomorrow"
                    else -> "deadline in $daysLeft days"
                }
                "${goal.title}, $deadlineText, reason: ${goal.why.ifBlank { "not specified" }}"
            }
            .ifBlank { "No active goals are recorded yet." }

        val activeTasks = tasks
            .filter { it.status != TaskStatus.COMPLETED }
            .sortedWith(
                compareByDescending<TaskEntity> { it.status == TaskStatus.BLOCKED }
                    .thenBy { it.deadlineEpochDay ?: Long.MAX_VALUE }
                    .thenByDescending { it.priority }
            )
            .take(8)
            .joinToString("\n") { task ->
                val dueText = task.deadlineEpochDay?.let { deadline ->
                    val daysLeft = deadline - today.toEpochDay()
                    when {
                        daysLeft < 0 -> "overdue by ${-daysLeft} day(s)"
                        daysLeft == 0L -> "due today"
                        daysLeft == 1L -> "due tomorrow"
                        else -> "due in $daysLeft days"
                    }
                } ?: "no clear deadline"

                val blockerText = task.blockedReason?.let { " It is blocked because $it." }.orEmpty()
                val detailText = task.details
                    .takeIf { it.isNotBlank() && !it.equals("Auto-seeded sample task", ignoreCase = true) }
                    ?.let { " Context: $it." }
                    .orEmpty()

                "- ${task.title}: ${task.status.name.lowercase().replace('_', ' ')}, $dueText, priority ${task.priority}/10, owner ${task.owner}.$blockerText$detailText"
            }
            .ifBlank { "There are no open tasks." }

        return """
            Today is $today.
            You are acting as the user's private executive assistant.
            Active goals: $activeGoals
            Current work:
            $activeTasks
            Give the user a direct recommendation with a reason and a concrete next move.
        """.trimIndent()
    }

    private fun modelMissingMessage(path: String = _uiState.value.modelPath): String? {
        val modelDirectory = path.ifBlank { container.defaultModelDirectoryPath }
        val root = File(modelDirectory)
        val hasModel = when {
            root.isFile && root.extension.equals("gguf", ignoreCase = true) -> true
            !root.exists() || !root.isDirectory -> false
            File(root, "qwen/qwen.gguf").exists() -> true
            else -> root.walkTopDown()
                .maxDepth(4)
                .any { it.isFile && it.extension.equals("gguf", ignoreCase = true) }
        }

        return if (hasModel) {
            null
        } else {
            "Local model missing. Put qwen.gguf at $modelDirectory/qwen/qwen.gguf before expecting LLM analysis."
        }
    }

    private fun postMessage(text: String) {
        _uiState.update { it.copy(message = text) }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssistantViewModel(container) as T
        }
    }
}
