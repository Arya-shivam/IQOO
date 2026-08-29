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
                message = "Good morning. You are clear for today. Consider learning or recovery time.",
                topTasks = emptyList()
            )
        }

        val top = openTasks
            .sortedByDescending { importanceScorer.score(it, today) }
            .take(3)

        val bullets = top.map { task ->
            val due = task.deadlineEpochDay?.let { d ->
                val days = d - today.toEpochDay()
                when {
                    days <= 0 -> "due today"
                    days == 1L -> "due tomorrow"
                    else -> "due in $days days"
                }
            } ?: "no deadline"
            "${task.title} ($due)"
        }

        val message = buildString {
            append("Good morning. You have ")
            append(top.size)
            append(" key focus item")
            if (top.size > 1) append("s")
            append(" today. Start with ")
            append(top.first().title)
            append(".")
        }

        return Briefing(message = message, topTasks = bullets)
    }
}
