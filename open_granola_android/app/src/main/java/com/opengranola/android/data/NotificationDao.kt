package com.opengranola.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY postedAt DESC LIMIT 20")
    fun observeRecent(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY postedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE postedAt >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun clear()
}
