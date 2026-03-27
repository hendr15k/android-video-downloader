package com.example.videodownloader.presentation.settings

import com.example.videodownloader.domain.model.UserPreferences
import com.example.videodownloader.domain.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val fakePreferencesRepository = FakePreferencesRepository()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(fakePreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateDownloadLocation_callsRepository() = runTest {
        viewModel.updateDownloadLocation("/new/location")
        assertEquals("/new/location", fakePreferencesRepository.downloadLocation)
    }

    @Test
    fun updateDefaultQuality_callsRepository() = runTest {
        viewModel.updateDefaultQuality("1080p")
        assertEquals("1080p", fakePreferencesRepository.defaultQuality)
    }

    @Test
    fun updateDarkTheme_callsRepository() = runTest {
        viewModel.updateDarkTheme(true)
        assertEquals(true, fakePreferencesRepository.isDarkTheme)
    }

    @Test
    fun clearCache_callsRepository() = runTest {
        viewModel.clearCache()
        assertEquals(true, fakePreferencesRepository.isCacheCleared)
    }
}

class FakePreferencesRepository : PreferencesRepository {
    var downloadLocation = ""
    var defaultQuality = "720p"
    var isDarkTheme = false
    var isCacheCleared = false

    override val userPreferencesFlow = MutableStateFlow(UserPreferences())

    override suspend fun updateDownloadLocation(location: String) {
        downloadLocation = location
    }

    override suspend fun updateDefaultQuality(quality: String) {
        defaultQuality = quality
    }

    override suspend fun updateDarkTheme(darkTheme: Boolean) {
        isDarkTheme = darkTheme
    }

    override suspend fun clearCache() {
        isCacheCleared = true
    }
}
