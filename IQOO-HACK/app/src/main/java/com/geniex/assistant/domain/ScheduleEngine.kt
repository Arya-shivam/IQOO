package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class ScheduleEngine(private val importanceScorer: ImportanceScorer) {

    fun prioritize(tasks: List<TaskEntity>, today: LocalDate): List<TaskEntity> {
        return tasks
            .filter { it.status != TaskStatus.COMPLETED }
            .sortedWith(
                compareByDescending<TaskEntity> { rankScore(it, today) }
                    .thenBy { it.deadlineEpochDay ?: Long.MAX_VALUE }
                    .thenBy { it.createdAtEpochMs }
            )
    }

    fun buildTimetable(tasks: List<TaskEntity>, today: LocalDate): List<String> {
        return tasks.take(5).mapIndexed { index, task ->
            val window = timeWindowFor(task, index)
            val reason = reasonFor(task, today)
            val action = if (task.status == TaskStatus.BLOCKED) "Unblock ${task.title}" else task.title
            "$window: $action - $reason"
        }
    }

    private fun timeWindowFor(task: TaskEntity, index: Int): String {
        val text = "${task.title} ${task.details}".lowercase()
        return when {
            task.status == TaskStatus.BLOCKED -> "First available moment"
            containsAny(text, "learn", "understand", "study", "read", "research", "plan") ->
                "Morning"
            containsAny(text, "build", "implement", "write", "code", "design", "architecture") ->
                if (index == 0) "Morning deep work" else "Late morning"
            containsAny(text, "follow", "sync", "coordinate", "send", "call", "message", "procurement") ->
                "Afternoon"
            containsAny(text, "review", "test", "rehearsal", "validate", "qa", "check") ->
                "Evening review"
            else -> when (index) {
                0 -> "Morning deep work"
                1 -> "Late morning"
                2 -> "Afternoon"
                3 -> "Evening review"
                else -> "Later"
            }
        }
    }

    private fun reasonFor(task: TaskEntity, today: LocalDate): String {
        if (task.status == TaskStatus.BLOCKED) {
            return if (!task.blockedReason.isNullOrBlank()) {
                "it is blocked by ${task.blockedReason}, and clearing that unlocks the rest of the plan"
            } else {
                "it is blocked, so identify and clear the dependency before doing more execution work"
            }
        }

        val dueReason = task.deadlineEpochDay?.let { deadline ->
            val daysLeft = deadline - today.toEpochDay()
            when {
                daysLeft < 0 -> "it is already overdue, so it deserves immediate attention"
                daysLeft == 0L -> "it is due today, so delaying it creates risk"
                daysLeft == 1L -> "it is due tomorrow, so today is the right time to reduce pressure"
                daysLeft <= 3L -> "the deadline is close, so I would protect time for it now"
                else -> null
            }
        }

        if (dueReason != null) return dueReason
        val text = "${task.title} ${task.details}".lowercase()
        if (hasDependencySignal(text)) return "it is connected to another workstream, so resolving it reduces downstream risk"
        if (hasImpactSignal(text)) return "it has higher business or demo impact than the rest of the list"
        if (task.priority >= 9) return "it has the strongest priority signal in your current plan"
        if (isLearningWork(text)) return "morning is better for learning and thinking work while your mind is fresh"
        if (task.details.isNotBlank()) return task.details.trimEnd('.')
        return "it moves the plan forward without adding unnecessary context switching"
    }

    private fun rankScore(task: TaskEntity, today: LocalDate): Int {
        val text = "${task.title} ${task.details} ${task.blockedReason.orEmpty()}".lowercase()
        val deadlineScore = task.deadlineEpochDay?.let { deadline ->
            val daysLeft = deadline - today.toEpochDay()
            when {
                daysLeft < 0 -> 45
                daysLeft == 0L -> 40
                daysLeft == 1L -> 34
                daysLeft <= 3L -> 26
                daysLeft <= 7L -> 16
                else -> 6
            }
        } ?: 4

        val blockerScore = when {
            task.status == TaskStatus.BLOCKED -> 30
            hasDependencySignal(text) -> 14
            else -> 0
        }
        val impactScore = if (hasImpactSignal(text)) 14 else 0
        val momentumScore = if (task.status == TaskStatus.IN_PROGRESS) 8 else 0
        val effortScore = if (task.estimatedMinutes <= 30) 4 else 0

        return importanceScorer.score(task, today) * 3 +
            deadlineScore +
            blockerScore +
            impactScore +
            momentumScore +
            effortScore
    }

    private fun hasDependencySignal(text: String): Boolean {
        return containsAny(text, "blocked", "depends", "waiting", "credentials", "approval", "dependency", "unblock")
    }

    private fun hasImpactSignal(text: String): Boolean {
        return containsAny(text, "client", "demo", "investor", "revenue", "payment", "production", "security", "launch")
    }

    private fun isLearningWork(text: String): Boolean {
        return containsAny(text, "learn", "understand", "study", "research", "read")
    }

    private fun containsAny(text: String, vararg needles: String): Boolean {
        return needles.any { it in text }
    }
}
