package com.example.videodownloader.domain.usecase

import com.example.videodownloader.domain.model.VideoInfo
import com.example.videodownloader.domain.repository.DownloadRepository
import javax.inject.Inject

class ExtractVideoUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(url: String): VideoInfo {
        return repository.extractVideoInfo(url)
    }
}
