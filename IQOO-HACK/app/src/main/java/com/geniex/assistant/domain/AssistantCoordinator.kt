package com.geniex.assistant.domain

import com.geniex.assistant.data.db.GoalEntity
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.data.repo.AssistantRepository
import com.geniex.assistant.llm.LocalModelBridge
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
        val targetGoalId = repository.latestActiveGoalId() ?: repository.createGoal(
            GoalEntity(
                title = "Meeting Action Items",
                why = "Auto-created to track extracted commitments",
                deadlineEpochDay = LocalDate.now().plusDays(7).toEpochDay(),
                status = GoalStatus.ACTIVE,
                createdAtEpochMs = System.currentTimeMillis(),
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )

        val now = System.currentTimeMillis()
        extraction.extractedTasks.forEach { extracted ->
            repository.createTask(
                TaskEntity(
                    goalId = targetGoalId,
                    title = extracted.title,
                    details = extracted.dependencyNote ?: "Extracted from meeting",
                    status = TaskStatus.PENDING,
                    priority = 8,
                    owner = extracted.owner,
                    deadlineEpochDay = extracted.deadline?.toEpochDay(),
                    dependencyTaskId = null,
                    blockedReason = null,
                    estimatedMinutes = 45,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            )
        }

        repository.storeMemories(
            extraction.extractedCommitments.map { MemoryType.COMMITMENT to it } +
                (MemoryType.MEETING to extraction.summary)
        )

        return extraction.summary
    }

    fun generateMorningBriefing(tasks: List<TaskEntity>): String {
        val briefing = briefingEngine.generateMorningBriefing(tasks, LocalDate.now())
        return buildString {
            append(briefing.message)
            if (briefing.topTasks.isNotEmpty()) {
                append("\n\nTop tasks:\n")
                briefing.topTasks.forEach { append("• $it\n") }
            }
        }.trim()
    }

    fun generateProactiveNudges(tasks: List<TaskEntity>): List<String> {
        return proactiveEngine.generateNudges(tasks, LocalDate.now())
    }

    suspend fun runtimeRecommendation(context: String): String {
        return localModelBridge.generateRecommendation(context)
    }
}
