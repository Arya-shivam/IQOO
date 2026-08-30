package com.opengranola.android.context

import com.opengranola.android.data.ContextSnapshotEntity
import com.opengranola.android.data.OpenGranolaDatabase
import java.util.UUID

data class AssistantContext(
    val text: String,
    val snapshotId: String,
    val sourceIds: List<String>
)

/** Builds a bounded, inspectable prompt context from local-only sources. */
class ContextAssembler(private val database: OpenGranolaDatabase) {
    suspend fun build(purpose: String, extra: String = "", userName: String = ""): AssistantContext {
        val assistant = database.assistantDao()
        val now = System.currentTimeMillis()
        val weekAgo = now - 7L * 24 * 60 * 60 * 1000
        val memories = assistant.relevantMemories(8)
        val goals = assistant.activeGoals(8)
        val actions = assistant.recentActions(24).filter { it.occurredAt >= weekAgo }
        val graphNodes = assistant.graphNodes(400)
        val graphEdges = assistant.graphEdges(800)
        val queryTerms = extra.lowercase().split(Regex("[^a-z0-9+#.]+"))
            .filter { it.length > 2 && it !in STOP_WORDS }.distinct()
        val matchedNodes = if (queryTerms.isEmpty()) emptyList() else graphNodes
            .map { node ->
                val title = node.title.lowercase()
                val details = node.details.lowercase()
                val tags = node.tags.lowercase()
                node to queryTerms.sumOf { term -> (if (term in title) 4 else 0) + (if (term in tags) 2 else 0) + (if (term in details) 1 else 0) }
            }
            .filter { it.second > 0 }.sortedByDescending { it.second }.take(14).map { it.first }
        val matchedIds = matchedNodes.mapTo(mutableSetOf()) { it.id }
        val matchedEdges = graphEdges.filter { it.fromId in matchedIds || it.toId in matchedIds }.take(40)
        val neighborhoodIds = matchedEdges.flatMapTo(matchedIds) { listOf(it.fromId, it.toId) }
        val neighborhood = graphNodes.filter { it.id in neighborhoodIds }.take(28)
        val plans = assistant.activePlans(3)
        val tasks = if (plans.isEmpty()) emptyList() else assistant.activeTasks(plans.map { it.id })
        val commitments = assistant.activeCommitments(8)
        val messages = assistant.recentMessages(DEFAULT_SESSION, 8).reversed()
        // Meeting memory is intentionally summary-only. Raw transcripts never
        // enter general chat context, and clearing a summary removes that
        // meeting from future retrieval without deleting its transcript.
        val meetings = database.meetingDao().recent(12).filter { it.notes.isNotBlank() }.take(4)
        val notifications = database.notificationDao().recent(8)
        val recentEvents = assistant.recentEvents(40)
        val usageEvents = recentEvents.filter { it.type == "usage" }.take(8)
        val calendarEvents = recentEvents.filter { it.type == "calendar" }.sortedBy { it.timestamp }.take(12)
        val frontierSafe = purpose.contains("interactive chat", ignoreCase = true) ||
            purpose.contains("plan", ignoreCase = true) || purpose.contains("briefing", ignoreCase = true)
        val includeConversation = !purpose.contains("interactive chat", ignoreCase = true)
        val sourceIds = buildList {
            addAll(memories.map { it.id })
            addAll(plans.map { it.id })
            addAll(commitments.map { it.id })
            addAll(meetings.map { it.id })
            addAll(notifications.map { it.id })
            addAll(usageEvents.map { it.id })
            addAll(calendarEvents.map { it.id })
            addAll(goals.map { it.id })
            addAll(actions.map { it.id })
            addAll(neighborhood.map { it.id })
        }
        val rendered = buildString {
            appendLine("Purpose: $purpose")
            if (userName.isNotBlank()) appendLine("User profile: The user's preferred name is ${userName.take(60)}.")
            if (extra.isNotBlank()) appendLine("Current input: ${extra.take(1200)}")
            if (neighborhood.isNotEmpty()) {
                val labels = neighborhood.associateBy { it.id }
                appendLine("\nRelevant memory graph for the current input:")
                neighborhood.forEach { appendLine("- ${it.type.uppercase()} ${it.title}: ${it.details.take(360)} [status=${it.status}]") }
                appendLine("Relationships:")
                matchedEdges.forEach { relation ->
                    appendLine("- ${labels[relation.fromId]?.title ?: relation.fromId} --${relation.type}--> ${labels[relation.toId]?.title ?: relation.toId}; evidence=${relation.evidence.take(180)}")
                }
            }
            if (memories.isNotEmpty()) {
                appendLine("\nImportant memories:")
                memories.forEach { appendLine("- ${it.text.take(500)}") }
            }
            if (goals.isNotEmpty()) {
                appendLine("\nActive goals:")
                goals.forEach { goal -> appendLine("- ${goal.title}: ${goal.description}") }
            }
            if (actions.isNotEmpty()) {
                appendLine("\nCurated actions from the last seven days:")
                actions.forEach { action -> appendLine("- [${action.linkStatus}] ${action.title}: ${action.summary.take(500)}") }
            }
            if (goals.isNotEmpty()) {
                appendLine("\nComputed patterns:")
                goals.forEach { goal ->
                    val current = assistant.actionsForGoal(goal.id, weekAgo)
                    val previous = assistant.actionsForGoal(goal.id, weekAgo - 7L * 24 * 60 * 60 * 1000)
                        .count { it.occurredAt < weekAgo }
                    appendLine("- ${goal.title}: ${current.size} actions this week vs $previous last week; ${if (current.isEmpty()) "stale" else "active"}")
                }
            }
            if (plans.isNotEmpty()) {
                appendLine("\nActive plans:")
                plans.forEach { plan ->
                    appendLine("- ${plan.title}: ${plan.objective}")
                    tasks.filter { it.planId == plan.id }.take(6).forEach { appendLine("  - [${it.status}] ${it.title}: ${it.details}") }
                }
            }
            if (commitments.isNotEmpty()) {
                appendLine("\nOpen commitments:")
                commitments.forEach {
                    appendLine("- ${it.title}; owner=${it.owner.ifBlank { "unknown" }}; due=${it.dueText.ifBlank { "not stated" }}; source=${it.sourceTitle}")
                }
            }
            if (meetings.isNotEmpty()) {
                appendLine("\nMeeting memory (saved summaries only):")
                meetings.forEach { appendLine("- ${it.title}: ${it.notes.take(700)}") }
            }
            if (notifications.isNotEmpty()) {
                appendLine("\nRecent redacted notification signals:")
                notifications.forEach {
                    appendLine(if (frontierSafe) "- ${it.appLabel}: ${it.title}" else "- ${it.appLabel}: ${it.title}. ${it.body.take(220)}")
                }
            }
            if (calendarEvents.isNotEmpty()) {
                appendLine("\nUpcoming local calendar events and reminders:")
                calendarEvents.forEach { appendLine("- ${it.title}: ${it.content.take(300)}") }
            }
            if (usageEvents.isNotEmpty()) {
                appendLine("\nRecent app usage memory:")
                usageEvents.forEach {
                    appendLine(if (frontierSafe) "- ${it.title}: usage signal available; use only as a high-level pattern." else "- ${it.source}/${it.type}: ${it.title}. ${it.content.take(240)}")
                }
            }
            if (includeConversation && messages.isNotEmpty()) {
                appendLine("\nRecent conversation:")
                messages.forEach { appendLine("${it.role}: ${it.content.take(500)}") }
            }
        }.take(if (purpose.contains("interactive chat", ignoreCase = true)) CHAT_CONTEXT_CHARS else MAX_CONTEXT_CHARS)
        val snapshotId = UUID.randomUUID().toString()
        assistant.saveSnapshot(
            ContextSnapshotEntity(snapshotId, purpose, rendered, sourceIds.joinToString(","), System.currentTimeMillis())
        )
        return AssistantContext(rendered, snapshotId, sourceIds)
    }

    companion object {
        const val DEFAULT_SESSION = "default"
        private const val CHAT_CONTEXT_CHARS = 3_600
        private const val MAX_CONTEXT_CHARS = 7_000
        private val STOP_WORDS = setOf("what", "when", "where", "which", "with", "that", "this", "from", "about", "have", "does", "should", "could", "would", "please", "tell", "show")
    }
}
