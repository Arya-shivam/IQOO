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
                val daysOverdue = today.toEpochDay() - deadline
                nudges += "I would not leave ${task.title} open any longer. It is overdue by $daysOverdue day(s), so either finish it now or deliberately move the plan."
            }
            if (task.status == TaskStatus.BLOCKED && !task.blockedReason.isNullOrBlank()) {
                nudges += "${task.title} is blocked because ${task.blockedReason}. I would clear that dependency before spending time on lower-impact work."
            }
        }

        return nudges.take(3)
    }
}
