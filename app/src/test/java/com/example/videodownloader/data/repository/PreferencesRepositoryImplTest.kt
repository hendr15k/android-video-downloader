package com.example.videodownloader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: PreferencesRepositoryImpl
    private val mockContext = mock(Context::class.java)

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFolder.newFile("user_preferences.preferences_pb") }
        )

        val cacheDir = tempFolder.newFolder("cache")
        `when`(mockContext.cacheDir).thenReturn(cacheDir)
        `when`(mockContext.externalCacheDir).thenReturn(null)

        repository = PreferencesRepositoryImpl(dataStore, mockContext)
    }

    @Test
    fun defaultPreferences_areReturnedInitially() = testScope.runTest {
        val initialPreferences = repository.userPreferencesFlow.first()
        assertEquals("", initialPreferences.downloadLocation)
        assertEquals("720p", initialPreferences.defaultQuality)
        assertEquals(false, initialPreferences.darkTheme)
    }

    @Test
    fun updateDownloadLocation_updatesFlow() = testScope.runTest {
        repository.updateDownloadLocation("/test/location")
        val preferences = repository.userPreferencesFlow.first()
        assertEquals("/test/location", preferences.downloadLocation)
    }

    @Test
    fun updateDefaultQuality_updatesFlow() = testScope.runTest {
        repository.updateDefaultQuality("1080p")
        val preferences = repository.userPreferencesFlow.first()
        assertEquals("1080p", preferences.defaultQuality)
    }

    @Test
    fun updateDarkTheme_updatesFlow() = testScope.runTest {
        repository.updateDarkTheme(true)
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(true, preferences.darkTheme)
    }
}
