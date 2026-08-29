package com.geniex.demo

import android.content.Context
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONArray
import org.json.JSONObject

class ContextMemoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("assistant_memory", Context.MODE_PRIVATE)

    fun addContext(text: String, source: String = "manual"): JsonObject {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "context text is empty" }
        val item = JSONObject()
            .put("id", UUID.randomUUID().toString())
            .put("source", source)
            .put("type", "context")
            .put("text", trimmed)
            .put("timestamp", Instant.now().toString())
        val entries = rawEntries()
        entries.put(item)
        prefs.edit().putString(KEY_CONTEXT, entries.toString()).apply()
        return item.toJsonObject()
    }

    fun recent(limit: Int = 20): List<JSONObject> {
        val entries = rawEntries()
        val start = (entries.length() - limit).coerceAtLeast(0)
        return (start until entries.length()).map { entries.getJSONObject(it) }.reversed()
    }

    fun dashboardText(): String {
        val recent = recent(5)
        if (recent.isEmpty()) return "No context saved yet. Add what you are working on."
        return buildString {
            appendLine("Saved context: ${rawEntries().length()} items")
            recent.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.optString("text")}")
            }
        }.trim()
    }

    fun contextJson(): JsonObject = buildJsonObject {
        put("count", rawEntries().length())
        put("recent", JsonArray(recent(20).map { it.toJsonObject() }))
    }

    private fun rawEntries(): JSONArray = JSONArray(prefs.getString(KEY_CONTEXT, "[]"))

    private fun JSONObject.toJsonObject(): JsonObject = buildJsonObject {
        keys().forEach { key -> put(key, optString(key)) }
    }

    companion object {
        private const val KEY_CONTEXT = "context_events"
    }
}
