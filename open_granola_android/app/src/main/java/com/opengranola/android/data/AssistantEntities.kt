package com.opengranola.android.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
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
    val position: Int,
    @ColumnInfo(defaultValue = "15") val estimatedMinutes: Int = 15,
    @ColumnInfo(defaultValue = "0") val startedAt: Long = 0,
    @ColumnInfo(defaultValue = "0") val completedAt: Long = 0,
    @ColumnInfo(defaultValue = "''") val completionNote: String = "",
    @ColumnInfo(defaultValue = "''") val completionCredibility: String = ""
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

@Entity(tableName = "goals", indices = [Index(value = ["status"])])
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "actions", indices = [Index(value = ["occurredAt"]), Index(value = ["source", "sourceId"], unique = true)])
data class ActionEntity(
    @PrimaryKey val id: String,
    val source: String,
    val sourceId: String,
    val type: String,
    val title: String,
    val summary: String,
    val tags: String,
    val importance: Float,
    val linkStatus: String,
    val occurredAt: Long,
    val createdAt: Long
)

@Entity(tableName = "graph_nodes", indices = [Index(value = ["type"]), Index(value = ["sourceId"])])
data class GraphNodeEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val details: String,
    val tags: String,
    val status: String,
    val sourceId: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "graph_edges",
    indices = [Index(value = ["fromType", "fromId", "toType", "toId", "type"], unique = true)]
)
data class GraphEdgeEntity(
    @PrimaryKey val id: String,
    val fromType: String,
    val fromId: String,
    val toType: String,
    val toId: String,
    val type: String,
    val confidence: Float,
    val evidence: String,
    val createdAt: Long
)

@Entity(tableName = "curation_queue", indices = [Index(value = ["status", "createdAt"])])
data class CurationQueueEntity(
    @PrimaryKey val id: String,
    val source: String,
    val sourceId: String,
    val title: String,
    val content: String,
    val occurredAt: Long,
    val status: String,
    val attempts: Int,
    val lastError: String,
    val createdAt: Long
)
