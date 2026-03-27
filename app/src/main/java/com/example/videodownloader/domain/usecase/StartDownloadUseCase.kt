package com.example.videodownloader.domain.usecase

import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.repository.DownloadRepository
import javax.inject.Inject

class StartDownloadUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(task: DownloadTask): Long {
        return repository.startDownload(task)
    }
}
