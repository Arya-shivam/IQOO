package com.geniex.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geniex.assistant.AppContainer
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.llm.ModelConfig
import com.geniex.assistant.model.GoalInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            val context = _uiState.value.tasks.joinToString("; ") { task ->
                "${task.title} status=${task.status} deadline=${task.deadlineEpochDay}"
            }
            val recommendation = container.coordinator.runtimeRecommendation(context)
            _uiState.update { it.copy(recommendation = recommendation) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun observeData() {
        viewModelScope.launch {
            container.repository.observeGoals().collectLatest { goals ->
                _uiState.update { it.copy(goals = goals) }
            }
        }
        viewModelScope.launch {
            container.repository.observeTasks().collectLatest { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
                refreshBriefingAndNudges(tasks)
            }
        }
        viewModelScope.launch {
            container.repository.observeMeetings().collectLatest { meetings ->
                _uiState.update { it.copy(meetings = meetings) }
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
                        runtimeName = runtimeName
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
