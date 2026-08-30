package com.geniex.assistant.llm

import android.content.Context
import com.geniex.assistant.model.ExtractedTask
import com.geniex.assistant.model.MeetingExtraction
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class GenieXQwenLocalBridge(
    private val context: Context,
    private val modelDirectoryProvider: suspend () -> String?
) : LocalModelBridge {

    private val modelLoadLock = Mutex()
    private var loadedModel: LlamaModel? = null
    private var loadedModelFilePath: String? = null
    private var lastModelLoadError: String? = null

    private val defaultLlamaConfig = LlamaConfig(
        contextSize = 2048,
        threads = Runtime.getRuntime().availableProcessors().coerceAtLeast(2),
        gpuLayers = 0,
        temperature = 0.2f,
        topP = 0.9f,
        topK = 40,
        seed = -1
    )

    override suspend fun extractMeeting(transcript: String): MeetingExtraction {
        val modelDirectory = normalizeModelDirectory(modelDirectoryProvider())
        val accessIssue = modelAccessIssue(modelDirectory)
        val runtimeModel = if (accessIssue == null) getOrLoadRuntimeModel(modelDirectory) else null

        if (runtimeModel != null) {
            runCatching {
                val prompt = buildMeetingExtractionPrompt(transcript)
                val result = Llama.complete(
                    runtimeModel,
                    prompt,
                    "You extract meeting actions and output strict JSON only.",
                    320
                )

                val parsed = parseModelMeetingExtraction(result.text)
                if (parsed != null) {
                    return parsed
                }
            }
        }

        return fallbackMeetingExtraction(transcript)
    }

    override suspend fun generateRecommendation(context: String): String {
        val modelDirectory = normalizeModelDirectory(modelDirectoryProvider())
        val accessIssue = modelAccessIssue(modelDirectory)
        if (accessIssue != null) return accessIssue

        val runtimeModel = getOrLoadRuntimeModel(modelDirectory)
        if (runtimeModel == null) {
            return fallbackRecommendation(context)
        }

        return runCatching {
            val prompt = buildRecommendationPrompt(context)
            val result = Llama.complete(
                runtimeModel,
                prompt,
                "You are an on-device executive assistant. Do not reveal reasoning, analysis, or thinking. Output only the final answer in plain text.",
                180
            )

            formatRecommendation(result.text).ifBlank {
                "Local model returned an empty recommendation. Try again after adding more task context."
            }
        }.getOrElse {
            fallbackRecommendation(context)
        }
    }

    private fun fallbackRecommendation(context: String): String {
        val taskLine = context
            .lines()
            .map { it.trim() }
            .firstOrNull { it.startsWith("- ") }
            ?.removePrefix("- ")

        if (taskLine.isNullOrBlank()) {
            return "I do not see any open work that needs a decision right now.\nI would use this time to plan the next milestone or clear small admin work before it piles up."
        }

        val taskTitle = taskLine.substringBefore(":").trim().ifBlank { "the first open task" }
        val lowerTask = taskLine.lowercase()
        val reason = when {
            "blocked because" in lowerTask -> "it is blocking the rest of the plan"
            "overdue" in lowerTask -> "it is already overdue"
            "due today" in lowerTask -> "it is due today"
            "due tomorrow" in lowerTask -> "the deadline is close"
            else -> "it has the strongest priority signal in your current plan"
        }

        return buildString {
            append("I'd start with ")
            append(taskTitle)
            append(" first.\n")
            append("It matters now because ")
            append(reason)
            append(".\n")
            append("I would spend one focused block moving it forward before touching lower-impact work.")
        }
    }

    private fun fallbackMeetingExtraction(transcript: String): MeetingExtraction {
        val normalized = transcript
            .split(Regex("\\n|(?<=[.!?])\\s+|\\bthen\\b|\\bafter that\\b|\\bfirst\\b|\\bsecond\\b|\\bthird\\b", RegexOption.IGNORE_CASE))
            .map { it.trim().trim('.', ',', ';', ':') }
            .filter { it.isNotEmpty() }
        val extractedTasks = mutableListOf<ExtractedTask>()
        val commitments = mutableListOf<String>()
        val decisions = mutableListOf<String>()

        normalized.forEach { line ->
            val lower = line.lowercase()
            if ("decided" in lower || "decision" in lower || "we agreed" in lower) {
                decisions += line
            }
            if (
                " will " in lower ||
                lower.startsWith("will ") ||
                " going to " in lower ||
                lower.startsWith("going to ") ||
                " have to " in lower ||
                " need to " in lower
            ) {
                commitments += line
            }

            if (isActionableSpeechLine(lower)) {
                val title = cleanActionTitle(line)
                expandTaskTitles(title).forEach { atomicTitle ->
                    extractedTasks += ExtractedTask(
                        title = atomicTitle.take(100),
                        owner = inferOwner(line),
                        deadline = inferDate(lower),
                        dependencyNote = inferDependency(line),
                        priority = inferPriority(lower)
                    )
                }
            }
        }

        val summary = when {
            extractedTasks.isNotEmpty() && commitments.isNotEmpty() ->
                "I found ${extractedTasks.size} action item(s) and ${commitments.size} commitment(s)."
            extractedTasks.isNotEmpty() -> "I found ${extractedTasks.size} action item(s)."
            commitments.isNotEmpty() -> "I found ${commitments.size} commitment(s)."
            else -> "I saved the update, but did not find a clear action item."
        }

        return MeetingExtraction(
            summary = summary,
            extractedTasks = extractedTasks.distinctBy { it.title },
            extractedCommitments = commitments.distinct(),
            extractedDecisions = decisions.distinct()
        )
    }

    private fun isActionableSpeechLine(lowerLine: String): Boolean {
        val actionSignals = listOf(
            "will ",
            "going to ",
            "have to ",
            "need to ",
            "should ",
            "todo",
            "task",
            "implement",
            "finish",
            "fix",
            "build",
            "capture",
            "store",
            "update",
            "test",
            "deadline",
            "demo"
        )
        return actionSignals.any { it in lowerLine }
    }

    private fun cleanActionTitle(line: String): String {
        val withoutCompletionTail = line.replace(
            Regex("\\b(should|must|needs to|need to|has to|have to|will)?\\s*(be\\s+)?(completed|finished|done)\\b.*$", RegexOption.IGNORE_CASE),
            ""
        )
        return withoutCompletionTail
            .replace(
                Regex(
                    "^(i am going to|i'm going to|we are going to|we're going to|going to|we have to|i have to|have to|we need to|i need to|need to|we should|i should|should|todo|task)\\s+",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun expandTaskTitles(rawTitle: String): List<String> {
        val cleaned = rawTitle.trim().trim('.', ',', ';', ':')
        if (cleaned.isBlank() || isMetaTaskTitle(cleaned)) return emptyList()

        val separated = cleaned
            .split(Regex("\\s*,\\s*|\\s+and\\s+|\\s*&\\s+", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.isNotBlank() && !isMetaTaskTitle(it) }

        if (separated.size > 1) return separated.map(::normalizeTaskTitle).distinct()

        val knownPhrases = listOf(
            "inventory management",
            "factory management",
            "financial model",
            "meeting recording",
            "voice input",
            "capture audio",
            "store meeting context",
            "extract tasks",
            "update schedules",
            "memory system",
            "local llm",
            "notification",
            "procurement",
            "api"
        )

        val lower = cleaned.lowercase()
        val phraseMatches = knownPhrases
            .filter { phrase -> Regex("\\b${Regex.escape(phrase)}\\b").containsMatchIn(lower) }
            .map(::normalizeTaskTitle)
            .distinct()

        return if (phraseMatches.size > 1) phraseMatches else listOf(normalizeTaskTitle(cleaned))
    }

    private fun normalizeTaskTitle(value: String): String {
        val normalized = value.trim().trim('.', ',', ';', ':')
        if (normalized.equals("api", ignoreCase = true)) return "API"
        if (normalized.equals("llm", ignoreCase = true)) return "LLM"
        return normalized
            .split(Regex("\\s+"))
            .joinToString(" ") { word ->
                if (word.equals("api", ignoreCase = true) || word.equals("llm", ignoreCase = true)) {
                    word.uppercase()
                } else {
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
    }

    private fun isMetaTaskTitle(value: String): Boolean {
        val lower = value.lowercase()
        return lower.contains("list of task") ||
            lower.contains("list of tasks") ||
            lower == "task" ||
            lower == "tasks" ||
            lower.startsWith("so we have")
    }

    private suspend fun getOrLoadRuntimeModel(modelDirectory: String?): LlamaModel? {
        if (modelDirectory.isNullOrBlank()) {
            lastModelLoadError = "Model directory is empty."
            return null
        }

        val ggufFile = resolveModelFile(modelDirectory)
        if (ggufFile == null) {
            lastModelLoadError = "No .gguf file found under $modelDirectory"
            return null
        }

        return modelLoadLock.withLock {
            if (loadedModel?.isLoaded == true && loadedModelFilePath == ggufFile.absolutePath) {
                lastModelLoadError = null
                return@withLock loadedModel
            }

            loadedModel?.let {
                runCatching { Llama.releaseModel(it) }
            }

            val loaded = runCatching {
                Llama.loadModel(ggufFile.absolutePath, defaultLlamaConfig)
            }.getOrElse { throwable ->
                lastModelLoadError = "Failed to load ${ggufFile.absolutePath}: ${throwable.message ?: "unknown error"}"
                return@withLock null
            }

            loadedModel = loaded
            loadedModelFilePath = ggufFile.absolutePath
            lastModelLoadError = null
            loaded
        }
    }

    private fun normalizeModelDirectory(rawPath: String?): String? {
        val defaultDirectory = File(context.filesDir, "models")
        val configured = rawPath?.trim()?.takeIf { it.isNotBlank() }?.let(::File)
            ?: return defaultDirectory.absolutePath
        val appFilesPath = context.filesDir.canonicalPath
        return when {
            configured.canonicalPath.startsWith("$appFilesPath/") -> configured.canonicalPath
            else -> defaultDirectory.absolutePath
        }
    }

    private fun modelAccessIssue(modelDirectory: String?): String? {
        return if (modelDirectory.isNullOrBlank()) "The local model directory is not configured." else null
    }

    private fun resolveModelFile(modelDirectory: String): File? {
        val root = File(modelDirectory)
        if (root.isFile && root.extension.equals("gguf", ignoreCase = true)) {
            return root
        }

        if (!root.exists() || !root.isDirectory) return null

        val preferred = File(root, "qwen/qwen.gguf")
        if (preferred.exists() && preferred.isFile) {
            return preferred
        }

        return root.walkTopDown()
            .maxDepth(4)
            .firstOrNull { it.isFile && it.extension.equals("gguf", ignoreCase = true) }
    }

    private fun buildMeetingExtractionPrompt(transcript: String): String {
        return """
            Analyze this user update or meeting note using a Meetily-style notes structure.
            Extract an executive summary, key decisions, commitments, and action items.
            Action item rules:
            - Each task must be atomic. Never merge two or more tasks into one title.
            - Split separate workstreams into separate tasks.
            - Preserve owner, due date, blocker/dependency, priority, and status when mentioned.
            - Priority 10 means urgent, deadline-risk, client/demo/revenue/security impact, or blocking other work.
            - Priority 8-9 means important this week, dependency-sensitive, or owned by someone else.
            - Priority 5-7 means useful but not deadline-sensitive.
            - If work is blocked or waiting for someone/something, set dependencyNote and status "blocked".
            - Ignore meta speech like "we have a list of tasks" or "these are the tasks".
            - If a sentence lists multiple items, create one task per item.
            - Convert relative dates to ISO dates when possible. Use null if uncertain.
            Example: "inventory management procurement factory management should be completed this week" becomes three tasks: "Inventory management", "Procurement", and "Factory management".
            Return strict JSON only, no markdown.
            JSON format:
            {
              "summary": "short summary",
              "decisions": ["decision made"],
              "tasks": [
                {"title":"...","owner":"...","deadline":"YYYY-MM-DD or null","priority":1-10,"status":"pending/in_progress/blocked","dependencyNote":"... or null"}
              ],
              "commitments": ["..."]
            }
            Transcript:
            $transcript
        """.trimIndent()
    }

    private fun buildRecommendationPrompt(context: String): String {
        return """
            You are the user's private executive assistant.
            Speak like a calm, trusted chief-of-staff: direct, warm, and specific.
            Write as if you are personally advising the user, not displaying database records.
            Use simple, natural language. Write two or three short sentences.
            Mention the most important task first, why it matters now, and one concrete next step.
            Do not include labels such as Role, Formal, Priority, Why now, Next, or Next action.
            Do not include formatting instructions, reasoning, analysis, scratchpad, <think> blocks, markdown, bullets, code fences, or JSON.
            Task context:
            $context
        """.trimIndent()
    }

    private fun formatRecommendation(rawText: String): String {
        val cleaned = stripReasoning(rawText)
            .replace("```", "")
            .replace("**", "")
            .replace("__", "")
            .replace("`", "")
            .replace(Regex("\\[(.*?)]\\((.*?)\\)"), "$1")
            .replace("\r", "")
            .trim()

        val structured = extractStructuredResponse(cleaned)
        if (structured != null) return structured

        val candidateJson = extractJsonObject(cleaned)
        if (candidateJson != null) {
            runCatching { JSONObject(candidateJson) }.getOrNull()?.let { root ->
                val priority = root.optString("priority").trim()
                val whyNow = root.optString("why").ifBlank { root.optString("why_now") }.trim()
                val nextAction = root.optString("next_action").ifBlank { root.optString("action") }.trim()
                if (priority.isNotBlank() || whyNow.isNotBlank() || nextAction.isNotBlank()) {
                    return buildHumanReadableResponse(
                        priority = normalizePlaceholder(priority, "Review current tasks"),
                        whyNow = normalizePlaceholder(whyNow, "No explicit reason provided"),
                        nextAction = normalizePlaceholder(nextAction, "Pick one pending task and start a 25-minute focus block")
                    )
                }
            }
        }

        val lines = cleaned
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                line
                    .replace(Regex("^#{1,6}\\s+"), "")
                    .replace(Regex("^[-*]\\s+"), "")
                    .replace(Regex("^\\d+[.)]\\s+"), "")
                    .trim()
            }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return "No data yet"

        val filtered = lines
            .filterNot(::isDisallowedLine)
            .map(::stripRecommendationLabel)
            .filter { it.isNotBlank() }

        val naturalParts = if (filtered.size == 1) {
            splitSentences(filtered.first())
        } else {
            filtered
        }
            .map(::stripRecommendationLabel)
            .filterNot(::isInstructionLeak)
            .take(3)

        if (naturalParts.isNotEmpty()) {
            return naturalParts.joinToString("\n") { ensureSentence(it) }
        }

        return "No data yet"
    }

    private fun stripReasoning(rawText: String): String {
        var text = rawText
            .replace(Regex("(?is)<think>.*?</think>"), " ")
            .replace(Regex("(?is)<analysis>.*?</analysis>"), " ")
            .replace(Regex("(?is)<constraints?>.*?</constraints?>"), " ")
            .replace(Regex("(?im)^\\s*(thinking|analysis|reasoning)\\s*:\\s*$"), "")

        val finalMarkers = listOf("final answer:", "answer:", "response:")
        finalMarkers.forEach { marker ->
            val idx = text.lowercase().lastIndexOf(marker)
            if (idx >= 0) {
                text = text.substring(idx + marker.length)
            }
        }

        return text.trim()
    }

    private fun extractStructuredResponse(text: String): String? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        val priority = lines.firstOrNull { it.startsWith("Priority:", ignoreCase = true) }
            ?.substringAfter(":", "")?.trim().orEmpty()
        val whyNow = lines.firstOrNull { it.startsWith("Why now:", ignoreCase = true) || it.startsWith("Why:", ignoreCase = true) }
            ?.substringAfter(":", "")?.trim().orEmpty()
        val nextAction = lines.firstOrNull { it.startsWith("Next action:", ignoreCase = true) || it.startsWith("Action:", ignoreCase = true) }
            ?.substringAfter(":", "")?.trim().orEmpty()

        if (priority.isBlank() && whyNow.isBlank() && nextAction.isBlank()) return null

        val normalizedPriority = normalizePlaceholder(priority, "Review current tasks")
        val normalizedWhyNow = normalizePlaceholder(whyNow, "No explicit reason provided")
        val normalizedNextAction = normalizePlaceholder(nextAction, "Pick one pending task and start a 25-minute focus block")

        return buildHumanReadableResponse(
            priority = normalizedPriority,
            whyNow = normalizedWhyNow,
            nextAction = normalizedNextAction
        )
    }

    private fun normalizePlaceholder(value: String, fallback: String): String {
        if (value.isBlank()) return fallback
        val stripped = stripRecommendationLabel(value)
        if (stripped.isBlank() || isInstructionLeak(stripped)) return fallback

        val cleaned = stripped.trim().lowercase()
        val placeholderTokens = listOf(
            "<top priority>",
            "<short reason>",
            "<one concrete action>",
            "top priority>",
            "short reason>",
            "one concrete action>",
            "top priority",
            "short reason",
            "one concrete action",
            "your top priority",
            "one short sentence",
            "exactly three lines",
            "n/a"
        )
        if (Regex("<[^>]+>").containsMatchIn(stripped)) return fallback
        return if (placeholderTokens.any { cleaned == it || cleaned.contains("<$it>") }) fallback else stripped.trim()
    }

    private fun buildHumanReadableResponse(
        priority: String,
        whyNow: String,
        nextAction: String
    ): String {
        if (
            priority.equals("Review current tasks", ignoreCase = true) &&
            whyNow.equals("No explicit reason provided", ignoreCase = true) &&
            nextAction.equals("Pick one pending task and start a 25-minute focus block", ignoreCase = true)
        ) {
            return "No data yet"
        }

        return buildString {
            append("I'd put ")
            append(priority.trimEnd('.'))
            append(" first.\n")
            append("It matters now because ")
            append(whyNow.trimEnd('.'))
            append(".\n")
            append("I would ")
            append(nextAction.trimEnd('.'))
            append('.')
        }
    }

    private fun isDisallowedLine(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("constraint") ||
            lower.startsWith("constraints") ||
            lower.startsWith("analysis") ||
            lower.startsWith("reasoning") ||
            lower.startsWith("thinking") ||
            lower.startsWith("task context") ||
            lower.startsWith("context") ||
            lower.startsWith("system") ||
            lower.startsWith("developer") ||
            lower.startsWith("assistant") ||
            lower.startsWith("user") ||
            lower.startsWith("role:") ||
            lower.startsWith("formal:") ||
            lower.startsWith("format:") ||
            lower.startsWith("style:") ||
            lower.startsWith("tone:") ||
            lower.startsWith("output:") ||
            lower.startsWith("instruction") ||
            lower.contains("exactly three lines") ||
            lower.contains("return plain text") ||
            lower.contains("do not include") ||
            lower.contains("<think>") ||
            lower.contains("</think>") ||
            lower.contains("<analysis>") ||
            lower.contains("</analysis>")
    }

    private fun stripRecommendationLabel(value: String): String {
        return value
            .replace(
                Regex(
                    "^(priority|why now|why|next action|action|next|role|formal|format|style|tone|output)\\s*:\\s*",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .trim()
    }

    private fun isInstructionLeak(value: String): Boolean {
        val lower = value.trim().lowercase()
        return lower.isBlank() ||
            lower == "plain text" ||
            lower == "exactly three lines" ||
            lower.contains("exactly three lines") ||
            lower.contains("one short sentence") ||
            lower.contains("one concrete step") ||
            lower.contains("formatting instruction") ||
            lower.contains("do not include") ||
            lower.contains("return plain text")
    }

    private fun splitSentences(value: String): List<String> {
        return value
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun ensureSentence(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return trimmed
        return if (trimmed.last() in listOf('.', '!', '?')) trimmed else "$trimmed."
    }

    private fun parseModelMeetingExtraction(rawText: String): MeetingExtraction? {
        val json = extractJsonObject(rawText) ?: return null
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return null

        val tasksJson = root.optJSONArray("tasks") ?: JSONArray()
        val commitmentsJson = root.optJSONArray("commitments") ?: JSONArray()
        val decisionsJson = root.optJSONArray("decisions") ?: JSONArray()

        val tasks = buildList {
            for (index in 0 until tasksJson.length()) {
                val item = tasksJson.optJSONObject(index) ?: continue
                val title = item.optString("title").trim()
                if (title.isBlank()) continue

                val owner = item.optString("owner").trim().ifBlank { "You" }
                val deadline = item.optString("deadline").trim().let(::parseIsoDateOrNull)
                val dependencyNote = item.optString("dependencyNote").trim().ifBlank { null }
                val priority = item.optInt("priority", inferPriority(title.lowercase())).coerceIn(1, 10)

                expandTaskTitles(cleanActionTitle(title)).forEach { atomicTitle ->
                    add(
                        ExtractedTask(
                            title = atomicTitle.take(120),
                            owner = owner,
                            deadline = deadline,
                            dependencyNote = dependencyNote,
                            priority = priority
                        )
                    )
                }
            }
        }

        val commitments = buildList {
            for (index in 0 until commitmentsJson.length()) {
                val line = commitmentsJson.optString(index).trim()
                if (line.isNotBlank()) add(line)
            }
        }

        val decisions = buildList {
            for (index in 0 until decisionsJson.length()) {
                val line = decisionsJson.optString(index).trim()
                if (line.isNotBlank()) add(line)
            }
        }

        val summary = root.optString("summary").trim().ifBlank {
            "Extracted ${tasks.size} task(s) and ${commitments.size} commitment(s)."
        }

        return MeetingExtraction(
            summary = summary,
            extractedTasks = tasks.distinctBy { it.title.lowercase() },
            extractedCommitments = commitments.distinct(),
            extractedDecisions = decisions.distinct()
        )
    }

    private fun extractJsonObject(text: String): String? {
        val cleaned = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return cleaned.substring(start, end + 1)
    }

    private fun parseIsoDateOrNull(value: String): LocalDate? {
        if (value.isBlank() || value.equals("null", ignoreCase = true)) return null
        return runCatching { LocalDate.parse(value) }.getOrNull()
    }

    private fun inferOwner(line: String): String {
        val first = line.split(" ").firstOrNull().orEmpty()
        return if (first.isNotBlank() && first.first().isUpperCase()) first else "You"
    }

    private fun inferDependency(line: String): String? {
        val lower = line.lowercase()
        return when {
            "depends on" in lower -> line.substringAfter("depends on", "").trim().ifBlank { null }
            "blocked by" in lower -> line.substringAfter("blocked by", "").trim().ifBlank { null }
            "waiting for" in lower -> line.substringAfter("waiting for", "").trim().ifBlank { null }
            "after " in lower -> line.substringAfter("after", "").trim().ifBlank { null }
            "once" in lower -> line.substringAfter("once", "").trim().ifBlank { null }
            else -> null
        }
    }

    private fun inferDate(lowerLine: String): LocalDate? {
        return when {
            "today" in lowerLine -> LocalDate.now()
            "end of day" in lowerLine || "eod" in lowerLine -> LocalDate.now()
            "tomorrow" in lowerLine -> LocalDate.now().plusDays(1)
            "this week" in lowerLine -> nextWeekday(5)
            "next week" in lowerLine -> LocalDate.now().plusWeeks(1)
            "friday" in lowerLine -> nextWeekday(5)
            "monday" in lowerLine -> nextWeekday(1)
            "tuesday" in lowerLine -> nextWeekday(2)
            "wednesday" in lowerLine -> nextWeekday(3)
            "thursday" in lowerLine -> nextWeekday(4)
            "saturday" in lowerLine -> nextWeekday(6)
            "sunday" in lowerLine -> nextWeekday(7)
            else -> null
        }
    }

    private fun inferPriority(lowerLine: String): Int {
        return when {
            "urgent" in lowerLine || "critical" in lowerLine || "important" in lowerLine -> 10
            "client" in lowerLine || "revenue" in lowerLine || "payment" in lowerLine || "security" in lowerLine || "production" in lowerLine || "launch" in lowerLine -> 10
            "blocked" in lowerLine || "depends on" in lowerLine || "waiting" in lowerLine -> 9
            "today" in lowerLine || "tomorrow" in lowerLine || "demo" in lowerLine -> 9
            "this week" in lowerLine || "next week" in lowerLine -> 8
            "should" in lowerLine || "need to" in lowerLine || "have to" in lowerLine -> 8
            else -> 7
        }
    }

    private fun nextWeekday(targetIsoDay: Int): LocalDate {
        var date = LocalDate.now().plusDays(1)
        while (date.dayOfWeek.value != targetIsoDay) {
            date = date.plusDays(1)
        }
        return date
    }
}
