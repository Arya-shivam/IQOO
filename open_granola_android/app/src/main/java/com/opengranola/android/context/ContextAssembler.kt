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
        val memories = assistant.relevantMemories(8)
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
        val includeConversation = !purpose.contains("interactive chat", ignoreCase = true)
        val sourceIds = buildList {
            addAll(memories.map { it.id })
            addAll(plans.map { it.id })
            addAll(commitments.map { it.id })
            addAll(meetings.map { it.id })
            addAll(notifications.map { it.id })
            addAll(usageEvents.map { it.id })
            addAll(calendarEvents.map { it.id })
        }
        val rendered = buildString {
            appendLine("Purpose: $purpose")
            if (userName.isNotBlank()) appendLine("User profile: The user's preferred name is ${userName.take(60)}.")
            if (extra.isNotBlank()) appendLine("Current input: ${extra.take(1200)}")
            if (memories.isNotEmpty()) {
                appendLine("\nImportant memories:")
                memories.forEach { appendLine("- ${it.text.take(500)}") }
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
                notifications.forEach { appendLine("- ${it.appLabel}: ${it.title}. ${it.body.take(220)}") }
            }
            if (calendarEvents.isNotEmpty()) {
                appendLine("\nUpcoming local calendar events and reminders:")
                calendarEvents.forEach { appendLine("- ${it.title}: ${it.content.take(300)}") }
            }
            if (usageEvents.isNotEmpty()) {
                appendLine("\nRecent app usage memory:")
                usageEvents.forEach { appendLine("- ${it.source}/${it.type}: ${it.title}. ${it.content.take(240)}") }
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
    }
}
