package com.geniex.assistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Insert
    suspend fun insert(meeting: MeetingEntity): Long

    @Query("SELECT * FROM meetings ORDER BY createdAtEpochMs DESC")
    fun observeMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT COUNT(*) FROM meetings")
    suspend fun countMeetings(): Int
}
