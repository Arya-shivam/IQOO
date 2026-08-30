package com.opengranola.android.domain

import com.opengranola.android.calendar.CalendarSnapshot
import com.opengranola.android.data.CommitmentEntity
import com.opengranola.android.data.PlanTaskEntity
import com.opengranola.android.usage.AppUsage
import com.opengranola.android.usage.UsageSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriorityEngineTest {
    private val engine = PriorityEngine()

    @Test
    fun ranksOpenCommitmentsAndPlanTasksIntoTimetable() {
        val briefing = engine.build(
            planTasks = listOf(
                PlanTaskEntity(
                    id = "task-1",
                    planId = "plan-1",
                    title = "Polish demo",
                    details = "Launch demo due today",
                    status = "todo",
                    priority = 1,
                    position = 0
                ),
                PlanTaskEntity(
                    id = "task-2",
                    planId = "plan-1",
                    title = "Archive old notes",
                    details = "",
                    status = "done",
                    priority = 1,
                    position = 1
                )
            ),
            commitments = listOf(
                CommitmentEntity(
                    id = "commitment-1",
                    meetingId = "meeting-1",
                    sourceTitle = "Investor sync",
                    title = "Send prototype update",
                    owner = "You",
                    dueText = "today",
                    evidence = "I will send the prototype update today",
                    confidence = .9f,
                    status = "open",
                    createdAt = 1L,
                    updatedAt = 2L
                )
            ),
            calendar = CalendarSnapshot(hasPermission = true),
            usage = UsageSnapshot(),
            notificationsToday = 0
        )

        assertEquals(2, briefing.timetable.size)
        assertTrue(briefing.timetable.first().title in setOf("Polish demo", "Send prototype update"))
        assertTrue(briefing.timetable.none { it.title == "Archive old notes" })
        assertTrue(briefing.nudges.isNotEmpty())
    }

    @Test
    fun includesUsageAndNotificationNudgesFromExistingSnapshots() {
        val briefing = engine.build(
            planTasks = emptyList(),
            commitments = emptyList(),
            calendar = CalendarSnapshot(),
            usage = UsageSnapshot(
                totalMinutes = 90,
                pickups = 4,
                apps = listOf(AppUsage("chat.app", "ChatApp", 55, .61f)),
                hasPermission = true
            ),
            notificationsToday = 13
        )

        assertTrue(briefing.nudges.any { it.contains("ChatApp") })
        assertTrue(briefing.nudges.any { it.contains("13 alerts") })
    }
}
