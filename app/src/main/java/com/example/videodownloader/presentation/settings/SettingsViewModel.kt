package com.example.videodownloader.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.videodownloader.domain.model.UserPreferences
import com.example.videodownloader.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateDownloadLocation(location: String) {
        viewModelScope.launch {
            preferencesRepository.updateDownloadLocation(location)
        }
    }

    fun updateDefaultQuality(quality: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultQuality(quality)
        }
    }

    fun updateDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDarkTheme(isDarkTheme)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            preferencesRepository.clearCache()
        }
    }
}
