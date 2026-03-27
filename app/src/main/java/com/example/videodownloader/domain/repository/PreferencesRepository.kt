package com.example.videodownloader.domain.repository


import com.example.videodownloader.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun updateDownloadLocation(location: String)
    suspend fun updateDefaultQuality(quality: String)
    suspend fun updateDarkTheme(isDarkTheme: Boolean)
    suspend fun clearCache()
}
