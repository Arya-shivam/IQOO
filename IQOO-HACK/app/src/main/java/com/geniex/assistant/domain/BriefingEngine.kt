package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.Briefing
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class BriefingEngine(private val importanceScorer: ImportanceScorer) {

    fun generateMorningBriefing(tasks: List<TaskEntity>, today: LocalDate): Briefing {
        val openTasks = tasks.filter { it.status != TaskStatus.COMPLETED }
        if (openTasks.isEmpty()) {
            return Briefing(
                message = "Good morning. Your plate is clear right now. I would use this window for planning, learning, or recovery before something urgent appears.",
                topTasks = emptyList()
            )
        }

        val top = openTasks
            .sortedByDescending { importanceScorer.score(it, today) }
            .take(3)

        val taskReads = top.map { task ->
            val due = task.deadlineEpochDay?.let { d ->
                val days = d - today.toEpochDay()
                when {
                    days < 0 -> "already overdue"
                    days <= 0 -> "due today"
                    days == 1L -> "due tomorrow"
                    else -> "due in $days days"
                }
            } ?: "no deadline"
            val blocker = task.blockedReason?.let { " It is blocked because $it, so I would clear that first." }.orEmpty()
            "${task.title} is $due.$blocker"
        }

        val message = buildString {
            val first = top.first()
            append("Good morning. I'd start with ")
            append(first.title)
            append(" today")
            first.deadlineEpochDay?.let { deadline ->
                val days = deadline - today.toEpochDay()
                append(
                    when {
                        days < 0 -> " because it is already overdue"
                        days == 0L -> " because it is due today"
                        days == 1L -> " because it is due tomorrow"
                        days <= 3L -> " because the deadline is close"
                        else -> " because it has the strongest priority signal"
                    }
                )
            } ?: append(" because it has the strongest priority signal")
            append(".")
            if (top.size > 1) {
                append(" After that, I would keep ")
                append(top.drop(1).joinToString(" and ") { it.title })
                append(" in view, but not let them distract from the first move.")
            }
        }

        return Briefing(message = message, topTasks = taskReads)
    }
}
