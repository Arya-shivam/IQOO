package com.geniex.assistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert
    suspend fun insert(memory: MemoryEntity): Long

    @Insert
    suspend fun insertAll(memories: List<MemoryEntity>): List<Long>

    @Query("SELECT * FROM memories ORDER BY importanceScore DESC, createdAtEpochMs DESC")
    fun observeMemories(): Flow<List<MemoryEntity>>
}
