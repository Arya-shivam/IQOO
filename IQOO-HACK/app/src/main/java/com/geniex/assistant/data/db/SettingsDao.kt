package com.geniex.assistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: SettingEntity)

    @Query("SELECT * FROM assistant_settings WHERE key = :key")
    suspend fun getByKey(key: String): SettingEntity?

    @Query("SELECT * FROM assistant_settings")
    fun observeAll(): Flow<List<SettingEntity>>
}
