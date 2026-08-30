package com.geniex.assistant.domain

import com.geniex.assistant.data.db.GoalEntity
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.data.repo.AssistantRepository
import com.geniex.assistant.llm.LocalModelBridge
import com.geniex.assistant.model.AssistantAnalysis
import com.geniex.assistant.model.GoalInput
import com.geniex.assistant.model.GoalStatus
import com.geniex.assistant.model.MemoryType
import com.geniex.assistant.model.TaskStatus
import java.time.LocalDate

class AssistantCoordinator(
    private val repository: AssistantRepository,
    private val planningEngine: PlanningEngine,
    private val briefingEngine: BriefingEngine,
    private val proactiveEngine: ProactiveEngine,
    private val scheduleEngine: ScheduleEngine,
    private val localModelBridge: LocalModelBridge
) {

    suspend fun createGoalAndPlan(input: GoalInput) {
        val nowMs = System.currentTimeMillis()
        val goalId = repository.createGoal(
            GoalEntity(
                title = input.title,
                why = input.why,
                deadlineEpochDay = input.deadline.toEpochDay(),
                status = GoalStatus.ACTIVE,
                createdAtEpochMs = nowMs,
                updatedAtEpochMs = nowMs
            )
        )

        val initialTasks = planningEngine.generateInitialPlan(goalId, input, nowMs)
        repository.insertGoalTasks(initialTasks)
        repository.storeMemories(
            listOf(
                MemoryType.LONG_TERM to "Goal created: ${input.title}, deadline ${input.deadline}",
                MemoryType.EPISODIC to "Planning initialized for goal ${input.title}"
            )
        )
    }

    suspend fun markTaskComplete(taskId: Long, goalId: Long) {
        if (repository.markTaskDone(taskId)) {
            repository.completeGoalIfAllTasksDone(goalId)
        }
    }

    suspend fun processMeetingTranscript(title: String, transcript: String): String {
        val extraction = localModelBridge.extractMeeting(transcript)
        repository.storeMeeting(title = title, transcript = transcript, summary = extraction.summary)
        val targetGoalId = getOrCreateCaptureGoal()

        val now = System.currentTimeMillis()
        extraction.extractedTasks.forEach { extracted ->
            if (repository.openTaskExists(targetGoalId, extracted.title)) return@forEach
            val blocker = extracted.dependencyNote?.takeIf { it.isNotBlank() }
            repository.createTask(
                TaskEntity(
                    goalId = targetGoalId,
                    title = extracted.title,
                    details = detailsForExtractedTask(extracted.title, extracted.dependencyNote, transcript),
                    status = if (blocker == null) TaskStatus.PENDING else TaskStatus.BLOCKED,
                    priority = extracted.priority,
                    owner = extracted.owner,
                    deadlineEpochDay = extracted.deadline?.toEpochDay(),
                    dependencyTaskId = null,
                    blockedReason = blocker,
                    estimatedMinutes = 45,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            )
        }

        repository.storeMemories(
            extraction.extractedCommitments.map { MemoryType.COMMITMENT to it } +
                extraction.extractedDecisions.map { MemoryType.DECISION to it } +
                (MemoryType.MEETING to extraction.summary)
        )

        return extraction.summary
    }

    suspend fun processCapturedMeeting(
        title: String,
        capturedNotes: String,
        audioPath: String?
    ): AssistantAnalysis {
        val safeNotes = capturedNotes.trim()
        if (safeNotes.isBlank()) {
            val message = "I could not capture enough speech to analyze that update. Please try again in a quieter setting and speak for a little longer."
            repository.storeMeeting(
                title = title,
                transcript = "",
                summary = message,
                audioPath = audioPath,
                assistantReply = message
            )
            return AssistantAnalysis(
                timetable = emptyList(),
                summary = message
            )
        }

        val extraction = localModelBridge.extractMeeting(safeNotes)
        val targetGoalId = getOrCreateCaptureGoal()

        val now = System.currentTimeMillis()
        extraction.extractedTasks.forEach { extracted ->
            if (repository.openTaskExists(targetGoalId, extracted.title)) return@forEach
            val blocker = extracted.dependencyNote?.takeIf { it.isNotBlank() }
            repository.createTask(
                TaskEntity(
                    goalId = targetGoalId,
                    title = extracted.title,
                    details = detailsForExtractedTask(extracted.title, extracted.dependencyNote, safeNotes),
                    status = if (blocker == null) TaskStatus.PENDING else TaskStatus.BLOCKED,
                    priority = extracted.priority,
                    owner = extracted.owner,
                    deadlineEpochDay = extracted.deadline?.toEpochDay(),
                    dependencyTaskId = null,
                    blockedReason = blocker,
                    estimatedMinutes = 45,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            )
        }

        val prioritizedTasks = scheduleEngine.prioritize(repository.getOpenTasks(), LocalDate.now())
        val timetable = scheduleEngine.buildTimetable(prioritizedTasks, LocalDate.now())
        val summary = localModelBridge.generateRecommendation(
            buildPaRecommendationContext(safeNotes, extraction.summary, timetable)
        )

        repository.storeMeeting(
            title = title,
            transcript = safeNotes,
            summary = extraction.summary,
            audioPath = audioPath,
            assistantReply = summary
        )
        repository.storeMemories(
            extraction.extractedCommitments.map { MemoryType.COMMITMENT to it } +
                extraction.extractedDecisions.map { MemoryType.DECISION to it } +
                (MemoryType.MEETING to extraction.summary) +
                (MemoryType.EPISODIC to summary)
        )

        return AssistantAnalysis(timetable = timetable, summary = summary)
    }

    fun generateMorningBriefing(tasks: List<TaskEntity>): String {
        val briefing = briefingEngine.generateMorningBriefing(tasks, LocalDate.now())
        return buildString {
            append(briefing.message)
            if (briefing.topTasks.isNotEmpty()) {
                append("\n\nMy read: ")
                append(briefing.topTasks.joinToString(" "))
            }
        }.trim()
    }

    fun generateProactiveNudges(tasks: List<TaskEntity>): List<String> {
        return proactiveEngine.generateNudges(tasks, LocalDate.now())
    }

    fun buildCurrentTimetable(tasks: List<TaskEntity>): List<String> {
        val prioritized = scheduleEngine.prioritize(tasks, LocalDate.now())
        return scheduleEngine.buildTimetable(prioritized, LocalDate.now())
    }

    suspend fun runtimeRecommendation(context: String): String {
        return localModelBridge.generateRecommendation(context)
    }

    private fun buildPaRecommendationContext(
        capturedNotes: String,
        extractionSummary: String,
        timetable: List<String>
    ): String {
        return """
            Meeting notes:
            $capturedNotes

            Extracted summary:
            $extractionSummary

            Recommended timetable:
            ${timetable.joinToString("\n")}

            Speak to the user like a private executive assistant. Explain what to do first, why it matters, and how to move through the timetable.
        """.trimIndent()
    }

    private suspend fun getOrCreateCaptureGoal(): Long {
        val title = "Captured Commitments"
        repository.activeGoalIdByTitle(title)?.let { return it }
        val now = System.currentTimeMillis()
        return repository.createGoal(
            GoalEntity(
                title = title,
                why = "Keep meeting actions and spoken commitments together",
                deadlineEpochDay = LocalDate.now().plusDays(7).toEpochDay(),
                status = GoalStatus.ACTIVE,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
        )
    }

    private fun detailsForExtractedTask(
        title: String,
        dependencyNote: String?,
        sourceNotes: String
    ): String {
        val relatedLine = sourceNotes
            .lines()
            .map { it.trim() }
            .firstOrNull { line -> line.contains(title, ignoreCase = true) }
            ?: sourceNotes.take(180)

        return buildString {
            append("Captured context: ")
            append(relatedLine.trim().trimEnd('.'))
            dependencyNote?.takeIf { it.isNotBlank() }?.let {
                append(". Dependency: ")
                append(it.trim().trimEnd('.'))
            }
        }
    }
}
