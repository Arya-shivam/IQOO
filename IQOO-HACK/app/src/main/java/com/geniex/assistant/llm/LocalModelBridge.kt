package com.geniex.assistant.llm

import com.geniex.assistant.model.ExtractedTask
import com.geniex.assistant.model.MeetingExtraction

interface LocalModelBridge {
    suspend fun extractMeeting(transcript: String): MeetingExtraction
    suspend fun generateRecommendation(context: String): String
}
