package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class ImportanceScorer {
    fun score(task: TaskEntity, today: LocalDate): Int {
        val urgency = task.deadlineEpochDay?.let {
            val daysLeft = it - today.toEpochDay()
            when {
                daysLeft < 0 -> 30
                daysLeft == 0L -> 28
                daysLeft == 1L -> 24
                daysLeft <= 3 -> 18
                daysLeft <= 7 -> 10
                else -> 3
            }
        } ?: 0

        val statusWeight = when (task.status) {
            TaskStatus.BLOCKED -> 12
            TaskStatus.COMPLETED -> -100
            TaskStatus.IN_PROGRESS -> 8
            TaskStatus.PENDING -> 0
        }

        val explicitPriority = task.priority.coerceIn(0, 10) * 5
        return (explicitPriority + urgency + statusWeight).coerceIn(0, 100)
    }
}
