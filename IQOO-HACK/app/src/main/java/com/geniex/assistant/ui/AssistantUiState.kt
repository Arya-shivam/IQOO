package com.geniex.assistant.ui

import com.geniex.assistant.data.db.GoalEntity
import com.geniex.assistant.data.db.MeetingEntity
import com.geniex.assistant.data.db.MemoryEntity
import com.geniex.assistant.data.db.TaskEntity

data class AssistantUiState(
    val goals: List<GoalEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val meetings: List<MeetingEntity> = emptyList(),
    val memories: List<MemoryEntity> = emptyList(),
    val morningBriefing: String = "No briefing yet.",
    val proactiveNudges: List<String> = emptyList(),
    val modelPath: String = "",
    val runtimeName: String = "",
    val recommendation: String = "",
    val timetable: List<String> = emptyList(),
    val assistantSummary: String = "",
    val modelError: String? = null,
    val loading: Boolean = false,
    val message: String? = null
)
