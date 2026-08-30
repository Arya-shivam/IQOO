package com.geniex.assistant.model

import java.time.LocalDate

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    PAUSED
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED
}

enum class MemoryType {
    LONG_TERM,
    EPISODIC,
    DECISION,
    COMMITMENT,
    MEETING
}

data class GoalInput(
    val title: String,
    val why: String,
    val deadline: LocalDate
)

data class MeetingExtraction(
    val summary: String,
    val extractedTasks: List<ExtractedTask>,
    val extractedCommitments: List<String>,
    val extractedDecisions: List<String> = emptyList()
)

data class ExtractedTask(
    val title: String,
    val owner: String,
    val deadline: LocalDate?,
    val dependencyNote: String?,
    val priority: Int = 8
)

data class Briefing(
    val message: String,
    val topTasks: List<String>
)

data class AssistantAnalysis(
    val timetable: List<String>,
    val summary: String
)
