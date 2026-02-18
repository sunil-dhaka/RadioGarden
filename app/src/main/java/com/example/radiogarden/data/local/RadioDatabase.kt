package com.example.radiogarden.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StationEntity::class], version = 1, exportSchema = false)
abstract class RadioDatabase : RoomDatabase() {

    abstract fun stationDao(): StationDao

    companion object {
        @Volatile
        private var INSTANCE: RadioDatabase? = null

        fun getInstance(context: Context): RadioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RadioDatabase::class.java,
                    "radio_garden.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
