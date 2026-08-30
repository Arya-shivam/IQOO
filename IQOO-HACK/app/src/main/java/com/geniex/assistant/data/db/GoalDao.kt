package com.geniex.assistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("SELECT * FROM goals ORDER BY deadlineEpochDay ASC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?

    @Query("SELECT id FROM goals WHERE status = 'ACTIVE' ORDER BY updatedAtEpochMs DESC LIMIT 1")
    suspend fun getLatestActiveGoalId(): Long?

    @Query("SELECT id FROM goals WHERE status = 'ACTIVE' AND LOWER(title) = LOWER(:title) LIMIT 1")
    suspend fun getActiveGoalIdByTitle(title: String): Long?

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun countGoals(): Int
}
