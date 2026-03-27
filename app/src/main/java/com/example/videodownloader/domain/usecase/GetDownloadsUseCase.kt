package com.example.videodownloader.domain.usecase

import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadsUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(): Flow<List<DownloadTask>> {
        return repository.getDownloads()
    }
}
