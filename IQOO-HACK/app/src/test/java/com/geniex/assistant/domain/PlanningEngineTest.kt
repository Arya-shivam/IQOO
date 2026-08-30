package com.geniex.assistant.domain

import com.geniex.assistant.model.GoalInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlanningEngineTest {
    @Test
    fun `generated plan belongs to goal and never passes its deadline`() {
        val deadline = LocalDate.now().plusDays(2)
        val tasks = PlanningEngine().generateInitialPlan(
            goalId = 42,
            goalInput = GoalInput("Prepare for hackathon", "Build the demo", deadline),
            nowMs = 100
        )

        assertEquals(5, tasks.size)
        assertTrue(tasks.all { it.goalId == 42L })
        assertTrue(tasks.all { task ->
            task.deadlineEpochDay?.let { it <= deadline.toEpochDay() } == true
        })
        assertTrue(tasks.zipWithNext().all { (first, second) -> first.priority >= second.priority })
    }
}
