package com.opengranola.android.domain

import com.opengranola.android.calendar.CalendarSnapshot
import com.opengranola.android.data.CommitmentEntity
import com.opengranola.android.data.PlanTaskEntity
import com.opengranola.android.usage.UsageSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class PriorityBriefing(
    val headline: String,
    val timetable: List<PrioritySlot>,
    val nudges: List<String>
)

data class PrioritySlot(
    val window: String,
    val title: String,
    val reason: String,
    val source: String,
    val score: Int
)

private data class PriorityCandidate(
    val title: String,
    val details: String,
    val source: String,
    val score: Int,
    val dueText: String = ""
)

/**
 * Deterministic assistant layer inspired by IQOO-HACK's scheduling engines.
 * It gives pa useful guidance even before a local model is loaded.
 */
class PriorityEngine {
    fun build(
        planTasks: List<PlanTaskEntity>,
        commitments: List<CommitmentEntity>,
        calendar: CalendarSnapshot,
        usage: UsageSnapshot,
        notificationsToday: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): PriorityBriefing {
        val candidates = buildCandidates(planTasks, commitments)
            .sortedWith(compareByDescending<PriorityCandidate> { it.score }.thenBy { it.title })

        val timetable = candidates.take(5).mapIndexed { index, candidate ->
            PrioritySlot(
                window = timeWindowFor(candidate, index),
                title = candidate.title,
                reason = reasonFor(candidate),
                source = candidate.source,
                score = candidate.score
            )
        }

        val headline = when {
            timetable.isEmpty() -> "No open commitments or plan steps. Capture a meeting or create a plan to start the loop."
            calendar.events.any { it.startsAt in nowMillis..(nowMillis + TWO_HOURS_MS) } ->
                "Protect the first priority before your next calendar event."
            notificationsToday >= HIGH_NOTIFICATION_COUNT ->
                "Notifications are noisy today. Pick one priority and keep it visible."
            usage.hasPermission && usage.apps.firstOrNull()?.minutes.orZero() >= HIGH_USAGE_MINUTES ->
                "Your attention has a clear pull today. Anchor it to the top commitment."
            else -> "Start with ${timetable.first().title}."
        }

        return PriorityBriefing(
            headline = headline,
            timetable = timetable,
            nudges = proactiveNudges(timetable, calendar, usage, notificationsToday, nowMillis)
        )
    }

    private fun buildCandidates(
        planTasks: List<PlanTaskEntity>,
        commitments: List<CommitmentEntity>
    ): List<PriorityCandidate> {
        val taskCandidates = planTasks
            .filterNot { it.status.equals("done", ignoreCase = true) }
            .map { task ->
                val text = "${task.title} ${task.details}"
                PriorityCandidate(
                    title = task.title,
                    details = task.details,
                    source = "Plan step",
                    score = taskScore(task, text)
                )
            }

        val commitmentCandidates = commitments
            .filter { it.status.equals("open", ignoreCase = true) }
            .map { commitment ->
                val text = "${commitment.title} ${commitment.owner} ${commitment.dueText} ${commitment.evidence}"
                PriorityCandidate(
                    title = commitment.title,
                    details = text,
                    source = if (commitment.sourceTitle.isBlank()) "Commitment" else "Commitment from ${commitment.sourceTitle}",
                    score = commitmentScore(commitment, text),
                    dueText = commitment.dueText
                )
            }

        return taskCandidates + commitmentCandidates
    }

    private fun taskScore(task: PlanTaskEntity, text: String): Int {
        val normalizedPriority = (8 - task.priority.coerceIn(1, 7)) * 8
        return normalizedPriority +
            impactScore(text) +
            dueSignalScore(text) +
            if (task.details.isBlank()) 0 else 6
    }

    private fun commitmentScore(commitment: CommitmentEntity, text: String): Int {
        return 42 +
            (commitment.confidence.coerceIn(0f, 1f) * 20).roundToInt() +
            dueSignalScore(text) +
            impactScore(text) +
            if (commitment.evidence.isBlank()) 0 else 8
    }

    private fun timeWindowFor(candidate: PriorityCandidate, index: Int): String {
        val text = candidate.details.lowercase()
        return when {
            hasAny(text, "blocked", "waiting", "approval", "dependency", "depends") -> "First available moment"
            hasAny(text, "review", "test", "validate", "check", "qa") -> "Evening review"
            hasAny(text, "send", "call", "follow", "message", "coordinate", "reply") -> "Afternoon"
            hasAny(text, "learn", "study", "research", "plan", "think") -> "Morning"
            hasAny(text, "build", "write", "implement", "design", "prototype") -> if (index == 0) "Morning deep work" else "Late morning"
            else -> DEFAULT_WINDOWS.getOrElse(index) { "Later" }
        }
    }

    private fun reasonFor(candidate: PriorityCandidate): String {
        val text = "${candidate.title} ${candidate.details} ${candidate.dueText}".lowercase()
        return when {
            hasAny(text, "overdue", "yesterday") -> "it looks overdue, so it should be handled before new work"
            hasAny(text, "today", "eod", "end of day") -> "it appears due today"
            hasAny(text, "tomorrow") -> "the deadline is close enough to reduce risk now"
            hasAny(text, "blocked", "waiting", "depends", "dependency") -> "clearing it can unblock the rest of the plan"
            hasAny(text, "client", "demo", "launch", "production", "payment", "security") -> "it has high impact signals"
            candidate.source.startsWith("Commitment") -> "it came from an explicit meeting commitment"
            else -> "it has the strongest priority signal in the current plan"
        }
    }

    private fun proactiveNudges(
        timetable: List<PrioritySlot>,
        calendar: CalendarSnapshot,
        usage: UsageSnapshot,
        notificationsToday: Int,
        nowMillis: Long
    ): List<String> {
        val nudges = mutableListOf<String>()
        val next = timetable.firstOrNull()
        if (next != null) {
            nudges += "Start with ${next.title}; ${next.reason}."
        }
        calendar.events.firstOrNull { it.startsAt in nowMillis..(nowMillis + TWO_HOURS_MS) }?.let { event ->
            nudges += "Upcoming: ${event.title} at ${timeLabel(event.startsAt)}. Keep the next work block realistic."
        }
        usage.apps.firstOrNull()?.takeIf { usage.hasPermission && it.minutes >= HIGH_USAGE_MINUTES }?.let { app ->
            nudges += "${app.label} has taken ${app.minutes} minutes today. Check whether that matches your top priority."
        }
        if (notificationsToday >= HIGH_NOTIFICATION_COUNT) {
            nudges += "$notificationsToday alerts landed today. Batch replies after the first priority block."
        }
        return nudges.distinct().take(4)
    }

    private fun dueSignalScore(text: String): Int {
        val lower = text.lowercase()
        return when {
            hasAny(lower, "overdue", "yesterday") -> 36
            hasAny(lower, "today", "eod", "end of day") -> 32
            hasAny(lower, "tomorrow") -> 24
            hasAny(lower, "this week", "friday", "monday", "tuesday", "wednesday", "thursday") -> 16
            else -> 0
        }
    }

    private fun impactScore(text: String): Int {
        val lower = text.lowercase()
        return when {
            hasAny(lower, "security", "production", "payment", "client", "launch", "demo") -> 18
            hasAny(lower, "blocked", "waiting", "dependency", "approval") -> 14
            hasAny(lower, "important", "urgent", "critical") -> 12
            else -> 0
        }
    }

    private fun timeLabel(epochMs: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMs))

    private fun hasAny(text: String, vararg signals: String): Boolean =
        signals.any { it in text }

    private fun Int?.orZero(): Int = this ?: 0

    private companion object {
        const val TWO_HOURS_MS = 2L * 60L * 60L * 1000L
        const val HIGH_NOTIFICATION_COUNT = 12
        const val HIGH_USAGE_MINUTES = 45
        val DEFAULT_WINDOWS = listOf("Morning deep work", "Late morning", "Afternoon", "Evening review", "Later")
    }
}
