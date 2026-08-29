package com.geniex.assistant.llm

import com.geniex.assistant.model.ExtractedTask
import com.geniex.assistant.model.MeetingExtraction
import java.time.LocalDate

class GenieXQwenLocalBridge(
    private val modelDirectoryProvider: suspend () -> String?
) : LocalModelBridge {

    override suspend fun extractMeeting(transcript: String): MeetingExtraction {
        val modelPath = modelDirectoryProvider()
        val normalized = transcript.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val extractedTasks = mutableListOf<ExtractedTask>()
        val commitments = mutableListOf<String>()

        normalized.forEach { line ->
            val lower = line.lowercase()
            if (" will " in lower || lower.startsWith("will ")) {
                commitments += line
            }

            if (lower.contains("deadline") || lower.contains("by ") || lower.contains("demo")) {
                extractedTasks += ExtractedTask(
                    title = line.take(80),
                    owner = inferOwner(line),
                    deadline = inferDate(lower),
                    dependencyNote = inferDependency(line)
                )
            }
        }

        val summaryIntro = if (modelPath.isNullOrBlank()) {
            "Using fallback extractor (no local model path configured)."
        } else {
            "Prepared for local GenieX/Qwen inference at: $modelPath"
        }

        val summary = "$summaryIntro Extracted ${extractedTasks.size} task(s) and ${commitments.size} commitment(s)."

        return MeetingExtraction(
            summary = summary,
            extractedTasks = extractedTasks.distinctBy { it.title },
            extractedCommitments = commitments.distinct()
        )
    }

    override suspend fun generateRecommendation(context: String): String {
        val modelPath = modelDirectoryProvider()
        return if (modelPath.isNullOrBlank()) {
            "Model path is not set. I can still guide using rules: focus on nearest deadlines and blocked dependencies first."
        } else {
            "Model path is configured at $modelPath. Once runtime binding is added, GenieX/Qwen can replace this rule-based recommendation."
        }
    }

    private fun inferOwner(line: String): String {
        val first = line.split(" ").firstOrNull().orEmpty()
        return if (first.isNotBlank() && first.first().isUpperCase()) first else "You"
    }

    private fun inferDependency(line: String): String? {
        val lower = line.lowercase()
        return when {
            "depends on" in lower -> line.substringAfter("depends on", "").trim().ifBlank { null }
            "once" in lower -> line.substringAfter("once", "").trim().ifBlank { null }
            else -> null
        }
    }

    private fun inferDate(lowerLine: String): LocalDate? {
        return when {
            "today" in lowerLine -> LocalDate.now()
            "tomorrow" in lowerLine -> LocalDate.now().plusDays(1)
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

    private fun nextWeekday(targetIsoDay: Int): LocalDate {
        var date = LocalDate.now().plusDays(1)
        while (date.dayOfWeek.value != targetIsoDay) {
            date = date.plusDays(1)
        }
        return date
    }
}
