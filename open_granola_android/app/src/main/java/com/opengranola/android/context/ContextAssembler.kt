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
        val messages = assistant.recentMessages(DEFAULT_SESSION, 8).reversed()
        // Meeting memory is intentionally summary-only. Raw transcripts never
        // enter general chat context, and clearing a summary removes that
        // meeting from future retrieval without deleting its transcript.
        val meetings = database.meetingDao().recent(12).filter { it.notes.isNotBlank() }.take(4)
        val notifications = database.notificationDao().recent(8)
        val events = assistant.recentEvents(16).filter { it.type == "usage" }.take(8)
        val sourceIds = buildList {
            addAll(memories.map { it.id })
            addAll(plans.map { it.id })
            addAll(meetings.map { it.id })
            addAll(notifications.map { it.id })
            addAll(events.map { it.id })
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
            if (meetings.isNotEmpty()) {
                appendLine("\nMeeting memory (saved summaries only):")
                meetings.forEach { appendLine("- ${it.title}: ${it.notes.take(700)}") }
            }
            if (notifications.isNotEmpty()) {
                appendLine("\nRecent redacted notification signals:")
                notifications.forEach { appendLine("- ${it.appLabel}: ${it.title}. ${it.body.take(220)}") }
            }
            if (events.isNotEmpty()) {
                appendLine("\nRecent app usage memory:")
                events.forEach { appendLine("- ${it.source}/${it.type}: ${it.title}. ${it.content.take(240)}") }
            }
            if (messages.isNotEmpty()) {
                appendLine("\nRecent conversation:")
                messages.forEach { appendLine("${it.role}: ${it.content.take(500)}") }
            }
        }.take(MAX_CONTEXT_CHARS)
        val snapshotId = UUID.randomUUID().toString()
        assistant.saveSnapshot(
            ContextSnapshotEntity(snapshotId, purpose, rendered, sourceIds.joinToString(","), System.currentTimeMillis())
        )
        return AssistantContext(rendered, snapshotId, sourceIds)
    }

    companion object {
        const val DEFAULT_SESSION = "default"
        private const val MAX_CONTEXT_CHARS = 7_000
    }
}
