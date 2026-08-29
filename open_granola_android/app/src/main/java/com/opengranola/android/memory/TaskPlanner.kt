package com.opengranola.android.memory

import java.util.PriorityQueue

data class SmartTask(
    val id: String,
    val title: String,
    val description: String = "",
    val priority: Int = 3,
    val urgency: Int = 3,
    val impact: Int = 3,
    val dueAt: Long? = null,
    val status: String = "todo",
    val dependsOn: List<String> = emptyList()
)

data class PlannedTask(val task: SmartTask, val score: Int, val blockedBy: List<String> = emptyList())
data class TaskPlan(val ordered: List<PlannedTask>, val blocked: List<PlannedTask>, val cycles: List<String>)

object TaskPlanner {
    fun plan(tasks: List<SmartTask>, now: Long = System.currentTimeMillis()): TaskPlan {
        val active = tasks.filter { it.status != "done" }
        val all = tasks.associateBy { it.id }
        val byId = active.associateBy { it.id }
        val dependents = active.associate { task -> task.id to active.count { task.id in it.dependsOn } }
        val blockers = active.associate { task -> task.id to task.dependsOn.filter { all[it]?.status != "done" } }
        val indegree = active.associate { it.id to it.dependsOn.count { dependency -> dependency in byId } }.toMutableMap()
        val available = PriorityQueue<String> { left, right ->
            (score(byId.getValue(right), dependents.getValue(right), now) - score(byId.getValue(left), dependents.getValue(left), now))
                .takeIf { it != 0 } ?: left.compareTo(right)
        }
        active.filter { indegree.getValue(it.id) == 0 && blockers.getValue(it.id).isEmpty() }.forEach { available += it.id }
        val order = mutableListOf<String>()
        while (available.isNotEmpty()) {
            val id = available.remove()
            order += id
            active.filter { id in it.dependsOn }.forEach { dependent ->
                indegree[dependent.id] = indegree.getValue(dependent.id) - 1
                if (indegree.getValue(dependent.id) == 0) available += dependent.id
            }
        }
        val ordered = order.map { PlannedTask(byId.getValue(it), score(byId.getValue(it), dependents.getValue(it), now)) }
        val blocked = active.filter { it.id !in order }.map { PlannedTask(it, score(it, dependents.getValue(it), now), blockers.getValue(it)) }
        return TaskPlan(ordered, blocked, findCycles(active))
    }

    private fun score(task: SmartTask, unlocks: Int, now: Long): Int {
        val dueBoost = task.dueAt?.let { when {
            it <= now -> 8
            it <= now + DAY -> 5
            it <= now + 7 * DAY -> 2
            else -> 0
        } } ?: 0
        return task.priority.coerceIn(1, 5) * 4 + task.urgency.coerceIn(1, 5) * 3 +
            task.impact.coerceIn(1, 5) * 2 + dueBoost + unlocks * 2
    }

    private fun findCycles(tasks: List<SmartTask>): List<String> {
        val byId = tasks.associateBy { it.id }
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        val cycles = linkedSetOf<String>()
        fun visit(id: String) {
            if (id in visiting) { cycles += id; return }
            if (!visited.add(id)) return
            visiting += id
            byId.getValue(id).dependsOn.filter { it in byId }.forEach(::visit)
            visiting -= id
        }
        tasks.forEach { visit(it.id) }
        return cycles.toList()
    }

    private const val DAY = 24 * 60 * 60 * 1000L
}
