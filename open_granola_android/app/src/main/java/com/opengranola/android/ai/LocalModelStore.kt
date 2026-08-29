package com.opengranola.android.ai

import android.content.Context
import android.net.Uri
import java.io.File

/** Stores user-supplied local model artifacts without uploading them anywhere. */
class LocalModelStore(context: Context) {
    private val directory = File(context.filesDir, "models").apply { mkdirs() }
    private val preferences = context.getSharedPreferences("local_models", Context.MODE_PRIVATE)

    fun list(): List<File> = directory.listFiles()
        ?.filter { it.isFile }
        ?.sortedBy { it.name.lowercase() }
        ?: emptyList()

    fun selected(): File? = preferences.getString(KEY_SELECTED, null)
        ?.let(::File)
        ?.takeIf { it.exists() }

    fun import(uri: Uri, displayName: String?, resolver: android.content.ContentResolver): File {
        val safeName = (displayName ?: "model.bin").replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destination = File(directory, safeName).let { candidate ->
            if (!candidate.exists()) candidate else File(directory, "${candidate.nameWithoutExtension}-${System.currentTimeMillis()}.${candidate.extension}")
        }
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected model" }
            destination.outputStream().use { output -> input.copyTo(output) }
        }
        preferences.edit().putString(KEY_SELECTED, destination.absolutePath).apply()
        return destination
    }

    companion object { private const val KEY_SELECTED = "selected_model" }
}
