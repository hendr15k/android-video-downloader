package com.example.videodownloader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo
import com.example.videodownloader.domain.usecase.ExtractVideoUseCase
import com.example.videodownloader.domain.usecase.GetDownloadsUseCase
import com.example.videodownloader.domain.usecase.StartDownloadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val extractVideoUseCase: ExtractVideoUseCase,
    private val startDownloadUseCase: StartDownloadUseCase,
    getDownloadsUseCase: GetDownloadsUseCase
) : ViewModel() {

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _videoInfo = MutableStateFlow<VideoInfo?>(null)
    val videoInfo: StateFlow<VideoInfo?> = _videoInfo.asStateFlow()

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val downloads = getDownloadsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onUrlChange(newUrl: String) {
        _url.value = newUrl
        _error.value = null
    }

    fun extractVideo() {
        val currentUrl = _url.value
        if (currentUrl.isBlank()) {
            _error.value = "URL cannot be empty"
            return
        }
        viewModelScope.launch {
            _isExtracting.value = true
            _error.value = null
            try {
                _videoInfo.value = extractVideoUseCase(currentUrl)
            } catch (e: Exception) {
                _error.value = "Failed to extract video: ${e.message}"
            } finally {
                _isExtracting.value = false
            }
        }
    }

    fun startDownload(quality: Quality) {
        val info = _videoInfo.value ?: return
        val task = DownloadTask(
            url = quality.downloadUrl,
            title = info.title,
            quality = quality.resolution
        )
        viewModelScope.launch {
            startDownloadUseCase(task)
            _videoInfo.value = null
            _url.value = ""
        }
    }

    fun dismissVideoInfo() {
        _videoInfo.value = null
    }
}
