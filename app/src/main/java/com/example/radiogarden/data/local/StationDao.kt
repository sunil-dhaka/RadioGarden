package com.example.radiogarden.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Query("SELECT * FROM stations ORDER BY addedAt ASC")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE channelId = :channelId LIMIT 1")
    suspend fun getByChannelId(channelId: String): StationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: StationEntity): Long

    @Query("DELETE FROM stations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
