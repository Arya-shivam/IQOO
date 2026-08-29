package com.geniex.assistant.llm

import android.content.Context
import java.io.File

object ModelConfig {
    const val KEY_MODEL_DIRECTORY = "model_directory"
    const val KEY_ACTIVE_RUNTIME = "active_runtime"

    const val DEFAULT_RUNTIME = "GenieX + Qwen"

    fun defaultInternalModelDirectory(context: Context): String {
        val directory = File(context.filesDir, "models")
        directory.mkdirs()
        return directory.absolutePath
    }
}
