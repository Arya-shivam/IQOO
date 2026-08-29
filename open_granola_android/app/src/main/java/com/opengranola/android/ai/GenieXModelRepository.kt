package com.opengranola.android.ai

import android.content.Context
import com.geniex.sdk.GenieXSdk
import com.geniex.sdk.ModelManagerWrapper
import com.geniex.sdk.bean.HubSource
import com.geniex.sdk.bean.ModelPullInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CompletableDeferred

data class GenieXCatalogModel(
    val id: String,
    val displayName: String,
    val modelName: String,
    val quant: String = "Q4_0"
)

/** Text-model catalog verified against the GenieX Android demo. */
class GenieXModelRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("pa_model", Context.MODE_PRIVATE)
    private var initialized: CompletableDeferred<Result<Unit>>? = null
    val catalog = listOf(
        GenieXCatalogModel("qwen3-0.6b", "Qwen3 0.6B · fast", "unsloth/Qwen3-0.6B-GGUF"),
        GenieXCatalogModel("qwen3-1.7b", "Qwen3 1.7B · richer", "unsloth/Qwen3-1.7B-GGUF"),
        GenieXCatalogModel("granite-micro", "Granite 4 Micro", "ibm-granite/granite-4.0-micro-GGUF"),
        GenieXCatalogModel("ministral-3b", "Ministral 3B · powerful", "unsloth/Ministral-3-3B-Instruct-2512-GGUF"),
        GenieXCatalogModel("phi-4-mini", "Phi-4 Mini · reasoning", "bartowski/microsoft_Phi-4-mini-instruct-GGUF"),
        GenieXCatalogModel("gemma-4-e4b", "Gemma 4 E4B · advanced", "google/gemma-4-E4B-it-qat-q4_0-gguf"),
        GenieXCatalogModel("gpt-oss-20b", "GPT-OSS 20B · experimental", "unsloth/gpt-oss-20b-GGUF")
    )

    suspend fun paths(model: GenieXCatalogModel) = ensureInitialized().let { ModelManagerWrapper.getPaths(model.modelName) }

    fun rememberSelected(model: GenieXCatalogModel, computeUnit: String) {
        preferences.edit()
            .putString(KEY_SELECTED, model.id)
            .putString(KEY_COMPUTE_UNIT, computeUnit)
            .apply()
    }

    fun selected(): GenieXCatalogModel? = preferences.getString(KEY_SELECTED, null)
        ?.let { id -> catalog.firstOrNull { it.id == id } }

    fun selectedComputeUnit(): String = preferences.getString(KEY_COMPUTE_UNIT, "cpu") ?: "cpu"

    fun download(model: GenieXCatalogModel): Flow<ModelManagerWrapper.PullEvent> =
        flow {
            ensureInitialized()
            emitAll(ModelManagerWrapper.pullFlow(
                ModelPullInput(
                    model_name = model.modelName,
                    precision = model.quant,
                    hub = HubSource.HUGGINGFACE
                )
            ))
        }

    private suspend fun ensureInitialized() {
        val result = initialized ?: CompletableDeferred<Result<Unit>>().also { deferred ->
            initialized = deferred
            GenieXSdk.getInstance().init(appContext, object : GenieXSdk.InitCallback {
                override fun onSuccess() { deferred.complete(Result.success(Unit)) }
                override fun onFailure(reason: String) { deferred.complete(Result.failure(IllegalStateException(reason))) }
            })
        }
        result.await().getOrThrow()
    }

    companion object {
        private const val KEY_SELECTED = "selected_catalog_model"
        private const val KEY_COMPUTE_UNIT = "selected_compute_unit"
    }
}
