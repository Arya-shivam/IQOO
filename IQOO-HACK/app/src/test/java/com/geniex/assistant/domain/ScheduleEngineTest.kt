package com.geniex.assistant.domain

import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleEngineTest {
    private val today = LocalDate.of(2026, 8, 29)
    private val engine = ScheduleEngine(ImportanceScorer())

    @Test
    fun `prioritize ranks overdue meaningful work ahead of undated work`() {
        val overdue = task(
            title = "Send client proposal",
            priority = 7,
            deadline = today.minusDays(1),
            createdAt = 2
        )
        val undated = task(
            title = "Polish animation",
            priority = 8,
            deadline = null,
            createdAt = 1
        )

        assertEquals(listOf(overdue, undated), engine.prioritize(listOf(undated, overdue), today))
    }

    @Test
    fun `prioritize does not invent deadlines or overwrite priority`() {
        val task = task(title = "Plan next milestone", priority = 6, deadline = null)

        val result = engine.prioritize(listOf(task), today).single()

        assertEquals(6, result.priority)
        assertNull(result.deadlineEpochDay)
        assertEquals(task, result)
    }

    @Test
    fun `timetable turns blocked work into an unblock action`() {
        val blocked = task(
            title = "API integration",
            priority = 8,
            deadline = today.plusDays(2),
            status = TaskStatus.BLOCKED,
            blockedReason = "credentials from Raj"
        )

        val line = engine.buildTimetable(listOf(blocked), today).single()

        assertTrue(line.startsWith("First available moment: Unblock API integration"))
        assertTrue(line.contains("credentials from Raj"))
    }

    private fun task(
        title: String,
        priority: Int,
        deadline: LocalDate?,
        status: TaskStatus = TaskStatus.PENDING,
        blockedReason: String? = null,
        createdAt: Long = 1
    ) = TaskEntity(
        id = createdAt,
        goalId = 1,
        title = title,
        details = "",
        status = status,
        priority = priority,
        owner = "You",
        deadlineEpochDay = deadline?.toEpochDay(),
        dependencyTaskId = null,
        blockedReason = blockedReason,
        estimatedMinutes = 45,
        createdAtEpochMs = createdAt,
        updatedAtEpochMs = createdAt
    )
}
