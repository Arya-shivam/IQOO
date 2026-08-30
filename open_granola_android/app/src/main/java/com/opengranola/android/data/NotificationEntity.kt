package com.opengranola.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val body: String,
    val postedAt: Long
)
