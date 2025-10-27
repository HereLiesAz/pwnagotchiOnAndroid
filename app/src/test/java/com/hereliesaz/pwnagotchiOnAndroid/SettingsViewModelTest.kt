package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    @Mock
    private lateinit var localAgentManager: LocalAgentManager

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        whenever(context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(localAgentManager.isDeviceRooted()).thenReturn(true)
        whenever(localAgentManager.hasNexmon()).thenReturn(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveSettings should save all settings to shared preferences`() = runTest {
        viewModel = SettingsViewModel(context, localAgentManager)

        val host = "testhost"
        val apiKey = "testkey"
        val city = "testcity"
        val mode = PwnagotchiMode.LOCAL
        val theme = AppTheme.DARK
        val selectedInterface = "wlan1"

        viewModel.saveSettings(context, host, apiKey, city, mode, theme, selectedInterface)

        // Advance the dispatcher to allow the save coroutine to run
        testDispatcher.scheduler.advanceUntilIdle()

        verify(editor).putString("host", host)
        verify(editor).putString("api_key", apiKey)
        verify(editor).putString("city", city)
        verify(editor).putString("mode", mode.name)
        verify(editor).putString("theme", theme.name)
        verify(editor).putString("selected_interface", selectedInterface)
        verify(editor).apply()
    }

    @Test
    fun `loadSettings should load all settings from shared preferences`() = runTest {
        val host = "testhost"
        val apiKey = "testkey"
        val city = "testcity"
        val mode = PwnagotchiMode.LOCAL
        val theme = AppTheme.LIGHT
        val selectedInterface = "wlan0"

        whenever(sharedPreferences.getString("host", "")).thenReturn(host)
        whenever(sharedPreferences.getString("api_key", "")).thenReturn(apiKey)
        whenever(sharedPreferences.getString("city", "")).thenReturn(city)
        whenever(sharedPreferences.getString("mode", PwnagotchiMode.REMOTE.name)).thenReturn(mode.name)
        whenever(sharedPreferences.getString("theme", AppTheme.SYSTEM.name)).thenReturn(theme.name)
        whenever(sharedPreferences.getString("selected_interface", "")).thenReturn(selectedInterface)


        viewModel = SettingsViewModel(context, localAgentManager)
        // Advance the dispatcher to allow the init block's coroutine to run
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value as SettingsUiState.Loaded

        assert(uiState.host == host)
        assert(uiState.apiKey == apiKey)
        assert(uiState.city == city)
        assert(uiState.mode == mode)
        assert(uiState.theme == theme)
        assert(uiState.selectedInterface == selectedInterface)
    }
}
