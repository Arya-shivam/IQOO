package com.geniex.assistant.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.geniex.assistant.model.GoalStatus
import com.geniex.assistant.model.MemoryType
import com.geniex.assistant.model.TaskStatus

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val why: String,
    val deadlineEpochDay: Long,
    val status: GoalStatus,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"]), Index(value = ["dependencyTaskId"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val details: String,
    val status: TaskStatus,
    val priority: Int,
    val owner: String,
    val deadlineEpochDay: Long?,
    val dependencyTaskId: Long?,
    val blockedReason: String?,
    val estimatedMinutes: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val transcript: String,
    val summary: String,
    val createdAtEpochMs: Long
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: MemoryType,
    val content: String,
    val importanceScore: Int,
    val relatedGoalId: Long?,
    val relatedTaskId: Long?,
    val createdAtEpochMs: Long
)

@Entity(tableName = "assistant_settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAtEpochMs: Long
)
