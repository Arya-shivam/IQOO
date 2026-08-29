package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class ProactiveEngine {
    fun generateNudges(tasks: List<TaskEntity>, today: LocalDate): List<String> {
        val nudges = mutableListOf<String>()

        tasks.filter { it.status != TaskStatus.COMPLETED }.forEach { task ->
            val deadline = task.deadlineEpochDay
            if (deadline != null && deadline < today.toEpochDay()) {
                nudges += "${task.title} is overdue. Re-plan this now."
            }
            if (task.status == TaskStatus.BLOCKED && !task.blockedReason.isNullOrBlank()) {
                nudges += "${task.title} is blocked: ${task.blockedReason}."
            }
        }

        return nudges.take(3)
    }
}
