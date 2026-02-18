package com.example.radiogarden

import android.app.Application
import com.example.radiogarden.data.local.RadioDatabase
import com.example.radiogarden.data.preferences.UserPreferences
import com.example.radiogarden.data.remote.RadioGardenApi
import com.example.radiogarden.data.repository.RadioRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class RadioGardenApp : Application() {

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val database: RadioDatabase by lazy {
        RadioDatabase.getInstance(this)
    }

    val api: RadioGardenApi by lazy {
        RadioGardenApi(okHttpClient)
    }

    val repository: RadioRepository by lazy {
        RadioRepository(database.stationDao(), api)
    }

    val preferences: UserPreferences by lazy {
        UserPreferences(this)
    }
}
