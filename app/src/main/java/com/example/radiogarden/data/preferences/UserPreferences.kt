package com.example.radiogarden.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "radio_prefs")

class UserPreferences(private val context: Context) {

    private val defaultStationKey = stringPreferencesKey("default_station_id")
    private val firstLaunchKey = booleanPreferencesKey("is_first_launch")

    val defaultStationId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[defaultStationKey]
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[firstLaunchKey] != false
    }

    suspend fun setDefaultStation(channelId: String) {
        context.dataStore.edit { prefs ->
            prefs[defaultStationKey] = channelId
        }
    }

    suspend fun markFirstLaunchDone() {
        context.dataStore.edit { prefs ->
            prefs[firstLaunchKey] = false
        }
    }
}
