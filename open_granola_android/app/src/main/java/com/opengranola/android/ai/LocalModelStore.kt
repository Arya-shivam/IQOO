package com.opengranola.android.ai

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

class LocalModelStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("models", Context.MODE_PRIVATE)
    private val directory = File(appContext.filesDir, "models").apply { mkdirs() }

    fun selected(): File? = prefs.getString("selected", null)?.let(::File)?.takeIf(File::exists)

    fun import(uri: Uri, name: String?, resolver: ContentResolver): File {
        val safeName = (name ?: "model.gguf").replace(Regex("[^A-Za-z0-9._-]"), "_")
        val target = File(directory, safeName)
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open selected model" }
            target.outputStream().use(input::copyTo)
        }
        prefs.edit().putString("selected", target.absolutePath).apply()
        return target
    }
}
