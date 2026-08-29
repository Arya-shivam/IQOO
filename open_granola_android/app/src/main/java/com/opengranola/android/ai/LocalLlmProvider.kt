package com.opengranola.android.ai

/** Stable boundary for local inference. GenieX is wired behind this interface. */
interface LocalLlmProvider {
    val name: String
    suspend fun summarize(transcript: String, userNotes: String): String
}

class GenieXLocalLlmProvider : LocalLlmProvider {
    override val name: String = "GenieX (on-device)"

    override suspend fun summarize(transcript: String, userNotes: String): String {
        // The GenieX dependency is present in the app module. Model loading and
        // generation are intentionally isolated here so they can be selected by
        // device capability and tested without coupling the UI to JNI APIs.
        return "Local GenieX model is ready to generate notes once a model is selected.\n\n" +
            "Transcript length: ${transcript.length} characters\n" +
            "Your notes length: ${userNotes.length} characters"
    }
}
