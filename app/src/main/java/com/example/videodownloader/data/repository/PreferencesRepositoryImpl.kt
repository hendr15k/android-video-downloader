package com.example.videodownloader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.example.videodownloader.domain.model.UserPreferences
import com.example.videodownloader.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>, private val context: Context) : PreferencesRepository {

    private object PreferencesKeys {
        val DOWNLOAD_LOCATION = stringPreferencesKey("download_location")
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val downloadLocation = preferences[PreferencesKeys.DOWNLOAD_LOCATION] ?: ""
            val defaultQuality = preferences[PreferencesKeys.DEFAULT_QUALITY] ?: "720p"
            val darkTheme = preferences[PreferencesKeys.DARK_THEME] ?: false

            UserPreferences(downloadLocation, defaultQuality, darkTheme)
        }

    override suspend fun updateDownloadLocation(location: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOWNLOAD_LOCATION] = location
        }
    }

    override suspend fun updateDefaultQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_QUALITY] = quality
        }
    }

    override suspend fun updateDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME] = isDarkTheme
        }
    }

    override suspend fun clearCache() {
        // Simple cache clear implementation: clear the application's cache directory
        val cacheDir = context.cacheDir
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            cacheDir.mkdir()
        }
        // Also clear external cache directory if available
        val externalCacheDir = context.externalCacheDir
        if (externalCacheDir != null && externalCacheDir.exists()) {
            externalCacheDir.deleteRecursively()
            externalCacheDir.mkdir()
        }
    }
}
