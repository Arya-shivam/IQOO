package com.opengranola.android.ai

import android.content.Context
import com.geniex.sdk.GenieXSdk
import com.geniex.sdk.LlmWrapper
import com.geniex.sdk.bean.ChatMessage
import com.geniex.sdk.bean.ComputeUnitValue
import com.geniex.sdk.bean.GenerationConfig
import com.geniex.sdk.bean.LlmCreateInput
import com.geniex.sdk.bean.LlmStreamResult
import com.geniex.sdk.bean.ModelConfig
import com.geniex.sdk.bean.RuntimeIdValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import org.json.JSONArray

/** Stable boundary for local inference. GenieX is wired behind this interface. */
interface LocalLlmProvider {
    val name: String
    suspend fun summarize(transcript: String, userNotes: String, context: String = ""): String
    suspend fun chat(message: String, context: String, history: List<AssistantTurn>, onToken: suspend (String) -> Unit = {}): InferenceResult
    suspend fun generatePlan(objective: String, context: String): GeneratedPlan
    suspend fun extractCommitments(meetingTitle: String, transcript: String, summary: String): List<GeneratedCommitment>
    suspend fun generateDailyBriefing(context: String): InferenceResult
}

data class AssistantTurn(val role: String, val content: String)
data class GeneratedTask(val title: String, val details: String, val priority: Int)
data class GeneratedPlan(val title: String, val objective: String, val tasks: List<GeneratedTask>)
data class GeneratedCommitment(
    val title: String,
    val owner: String,
    val dueText: String,
    val evidence: String,
    val confidence: Float
)
data class InferenceStats(
    val promptTokens: Int,
    val generatedTokens: Int,
    val computeUnit: String,
    val prefillTokensPerSecond: Double,
    val decodeTokensPerSecond: Double
) { val totalTokens: Int get() = promptTokens + generatedTokens }
data class InferenceResult(val text: String, val stats: InferenceStats)

class GenieXLocalLlmProvider(context: Context) : LocalLlmProvider {
    override val name: String = "GenieX (on-device)"
    private val appContext = context.applicationContext
    private val modelStore = LocalModelStore(appContext)
    // The native wrapper is not re-entrant. This also prevents two meeting
    // summaries from interleaving tokens and corrupting each other's output.
    private val inferenceLock = Mutex()
    private var llm: LlmWrapper? = null
    private var initialized: CompletableDeferred<Result<Unit>>? = null
    private var managedModel: ManagedModel? = null

    fun useManagedModel(model: ManagedModel) {
        managedModel = model
        llm = null
    }

    /** Warm the native runtime before the first user request. */
    suspend fun preload(): Result<Unit> = inferenceLock.withLock {
        runCatching { ensureLlm(); Unit }
    }

    override suspend fun summarize(transcript: String, userNotes: String, context: String): String {
        val prompt = """
            You are pa, a private on-device meeting assistant. Summarize faithfully.
            Return: Overview, Decisions, Action items, Open questions.
            Do not invent details. If a section has no evidence, write “None noted”.
            User notes: $userNotes

            Transcript:
            $transcript
        """.trimIndent()
        return generate(arrayOf(ChatMessage("user", prompt)), 2048).text
    }

    override suspend fun chat(message: String, context: String, history: List<AssistantTurn>, onToken: suspend (String) -> Unit): InferenceResult {
        val messages = history.takeLast(4).map { ChatMessage(it.role, it.content) }.toMutableList()
        messages += ChatMessage("user", """
            You are pa, a private local personal assistant. Use only relevant context below.
            Be concise, acknowledge uncertainty, and never claim an action was executed.

            LOCAL CONTEXT:
            $context

            USER MESSAGE:
            $message
        """.trimIndent())
        return generate(messages.toTypedArray(), 768, onToken)
    }

    override suspend fun generatePlan(objective: String, context: String): GeneratedPlan {
        val raw = generate(arrayOf(ChatMessage("user", """
            You are pa. Create a practical plan using relevant local context.
            Return only JSON with this shape:
            {"title":"...","objective":"...","tasks":[{"title":"...","details":"...","priority":1}]}
            Use 3-7 concrete tasks. Priority 1 is highest. Do not include markdown fences.

            Objective: $objective
            Local context: $context
        """.trimIndent())), 1536).text
        return parsePlan(raw, objective)
    }

    override suspend fun extractCommitments(
        meetingTitle: String,
        transcript: String,
        summary: String
    ): List<GeneratedCommitment> {
        val raw = generate(arrayOf(ChatMessage("user", """
            You are pa. Extract only explicit commitments, promises, assigned action items, and deadlines from this meeting.
            Return only a JSON array with this shape:
            [{"title":"...","owner":"...","due":"...","evidence":"exact short supporting phrase","confidence":0.0}]
            Use an empty string when owner or due date is not stated. Confidence must be between 0 and 1.
            Do not infer tasks that were not actually agreed. Return [] if there are none. Do not include markdown.

            Meeting: $meetingTitle
            Summary: ${summary.take(3500)}
            Transcript excerpt: ${transcript.take(5000)}
        """.trimIndent())), 1024).text
        return parseCommitments(raw).map { commitment ->
            if (isEvidenceVerified(commitment.evidence, transcript, summary)) {
                commitment
            } else {
                commitment.copy(evidence = "", confidence = commitment.confidence.coerceAtMost(.45f))
            }
        }
    }

    override suspend fun generateDailyBriefing(context: String): InferenceResult {
        return generate(arrayOf(ChatMessage("user", """
            You are pa, a private intent-to-reality assistant. Create a concise daily briefing from the local context.
            Compare open commitments and plans with notification and app-usage signals. Be supportive, factual, and never judgmental.
            Write exactly three short sections: Focus, Reality check, Next step.
            Do not invent deadlines or claim an action was completed. Keep the whole response under 130 words.

            LOCAL CONTEXT:
            $context
        """.trimIndent())), 512)
    }

    private suspend fun generate(
        messages: Array<ChatMessage>,
        maxTokens: Int,
        onToken: suspend (String) -> Unit = {}
    ): InferenceResult = inferenceLock.withLock {
        val activeLlm = ensureLlm()
        val formatted = activeLlm.applyChatTemplate(messages, null, false, false).getOrThrow().formattedText
        val output = StringBuilder()
        var promptTokens = 0
        var generatedTokens = 0
        var prefillSpeed = 0.0
        var decodeSpeed = 0.0
        activeLlm.generateStreamFlow(formatted, GenerationConfig().apply { this.maxTokens = maxTokens }).collect { event ->
            when (event) {
                is LlmStreamResult.Token -> {
                    output.append(event.text)
                    onToken(event.text)
                }
                is LlmStreamResult.Completed -> {
                    promptTokens = event.profile.promptTokens.toInt()
                    generatedTokens = event.profile.generatedTokens.toInt()
                    prefillSpeed = event.profile.prefillSpeed.toDouble()
                    decodeSpeed = event.profile.decodingSpeed.toDouble()
                }
                is LlmStreamResult.Error -> throw event.throwable
            }
        }
        val text = sanitizeModelOutput(output.toString())
            .ifBlank { error("GenieX returned an empty response") }
        InferenceResult(text, InferenceStats(promptTokens, generatedTokens, managedModel?.computeUnit ?: "cpu", prefillSpeed, decodeSpeed))
    }

    /**
     * Some reasoning-capable chat templates emit thinking delimiters even when
     * thinking is disabled. Keep those implementation tokens out of every UI
     * consumer (chat, summaries, and JSON plan parsing).
     */
    private fun sanitizeModelOutput(raw: String): String {
        return raw
            .replace(THINK_BLOCK, "")
            .replace(LEADING_THINK_CLOSE, "")
            .replace(SPECIAL_TOKEN, "")
            .replace(LEADING_ROLE_PREFIX, "")
            .trim()
    }

    private fun parsePlan(raw: String, objective: String): GeneratedPlan {
        val jsonText = raw.substringAfter('{', "").let { if (it.isBlank()) "" else "{$it" }.substringBeforeLast('}', "").let { if (it.isBlank()) "" else "$it}" }
        return runCatching {
            val json = JSONObject(jsonText)
            val array = json.getJSONArray("tasks")
            val tasks = (0 until array.length()).map { index ->
                val task = array.getJSONObject(index)
                GeneratedTask(task.getString("title"), task.optString("details"), task.optInt("priority", index + 1))
            }
            GeneratedPlan(json.optString("title", "Plan"), json.optString("objective", objective), tasks)
        }.getOrElse {
            GeneratedPlan("Plan for ${objective.take(48)}", objective, listOf(GeneratedTask("Review the generated plan", raw.take(1200), 1)))
        }
    }

    private fun parseCommitments(raw: String): List<GeneratedCommitment> {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        require(start >= 0 && end >= start) { "The model did not return a commitment list" }
        val array = JSONArray(raw.substring(start, end + 1))
        return (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val title = item.optString("title").trim()
            if (title.isBlank()) return@mapNotNull null
            GeneratedCommitment(
                title = title.take(240),
                owner = item.optString("owner").trim().take(100),
                dueText = item.optString("due").trim().take(100),
                evidence = item.optString("evidence").trim().take(500),
                confidence = item.optDouble("confidence", .6).toFloat().coerceIn(0f, 1f)
            )
        }
    }

    private fun isEvidenceVerified(evidence: String, transcript: String, summary: String): Boolean {
        if (evidence.isBlank()) return false
        fun normalize(value: String) = value.lowercase().replace(Regex("\\s+"), " ").trim()
        val needle = normalize(evidence)
        return needle.length >= 8 && (normalize(transcript).contains(needle) || normalize(summary).contains(needle))
    }

    private suspend fun ensureLlm(): LlmWrapper {
        llm?.let { return it }
        val managed = managedModel
        val model = modelStore.selected()
        check(managed != null || model != null) { "Load a GenieX-compatible local model before generating notes" }
        if (managed == null) check(model!!.length() > 0) { "Selected model file is empty" }

        val initResult = initialized ?: CompletableDeferred<Result<Unit>>().also { deferred ->
            initialized = deferred
            GenieXSdk.Companion.getInstance().init(appContext, object : GenieXSdk.InitCallback {
                override fun onSuccess() { deferred.complete(Result.success(Unit)) }
                override fun onFailure(message: String) { deferred.complete(Result.failure(IllegalStateException(message))) }
            })
        }
        initResult.await().getOrThrow()
        val isQairt = managed?.runtimeId.equals("qairt", ignoreCase = true)
        val computeUnit = if (isQairt) ComputeUnitValue.NPU.value else managed?.computeUnit ?: "cpu"
        val modelConfig = if (isQairt) {
            ModelConfig(nCtx = 0, nGpuLayers = 0)
        } else {
            ModelConfig().apply {
                nCtx = 2048
                nThreads = if (computeUnit == "cpu") 4 else 2
                nThreadsBatch = if (computeUnit == "cpu") 4 else 2
                nBatch = 256
                nUBatch = 128
                nSeqMax = 1
                nGpuLayers = when (computeUnit) {
                    "npu" -> 999
                    "gpu" -> 999
                    else -> 0
                }
            }
        }
        val input = LlmCreateInput(
            managed?.name ?: model!!.name,
            managed?.path ?: model!!.absolutePath,
            managed?.tokenizerPath,
            modelConfig,
            managed?.runtimeId ?: RuntimeIdValue.LLAMA_CPP.value,
            computeUnit
        )
        val created = LlmWrapper.builder().llmCreateInput(input).build().getOrThrow()
        llm = created
        return created
    }

    private companion object {
        val THINK_BLOCK = Regex("<think>.*?</think>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val LEADING_THINK_CLOSE = Regex("^\\s*</think>\\s*", RegexOption.IGNORE_CASE)
        val SPECIAL_TOKEN = Regex("<\\|[^>]+\\|>|\\[/?INST\\]", RegexOption.IGNORE_CASE)
        val LEADING_ROLE_PREFIX = Regex(
            "^\\s*(?:#{1,6}\\s*)?(?:assistant|model|system|user|pa)\\b(?:\\s*[:\\-]\\s*|\\s*\\n+|\\s+)",
            RegexOption.IGNORE_CASE
        )
    }
}

data class ManagedModel(
    val name: String,
    val path: String,
    val tokenizerPath: String?,
    val runtimeId: String,
    val computeUnit: String
)
