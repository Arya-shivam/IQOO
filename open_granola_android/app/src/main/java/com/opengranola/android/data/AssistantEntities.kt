package com.opengranola.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val text: String,
    val source: String,
    val importance: Float,
    val tags: String,
    val createdAt: Long,
    val lastUsedAt: Long,
    val archived: Boolean = false
)

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val objective: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "plan_tasks")
data class PlanTaskEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val title: String,
    val details: String,
    val status: String,
    val priority: Int,
    val position: Int
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val createdAt: Long
)

@Entity(tableName = "context_events")
data class ContextEventEntity(
    @PrimaryKey val id: String,
    val source: String,
    val type: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val importance: Float
)

@Entity(tableName = "context_snapshots")
data class ContextSnapshotEntity(
    @PrimaryKey val id: String,
    val purpose: String,
    val renderedContext: String,
    val sourceIds: String,
    val createdAt: Long
)

@Entity(tableName = "commitments")
data class CommitmentEntity(
    @PrimaryKey val id: String,
    val meetingId: String,
    val sourceTitle: String,
    val title: String,
    val owner: String,
    val dueText: String,
    val evidence: String,
    val confidence: Float,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "daily_insights")
data class DailyInsightEntity(
    @PrimaryKey val date: String,
    val briefing: String,
    val contextSnapshotId: String,
    val feedback: Int,
    val createdAt: Long
)
