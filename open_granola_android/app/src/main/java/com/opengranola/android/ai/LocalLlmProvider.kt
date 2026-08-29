package com.opengranola.android.ai

import android.content.Context
import com.geniex.sdk.GenieXSdk
import com.geniex.sdk.LlmWrapper
import com.geniex.sdk.bean.ChatMessage
import com.geniex.sdk.bean.GenerationConfig
import com.geniex.sdk.bean.LlmCreateInput
import com.geniex.sdk.bean.LlmStreamResult
import com.geniex.sdk.bean.ModelConfig
import com.geniex.sdk.bean.RuntimeIdValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Stable boundary for local inference. GenieX is wired behind this interface. */
interface LocalLlmProvider {
    val name: String
    suspend fun summarize(transcript: String, userNotes: String, context: String = ""): String
}

class GenieXLocalLlmProvider(context: Context) : LocalLlmProvider {
    override val name: String = "GenieX (on-device)"
    private val appContext = context.applicationContext
    private val modelStore = LocalModelStore(appContext)
    // The native wrapper is not re-entrant. This also prevents two meeting
    // summaries from interleaving tokens and corrupting each other's output.
    private val inferenceLock = Mutex()
    private var llm: LlmWrapper? = null
    private var initialized: CompletableDeferred<Result<Unit>>? = null

    override suspend fun summarize(transcript: String, userNotes: String, context: String): String {
        return inferenceLock.withLock {
            val activeLlm = ensureLlm()
            val prompt = """
                You are a private meeting assistant. Summarize the transcript faithfully.
                Return these sections: Overview, Decisions, Action items, Open questions.
                Do not invent details. If a section has no evidence, write “None noted”.
                User notes: $userNotes
                Optional phone context (treat as background only; do not invent connections): $context

                Transcript:
                $transcript
            """.trimIndent()
            // Match the Geniex demo flow: let the selected model format the
            // conversation instead of hand-building a model-specific prompt.
            val formattedPrompt = activeLlm.applyChatTemplate(
                arrayOf(ChatMessage("user", prompt)),
                null,
                false,
                false
            ).getOrThrow().formattedText
            val output = StringBuilder()
            val config = GenerationConfig().apply { maxTokens = 2048 }
            activeLlm.generateStreamFlow(formattedPrompt, config).collect { event ->
                when (event) {
                    is LlmStreamResult.Token -> output.append(event.text)
                    is LlmStreamResult.Completed -> Unit
                    is LlmStreamResult.Error -> throw event.throwable
                }
            }
            output.toString().trim().ifBlank { error("GenieX returned an empty summary") }
        }
    }

    private suspend fun ensureLlm(): LlmWrapper {
        llm?.let { return it }
        val model = modelStore.selected()
            ?: error("Load a GenieX-compatible local model before generating notes")
        check(model.length() > 0) { "Selected model file is empty" }

        val initResult = initialized ?: CompletableDeferred<Result<Unit>>().also { deferred ->
            initialized = deferred
            GenieXSdk.Companion.getInstance().init(appContext, object : GenieXSdk.InitCallback {
                override fun onSuccess() { deferred.complete(Result.success(Unit)) }
                override fun onFailure(message: String) { deferred.complete(Result.failure(IllegalStateException(message))) }
            })
        }
        initResult.await().getOrThrow()
        // Use a conservative CPU configuration. The phone's bundled ggml backend
        // was faulting in its parallel Q4 dispatch path inside an OpenMP worker.
        val modelConfig = ModelConfig().apply {
            nCtx = 2048
            nThreads = 1
            nThreadsBatch = 1
            nBatch = 256
            nUBatch = 128
            nSeqMax = 1
            nGpuLayers = 0
        }
        val input = LlmCreateInput(
            model.name,
            model.absolutePath,
            null,
            modelConfig,
            RuntimeIdValue.LLAMA_CPP.value,
            "cpu"
        )
        val created = LlmWrapper.builder().llmCreateInput(input).build().getOrThrow()
        llm = created
        return created
    }
}
