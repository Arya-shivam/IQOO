package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class ImportanceScorer {
    fun score(task: TaskEntity, today: LocalDate): Int {
        val urgency = task.deadlineEpochDay?.let {
            val daysLeft = it - today.toEpochDay()
            when {
                daysLeft <= 0 -> 5
                daysLeft <= 1 -> 4
                daysLeft <= 3 -> 3
                daysLeft <= 7 -> 2
                else -> 1
            }
        } ?: 1

        val statusPenalty = when (task.status) {
            TaskStatus.BLOCKED -> -1
            TaskStatus.COMPLETED -> -5
            TaskStatus.IN_PROGRESS -> 2
            TaskStatus.PENDING -> 1
        }

        val dependencyWeight = if (task.dependencyTaskId != null) 2 else 0
        return (task.priority + urgency + statusPenalty + dependencyWeight).coerceIn(0, 10)
    }
}
