package com.opengranola.android.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class FrontierMessage(val role: String, val content: String)

/** OpenRouter frontier calls; only curated context should be passed here. */
class OpenRouterCoachClient(context: Context) {
    private val appContext = context.applicationContext
    private val secrets = SecureSecretStore(appContext)
    private val preferences = appContext.getSharedPreferences("frontier", Context.MODE_PRIVATE)

    val isConfigured: Boolean get() = !secrets.get(KEY_API).isNullOrBlank()
    val model: String get() = preferences.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL

    fun saveApiKey(value: String) = secrets.put(KEY_API, value.trim())
    fun clearApiKey() = secrets.clear(KEY_API)
    fun saveModel(value: String) = preferences.edit().putString(KEY_MODEL, value.trim().ifBlank { DEFAULT_MODEL }).apply()

    suspend fun chat(message: String, context: String, history: List<AssistantTurn>): String = complete(
        listOf(FrontierMessage("system", "You are pa. $STRICT_COACH_INSTRUCTIONS Use only the curated context. Never claim an action was executed. Reply in at most 5 short lines, each starting with •. No Markdown headings, asterisks, or dashes.")) +
            history.takeLast(6).map { FrontierMessage(it.role, it.content) } +
            FrontierMessage("user", "CURATED CONTEXT:\n$context\n\nUSER MESSAGE:\n$message"),
        768
    )

    suspend fun generatePlan(objective: String, context: String): GeneratedPlan {
        val raw = complete(listOf(FrontierMessage("user", """
            You are pa. $STRICT_COACH_INSTRUCTIONS
            Create a practical plan from this curated context. Reject fantasy scheduling and expose missing prerequisites plainly.
            Return only JSON: {"title":"...","objective":"...","blockers":["..."],"tasks":[{"title":"...","details":"...","priority":1,"dependsOn":[],"estimatedMinutes":20}]}
            Inspect the context for unfinished skills, tasks, or prerequisites that can block this objective. Put up to 3 concise, evidence-based warnings in blockers; use [] when none exist.
            dependsOn contains zero-based indexes of real prerequisite tasks. Use [] when no prerequisite is known.
            estimatedMinutes must be a realistic focused-work estimate from 1 to 480 minutes.
            Use 3-7 concrete tasks. Do not claim anything is already done.
            OBJECTIVE: $objective
            CURATED CONTEXT: $context
        """.trimIndent())), 1536)
        val jsonText = raw.substringAfter('{', "").let { if (it.isBlank()) "" else "{$it" }
            .substringBeforeLast('}', "").let { if (it.isBlank()) "" else "$it}" }
        return runCatching {
            val json = JSONObject(jsonText)
            val tasks = json.getJSONArray("tasks").let { array ->
                (0 until array.length()).map { index ->
                    val task = array.getJSONObject(index)
                    GeneratedTask(
                        task.getString("title"),
                        task.optString("details"),
                        task.optInt("priority", index + 1),
                        task.optJSONArray("dependsOn")?.let { deps -> (0 until deps.length()).map { deps.optInt(it, -1) }.filter { it >= 0 } } ?: emptyList(),
                        task.optInt("estimatedMinutes", 15).coerceIn(1, 480)
                    )
                }
            }
            val blockers = json.optJSONArray("blockers")?.let { array ->
                (0 until array.length()).map { array.optString(it).trim() }.filter { it.isNotBlank() }.take(3)
            } ?: emptyList()
            GeneratedPlan(json.optString("title", "Plan"), json.optString("objective", objective), tasks, blockers)
        }.getOrElse {
            GeneratedPlan("Plan for ${objective.take(48)}", objective, listOf(GeneratedTask("Review the frontier response", raw.take(1200), 1)))
        }
    }

    suspend fun generateDailyBriefing(context: String): String = complete(listOf(FrontierMessage("user", """
        You are pa. $STRICT_COACH_INSTRUCTIONS
        Create a concise daily briefing from the curated context below.
        Write exactly three one-line sections: Focus, Reality check, Next step. Maximum 75 words total.
        No preamble, bullets, explanations, repetition, or extra sections.
        Be factual and do not invent deadlines or claim actions were completed.
        CURATED CONTEXT:
        $context
    """.trimIndent())), 512)

    private suspend fun complete(messages: List<FrontierMessage>, maxTokens: Int): String = withContext(Dispatchers.IO) {
        val key = secrets.get(KEY_API)?.takeIf { it.isNotBlank() } ?: error("Connect OpenRouter in Frontier settings first")
        val connection = (URL("https://openrouter.ai/api/v1/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-OpenRouter-Title", "pa")
        }
        try {
            val body = JSONObject()
                .put("model", model)
                .put("max_tokens", maxOf(maxTokens, 2_048))
                .put("reasoning", JSONObject().put("effort", "low").put("exclude", true))
                .put("messages", JSONArray().apply { messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) } })
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val response = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) error("OpenRouter ${connection.responseCode}: ${JSONObject(response).optString("error", response).take(300)}")
            val choice = JSONObject(response).getJSONArray("choices").getJSONObject(0)
            choice.getJSONObject("message").getString("content").trim()
                .takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
                ?: error("OpenRouter returned no text (${choice.optString("finish_reason", "unknown")}). Try another model.")
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val KEY_API = "openrouter_api_key"
        const val KEY_MODEL = "openrouter_model"
        const val DEFAULT_MODEL = "openai/gpt-chat-latest"
    }
}
