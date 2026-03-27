package com.example.videodownloader.domain.usecase

import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.VideoInfo
import com.example.videodownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDownloadsUseCaseTest {

    class FakeDownloadRepository : DownloadRepository {
        var dummyFlow: Flow<List<DownloadTask>> = flowOf(emptyList())

        override suspend fun extractVideoInfo(url: String): VideoInfo {
            throw NotImplementedError()
        }

        override suspend fun startDownload(task: DownloadTask): Long {
            throw NotImplementedError()
        }

        override fun getDownloads(): Flow<List<DownloadTask>> {
            return dummyFlow
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
    fun `invoke returns flow from repository`() = runBlocking {
        // Arrange
        val fakeRepository = FakeDownloadRepository()
        val mockTasks = listOf(
            DownloadTask(
                id = 1L,
                url = "http://example.com/video",
                title = "Test Video",
                quality = "720p",
                progress = 50,
                status = DownloadStatus.DOWNLOADING
            )
        )
        fakeRepository.dummyFlow = flowOf(mockTasks)
        val getDownloadsUseCase = GetDownloadsUseCase(fakeRepository)

        // Act
        val resultFlow = getDownloadsUseCase()
        val resultList = resultFlow.first()

        // Assert
        assertEquals(1, resultList.size)
        assertEquals("Test Video", resultList[0].title)
        assertEquals(mockTasks, resultList)
    }
}
