package com.example.videodownloader.domain.usecase

import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.VideoInfo
import com.example.videodownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StartDownloadUseCaseTest {

    class FakeDownloadRepository : DownloadRepository {
        var capturedTask: DownloadTask? = null
        var dummyIdToReturn: Long = 0L

        override suspend fun extractVideoInfo(url: String): VideoInfo {
            throw NotImplementedError()
        }

        override suspend fun startDownload(task: DownloadTask): Long {
            capturedTask = task
            return dummyIdToReturn
        }

        override fun getDownloads(): Flow<List<DownloadTask>> {
            throw NotImplementedError()
        }

        override suspend fun updateDownloadStatus(
            id: Long,
            status: DownloadStatus,
            progress: Int,
            filePath: String?
        ) {
            throw NotImplementedError()
        }

        override suspend fun getDownloadTaskById(id: Long): DownloadTask? {
            throw NotImplementedError()
        }
    }

    @Test
    fun `invoke calls repository startDownload and returns expected id`() = runBlocking {
        // Arrange
        val fakeRepository = FakeDownloadRepository()
        val expectedId = 42L
        fakeRepository.dummyIdToReturn = expectedId
        val startDownloadUseCase = StartDownloadUseCase(fakeRepository)

        val taskToStart = DownloadTask(
            url = "http://example.com/video",
            title = "Test Video",
            quality = "1080p",
            status = DownloadStatus.PENDING
        )

        // Act
        val resultId = startDownloadUseCase(taskToStart)

        // Assert
        assertEquals(expectedId, resultId)
        assertEquals(taskToStart, fakeRepository.capturedTask)
    }
}
