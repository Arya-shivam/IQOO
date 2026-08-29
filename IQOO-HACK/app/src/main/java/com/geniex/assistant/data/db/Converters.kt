package com.geniex.assistant.data.db

import androidx.room.TypeConverter
import com.geniex.assistant.model.GoalStatus
import com.geniex.assistant.model.MemoryType
import com.geniex.assistant.model.TaskStatus

class Converters {
    @TypeConverter
    fun goalStatusFromString(value: String): GoalStatus = GoalStatus.valueOf(value)

    @TypeConverter
    fun goalStatusToString(value: GoalStatus): String = value.name

    @TypeConverter
    fun taskStatusFromString(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun taskStatusToString(value: TaskStatus): String = value.name

    @TypeConverter
    fun memoryTypeFromString(value: String): MemoryType = MemoryType.valueOf(value)

    @TypeConverter
    fun memoryTypeToString(value: MemoryType): String = value.name
}
