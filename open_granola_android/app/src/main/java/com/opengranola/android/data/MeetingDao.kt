package com.opengranola.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(meeting: MeetingEntity): Long

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun delete(id: String)
}
