package com.pawedcat.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pawedcat_settings")

interface SettingsRepository {
    val downloadOnWifiOnlyFlow: Flow<Boolean>
    val defaultSleepTimerMinutesFlow: Flow<Int>
    val playbackSpeedFlow: Flow<Float>

    suspend fun setDownloadOnWifiOnly(wifiOnly: Boolean)
    suspend fun setDefaultSleepTimerMinutes(minutes: Int)
    suspend fun setPlaybackSpeed(speed: Float)
}

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    private val keyWifiOnly = booleanPreferencesKey("download_wifi_only")
    private val keySleepTimer = intPreferencesKey("default_sleep_timer_minutes")
    private val keyPlaybackSpeed = floatPreferencesKey("playback_speed")

    override val downloadOnWifiOnlyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[keyWifiOnly] ?: true // Default: Wi-Fi only
    }

    override val defaultSleepTimerMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[keySleepTimer] ?: 30
    }

    override val playbackSpeedFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[keyPlaybackSpeed] ?: 1.0f
    }

    override suspend fun setDownloadOnWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[keyWifiOnly] = wifiOnly
        }
    }

    override suspend fun setDefaultSleepTimerMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[keySleepTimer] = minutes
        }
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[keyPlaybackSpeed] = speed
        }
    }
}
