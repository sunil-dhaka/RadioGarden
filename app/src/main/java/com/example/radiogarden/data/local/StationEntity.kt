package com.example.radiogarden.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stations",
    indices = [Index(value = ["channelId"], unique = true)]
)
data class StationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val name: String,
    val place: String,
    val country: String,
    val streamUrl: String,
    val website: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)
