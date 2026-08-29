package com.opengranola.android.memory

import android.content.Context
import com.opengranola.android.notification.NotificationStore
import com.opengranola.android.sync.SyncStore
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ContextMemoryStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("assistant_memory", Context.MODE_PRIVATE)
    private val syncStore = SyncStore(appContext)

    @Synchronized
    fun addContext(text: String, source: String = "manual") {
        addMemory(text, source)
        text.split('\n', '.', '!', '?').map { it.trim().trimStart('-', '*').trim() }
            .filter { it.startsWithTaskVerb() }.take(5).forEach { sentence ->
                val parts = sentence.split(Regex("\\s+before\\s+"), limit = 2)
                val prerequisite = addTask(parts.first())
                if (parts.size == 2) addTask(parts[1], dependsOn = listOf(prerequisite.optString("id")))
            }
    }

    @Synchronized
    fun addMemory(text: String, source: String = "manual", importance: Int = 5): JSONObject {
        require(text.isNotBlank()) { "memory text is empty" }
        val item = JSONObject().put("id", UUID.randomUUID().toString()).put("type", "memory")
            .put("source", source).put("text", text.trim()).put("importance", importance.coerceIn(1, 10))
            .put("created_at", System.currentTimeMillis())
        val entries = rawEntries().put(item)
        prefs.edit().putString(KEY_ENTRIES, entries.toString()).apply()
        return item
    }

    @Synchronized
    fun addTask(title: String, description: String = "", priority: Int = 3, urgency: Int = 3, impact: Int = 3, dueAt: Long? = null, dependsOn: List<String> = emptyList()): JSONObject {
        require(title.isNotBlank()) { "task title is empty" }
        val entries = rawEntries()
        entries.asList().firstOrNull { it.optString("type") == "task" && it.optString("status") != "done" && it.optString("title").equals(title.trim(), true) }?.let { return it }
        val item = JSONObject().put("id", UUID.randomUUID().toString()).put("type", "task").put("title", title.trim())
            .put("description", description.trim()).put("priority", priority.coerceIn(1, 5)).put("urgency", urgency.coerceIn(1, 5))
            .put("impact", impact.coerceIn(1, 5)).put("status", "todo").put("created_at", System.currentTimeMillis())
            .put("depends_on", JSONArray(dependsOn.filter(String::isNotBlank).distinct()))
        if (dueAt != null) item.put("due_at", dueAt)
        prefs.edit().putString(KEY_ENTRIES, entries.put(item).toString()).apply()
        return item
    }

    @Synchronized
    fun completeTask(id: String) {
        val entries = rawEntries()
        entries.asList().firstOrNull { it.optString("id") == id }?.put("status", "done")
        prefs.edit().putString(KEY_ENTRIES, entries.toString()).apply()
    }

    @Synchronized
    fun contextJson(query: String? = null): JSONObject {
        val memories = rawEntries().asList().filter { it.optString("type") == "memory" }
        val terms = query.orEmpty().lowercase().split(Regex("[^a-z0-9]+")) .filter { it.length > 1 }
        val selected = memories.sortedByDescending { item -> terms.count { item.optString("text").lowercase().contains(it) } * 10 + item.optInt("importance") }.take(20)
        return JSONObject()
            .put("memory", JSONArray(selected))
            .put("task_plan", planJson())
            .put("synced_context", JSONArray(syncStore.context().ifBlank { "[]" }))
            .put("notifications", JSONArray(NotificationStore(appContext).recent().map {
                JSONObject().put("title", it.title).put("text", it.text).put("package", it.packageName).put("posted_at", it.postedAt)
            }))
    }

    @Synchronized
    fun planJson(): JSONObject {
        val tasks = rawEntries().asList().filter { it.optString("type") == "task" }.map { it.toTask() }
        val plan = TaskPlanner.plan(tasks)
        return JSONObject().put("next_task_id", plan.ordered.firstOrNull()?.task?.id.orEmpty())
            .put("today_tasks", JSONArray(plan.ordered.take(3).map { it.toJson() }))
            .put("blocked_tasks", JSONArray(plan.blocked.map { JSONObject().put("id", it.task.id).put("title", it.task.title).put("blocked_by", JSONArray(it.blockedBy)) }))
            .put("cycle_task_ids", JSONArray(plan.cycles))
    }

    fun planText(): String {
        val tasks = rawEntries().asList().filter { it.optString("type") == "task" }.map { it.toTask() }
        val plan = TaskPlanner.plan(tasks)
        return buildString {
            appendLine("Next actions")
            if (plan.ordered.isEmpty()) appendLine("No unblocked tasks yet.")
            plan.ordered.take(3).forEachIndexed { index, task -> appendLine("${index + 1}. ${task.task.title}") }
            plan.blocked.forEach { appendLine("Blocked: ${it.task.title} → ${it.blockedBy.joinToString()}") }
            if (plan.cycles.isNotEmpty()) appendLine("Dependency cycle: ${plan.cycles.joinToString()}")
        }.trim()
    }

    private fun rawEntries() = JSONArray(prefs.getString(KEY_ENTRIES, "[]"))
    private fun JSONArray.asList() = (0 until length()).map { getJSONObject(it) }
    private fun JSONObject.toTask(): SmartTask {
        val dependencies = optJSONArray("depends_on") ?: JSONArray()
        return SmartTask(optString("id"), optString("title"), optString("description"), optInt("priority", 3), optInt("urgency", 3), optInt("impact", 3), if (has("due_at")) optLong("due_at") else null, optString("status", "todo"), (0 until dependencies.length()).map { dependencies.optString(it) })
    }
    private fun PlannedTask.toJson() = JSONObject().put("id", task.id).put("title", task.title).put("score", score).put("depends_on", JSONArray(task.dependsOn))
    private fun String.startsWithTaskVerb() = lowercase().startsWithAny("todo:", "task:", "need to ", "must ", "should ", "finish ", "complete ", "implement ", "submit ", "learn ")
    private fun String.startsWithAny(vararg values: String) = values.any { startsWith(it) }

    companion object { private const val KEY_ENTRIES = "memory_entries" }
}
