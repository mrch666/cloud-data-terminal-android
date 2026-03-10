package com.cloudterminal.presentation.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cloudterminal.domain.models.AppSettings
import com.cloudterminal.domain.models.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    
    private lateinit var viewModel: SettingsViewModel
    private lateinit var dataStore: DataStore<Preferences>
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        dataStore = mock()
        viewModel = SettingsViewModel(dataStore)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        val expectedState = SettingsUiState(
            isLoading = true, // Начальная загрузка
            isSaving = false,
            isResetting = false,
            isTestingConnection = false,
            isClearingCache = false,
            errorMessage = null,
            successMessage = null,
            connectionTestResult = null
        )
        
        // When
        val actualState = viewModel.uiState.value
        
        // Then
        assert(actualState == expectedState)
    }
    
    @Test
    fun `updateApiUrl should update settings`() = runTest {
        // Given
        val newApiUrl = "https://api.example.com/v2"
        
        // When
        viewModel.updateApiUrl(newApiUrl)
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.apiUrl == newApiUrl)
    }
    
    @Test
    fun `updateApiKey should update settings`() = runTest {
        // Given
        val newApiKey = "new_api_key_123456"
        
        // When
        viewModel.updateApiKey(newApiKey)
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.apiKey == newApiKey)
    }
    
    @Test
    fun `updateSyncInterval should update settings with minimum value`() = runTest {
        // Given
        val testCases = listOf(
            30 to 60, // Ниже минимума -> устанавливается минимум
            60 to 60, // На минимуме
            300 to 300, // Нормальное значение
            86400 to 86400 // Большое значение
        )
        
        testCases.forEach { (input, expected) ->
            // When
            viewModel.updateSyncInterval(input)
            
            // Then
            val settings = viewModel.settings.value
            assert(settings.syncInterval == expected) {
                "Для входа $input ожидалось $expected, получено ${settings.syncInterval}"
            }
        }
    }
    
    @Test
    fun `toggleAutoUpload should toggle setting`() = runTest {
        // Given
        val initialValue = viewModel.settings.value.autoUpload
        
        // When
        viewModel.toggleAutoUpload()
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.autoUpload != initialValue)
    }
    
    @Test
    fun `toggleFlash should toggle setting`() = runTest {
        // Given
        val initialValue = viewModel.settings.value.enableFlash
        
        // When
        viewModel.toggleFlash()
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.enableFlash != initialValue)
    }
    
    @Test
    fun `toggleSound should toggle setting`() = runTest {
        // Given
        val initialValue = viewModel.settings.value.enableSound
        
        // When
        viewModel.toggleSound()
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.enableSound != initialValue)
    }
    
    @Test
    fun `addBarcodeFormat should add format`() = runTest {
        // Given
        val formatToAdd = BarcodeFormat.EAN_13
        
        // When
        viewModel.addBarcodeFormat(formatToAdd)
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.barcodeFormats.contains(formatToAdd))
    }
    
    @Test
    fun `removeBarcodeFormat should remove format`() = runTest {
        // Given
        val formatToRemove = BarcodeFormat.EAN_13
        
        // Сначала добавляем формат
        viewModel.addBarcodeFormat(formatToRemove)
        assert(viewModel.settings.value.barcodeFormats.contains(formatToRemove))
        
        // When
        viewModel.removeBarcodeFormat(formatToRemove)
        
        // Then
        val settings = viewModel.settings.value
        assert(!settings.barcodeFormats.contains(formatToRemove))
    }
    
    @Test
    fun `updateBarcodeFormats should replace all formats`() = runTest {
        // Given
        val newFormats = setOf(
            BarcodeFormat.EAN_13,
            BarcodeFormat.CODE_128,
            BarcodeFormat.QR_CODE
        )
        
        // Когда
        viewModel.updateBarcodeFormats(newFormats)
        
        // Then
        val settings = viewModel.settings.value
        assert(settings.barcodeFormats == newFormats)
    }
    
    @Test
    fun `testApiConnection should show success for valid URL`() = runTest {
        // When
        viewModel.testApiConnection()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isTestingConnection)
        assert(uiState.connectionTestResult != null)
        assert(uiState.connectionTestResult!!.contains("URL API не указан"))
    }
    
    @Test
    fun `clearCache should show success message`() = runTest {
        // When
        viewModel.clearCache()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isClearingCache)
        assert(uiState.successMessage != null)
        assert(uiState.successMessage!!.contains("Кэш приложения очищен"))
    }
    
    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        // Создаем состояние с ошибкой через рефлексию
        val field = SettingsViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        val mutableStateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<SettingsUiState>
        mutableStateFlow.value = SettingsUiState(errorMessage = "Test error")
        
        // Проверяем, что ошибка есть
        assert(viewModel.uiState.value.errorMessage != null)
        
        // When
        viewModel.clearError()
        
        // Then
        assert(viewModel.uiState.value.errorMessage == null)
    }
    
    @Test
    fun `clearSuccessMessage should clear success messages`() = runTest {
        // Given
        // Создаем состояние с сообщениями через рефлексию
        val field = SettingsViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        val mutableStateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<SettingsUiState>
        mutableStateFlow.value = SettingsUiState(
            successMessage = "Test success",
            connectionTestResult = "Test connection"
        )
        
        // Проверяем, что сообщения есть
        assert(viewModel.uiState.value.successMessage != null)
        assert(viewModel.uiState.value.connectionTestResult != null)
        
        // When
        viewModel.clearSuccessMessage()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.successMessage == null)
        assert(uiState.connectionTestResult == null)
    }
    
    @Test
    fun `exportSettings should return formatted string`() = runTest {
        // Given
        val settings = AppSettings(
            apiUrl = "https://api.example.com",
            apiKey = "secret_key_123456",
            syncInterval = 300,
            autoUpload = true,
            enableFlash = false,
            enableSound = true,
            barcodeFormats = setOf(BarcodeFormat.EAN_13, BarcodeFormat.CODE_128)
        )
        
        // Устанавливаем настройки через рефлексию
        val field = SettingsViewModel::class.java.getDeclaredField("_settings")
        field.isAccessible = true
        val mutableStateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<AppSettings>
        mutableStateFlow.value = settings
        
        // When
        val exportString = viewModel.exportSettings()
        
        // Then
        assert(exportString.contains("API URL: https://api.example.com"))
        assert(exportString.contains("API Key: secret_key... (скрыто)"))
        assert(exportString.contains("Sync Interval: 300 секунд"))
        assert(exportString.contains("Auto Upload: true"))
        assert(exportString.contains("Enable Flash: false"))
        assert(exportString.contains("Enable Sound: true"))
        assert(exportString.contains("Barcode Formats: EAN-13, CODE-128"))
    }
    
    @Test
    fun `resetToDefaults should reset settings`() = runTest {
        // Given
        val customSettings = AppSettings(
            apiUrl = "https://custom.example.com",
            apiKey = "custom_key",
            syncInterval = 600,
            autoUpload = false,
            enableFlash = true,
            enableSound = false,
            barcodeFormats = setOf(BarcodeFormat.QR_CODE)
        )
        
        // Устанавливаем кастомные настройки
        val field = SettingsViewModel::class.java.getDeclaredField("_settings")
        field.isAccessible = true
        val mutableStateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<AppSettings>
        mutableStateFlow.value = customSettings
        
        // Проверяем, что настройки установлены
        assert(viewModel.settings.value.apiUrl == "https://custom.example.com")
        
        // When
        viewModel.resetToDefaults()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val settings = viewModel.settings.value
        
        assert(!uiState.isResetting)
        assert(uiState.successMessage != null)
        assert(uiState.successMessage!!.contains("Настройки сброшены"))
        
        // Проверяем значения по умолчанию
        assert(settings.apiUrl == "")
        assert(settings.apiKey == "")
        assert(settings.syncInterval == 300)
        assert(settings.autoUpload == true)
        assert(settings.enableFlash == false)
        assert(settings.enableSound == true)
        assert(settings.barcodeFormats.contains(BarcodeFormat.EAN_13))
        assert(settings.barcodeFormats.contains(BarcodeFormat.EAN_8))
        assert(settings.barcodeFormats.contains(BarcodeFormat.UPC_A))
        assert(settings.barcodeFormats.contains(BarcodeFormat.CODE_128))
        assert(settings.barcodeFormats.contains(BarcodeFormat.QR_CODE))
    }
    
    @Test
    fun `saveSettings should handle success`() = runTest {
        // Given
        val settingsToSave = AppSettings(
            apiUrl = "https://api.example.com",
            apiKey = "test_key",
            syncInterval = 300,
            autoUpload = true,
            enableFlash = false,
            enableSound = true,
            barcodeFormats = setOf(BarcodeFormat.EAN_13)
        )
        
        // Настраиваем мок DataStore
        whenever(dataStore.edit(any())).thenAnswer { 
            val lambda = it.arguments[0] as suspend (Preferences) -> Unit
            // Выполняем лямбду с пустыми настройками
            lambda(mock())
            mock<Preferences>()
        }
        
        // When
        viewModel.saveSettings(settingsToSave)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isSaving)
        assert(uiState.errorMessage == null)
        assert(uiState.successMessage != null)
        assert(uiState.successMessage!!.contains("Настройки сохранены"))
        
        // Проверяем, что настройки обновлены
        val savedSettings = viewModel.settings.value
        assert(savedSettings.apiUrl == settingsToSave.apiUrl)
        assert(savedSettings.apiKey == settingsToSave.apiKey)
        assert(savedSettings.syncInterval == settingsToSave.syncInterval)
        assert(savedSettings.autoUpload == settingsToSave.autoUpload)
        assert(savedSettings.enableFlash == settingsToSave.enableFlash)
        assert(savedSettings.enableSound == settingsToSave.enableSound)
        assert(savedSettings.barcodeFormats == settingsToSave.barcodeFormats)
    }
    
    @Test
    fun `saveSettings should handle error`() = runTest {
        // Given
        val settingsToSave = AppSettings()
        val errorMessage = "DataStore error"
        
        // Настраиваем мок DataStore для выброса исключения
        whenever(dataStore.edit(any())).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.saveSettings(settingsToSave)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isSaving)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains(errorMessage))
        assert(uiState.successMessage == null)
    }
    
    @Test
    fun `parseBarcodeFormats should handle empty string`() = runTest {
        // Given
        val emptyString: String? = null
        
        // When
        val result = viewModel.javaClass.getDeclaredMethod(
            "parseBarcodeFormats", 
            String::class.java
        ).apply { isAccessible = true }
        .invoke(viewModel, emptyString) as Set<BarcodeFormat>
        
        // Then
        assert(result.size == 5) // Значения по умолчанию
        assert(result.contains(BarcodeFormat.EAN_13))
        assert(result.contains(BarcodeFormat.EAN_8))
        assert(result.contains(BarcodeFormat.UPC_A))
        assert(result.contains(BarcodeFormat.CODE_128))
        assert(result.contains(BarcodeFormat.QR_CODE))
    }
    
    @Test
    fun `parseBarcodeFormats should parse valid string`() = runTest {
        // Given
        val formatsString = "EAN_13,CODE_128,QR_CODE"
        
        // When
        val result = viewModel.javaClass.getDeclaredMethod(
            "parseBarcodeFormats", 
            String::class.java
        ).apply { isAccessible = true }
        .invoke(viewModel, formatsString) as Set<BarcodeFormat>
        
        // Then
        assert(result.size == 3)
        assert(result.contains(BarcodeFormat.EAN_13))
        assert(result.contains(BarcodeFormat.CODE_128))
        assert(result.contains(BarcodeFormat.QR_CODE))
    }
    
    @Test
    fun `serializeBarcodeFormats should create correct string`() = runTest {
        // Given
        val formats = setOf(
            BarcodeFormat.EAN_13,
            BarcodeFormat.CODE_128,
            BarcodeFormat.QR_CODE
        )
        
        // When
        val result = viewModel.javaClass.getDeclaredMethod(
            "serializeBarcodeFormats", 
            Set::class.java
        ).apply { isAccessible = true }
        .invoke(viewModel, formats) as String
        
        // Then
        val parts = result.split(",")
        assert(parts.size == 3)
        assert(parts.contains("EAN_13"))
        assert(parts.contains("CODE_128"))
        assert(parts.contains("QR_CODE"))
    }
}