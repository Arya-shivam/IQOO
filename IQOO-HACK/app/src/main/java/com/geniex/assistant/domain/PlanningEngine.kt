package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.GoalInput
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class PlanningEngine {

    fun generateInitialPlan(goalId: Long, goalInput: GoalInput, nowMs: Long): List<TaskEntity> {
        val normalizedTitle = goalInput.title.lowercase()
        val taskTitles = when {
            normalizedTitle.contains("hackathon") -> listOf(
                "Understand project scope",
                "Set up local Qwen pipeline",
                "Build memory schema with SQLite",
                "Implement agent tools and planner",
                "Run offline demo rehearsal"
            )

            normalizedTitle.contains("dsa") -> listOf(
                "Arrays and Strings revision",
                "Hashing and Two Pointers practice",
                "Binary Search problem set",
                "Trees and Graphs fundamentals",
                "Mock interview and revision"
            )

            else -> listOf(
                "Define milestones",
                "Set first deliverable",
                "Build execution checklist",
                "Review dependencies",
                "Daily progress review"
            )
        }

        val totalDays = (goalInput.deadline.toEpochDay() - LocalDate.now().toEpochDay()).coerceAtLeast(1)
        val spacing = (totalDays / taskTitles.size).coerceAtLeast(1)

        return taskTitles.mapIndexed { index, title ->
            val dueDate = LocalDate.now().plusDays((index * spacing).toLong()).coerceAtMost(goalInput.deadline)
            TaskEntity(
                goalId = goalId,
                title = title,
                details = "Auto-generated from goal planning engine",
                status = TaskStatus.PENDING,
                priority = (10 - index).coerceAtLeast(5),
                owner = "You",
                deadlineEpochDay = dueDate.toEpochDay(),
                dependencyTaskId = null,
                blockedReason = null,
                estimatedMinutes = 60,
                createdAtEpochMs = nowMs,
                updatedAtEpochMs = nowMs
            )
        }
    }
}

private fun LocalDate.coerceAtMost(other: LocalDate): LocalDate {
    return if (isAfter(other)) other else this
}
