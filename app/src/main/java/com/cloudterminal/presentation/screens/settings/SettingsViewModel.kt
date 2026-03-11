package com.cloudterminal.presentation.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.domain.models.AppSettings
import com.cloudterminal.domain.models.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана настроек
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    init {
        // Загрузка настроек при инициализации
        loadSettings()
    }
    
    /**
     * Загрузка настроек из DataStore
     */
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                AppSettings(
                    apiUrl = preferences[PreferencesKeys.API_URL] ?: "",
                    apiKey = preferences[PreferencesKeys.API_KEY] ?: "",
                    syncInterval = (preferences[PreferencesKeys.SYNC_INTERVAL] ?: "300").toIntOrNull() ?: 300,
                    autoUpload = preferences[PreferencesKeys.AUTO_UPLOAD]?.toBoolean() ?: true,
                    enableFlash = preferences[PreferencesKeys.ENABLE_FLASH]?.toBoolean() ?: false,
                    enableSound = preferences[PreferencesKeys.ENABLE_SOUND]?.toBoolean() ?: true,
                    barcodeFormats = parseBarcodeFormats(preferences[PreferencesKeys.BARCODE_FORMATS])
                )
            }.collect { loadedSettings ->
                _settings.update { loadedSettings }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Сохранение настроек в DataStore
     */
    fun saveSettings(settings: AppSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.API_URL] = settings.apiUrl
                    preferences[PreferencesKeys.API_KEY] = settings.apiKey
                    preferences[PreferencesKeys.SYNC_INTERVAL] = settings.syncInterval.toString()
                    preferences[PreferencesKeys.AUTO_UPLOAD] = settings.autoUpload.toString()
                    preferences[PreferencesKeys.ENABLE_FLASH] = settings.enableFlash.toString()
                    preferences[PreferencesKeys.ENABLE_SOUND] = settings.enableSound.toString()
                    preferences[PreferencesKeys.BARCODE_FORMATS] = serializeBarcodeFormats(settings.barcodeFormats)
                }
                
                _settings.update { settings }
                
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Настройки сохранены"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Ошибка сохранения: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Обновление URL API
     */
    fun updateApiUrl(apiUrl: String) {
        _settings.update { it.copy(apiUrl = apiUrl) }
    }
    
    /**
     * Обновление ключа API
     */
    fun updateApiKey(apiKey: String) {
        _settings.update { it.copy(apiKey = apiKey) }
    }
    
    /**
     * Обновление интервала синхронизации
     */
    fun updateSyncInterval(interval: Int) {
        _settings.update { it.copy(syncInterval = interval.coerceAtLeast(60)) } // Минимум 60 секунд
    }
    
    /**
     * Переключение автоматической загрузки
     */
    fun toggleAutoUpload() {
        _settings.update { it.copy(autoUpload = !it.autoUpload) }
    }
    
    /**
     * Переключение вспышки
     */
    fun toggleFlash() {
        _settings.update { it.copy(enableFlash = !it.enableFlash) }
    }
    
    /**
     * Переключение звука
     */
    fun toggleSound() {
        _settings.update { it.copy(enableSound = !it.enableSound) }
    }
    
    /**
     * Обновление форматов штрих-кодов
     */
    fun updateBarcodeFormats(formats: Set<BarcodeFormat>) {
        _settings.update { it.copy(barcodeFormats = formats) }
    }
    
    /**
     * Добавление формата штрих-кода
     */
    fun addBarcodeFormat(format: BarcodeFormat) {
        val currentFormats = _settings.value.barcodeFormats.toMutableSet()
        currentFormats.add(format)
        _settings.update { it.copy(barcodeFormats = currentFormats) }
    }
    
    /**
     * Удаление формата штрих-кода
     */
    fun removeBarcodeFormat(format: BarcodeFormat) {
        val currentFormats = _settings.value.barcodeFormats.toMutableSet()
        currentFormats.remove(format)
        _settings.update { it.copy(barcodeFormats = currentFormats) }
    }
    
    /**
     * Сброс настроек к значениям по умолчанию
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true, errorMessage = null) }
            
            try {
                val defaultSettings = AppSettings()
                saveSettings(defaultSettings)
                
                _uiState.update {
                    it.copy(
                        isResetting = false,
                        successMessage = "Настройки сброшены к значениям по умолчанию"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isResetting = false,
                        errorMessage = "Ошибка сброса: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Тестирование соединения с API
     */
    fun testApiConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, errorMessage = null) }
            
            try {
                // TODO: Реализовать тестирование соединения с API
                kotlinx.coroutines.delay(2000) // Имитация тестирования
                
                val isSuccess = _settings.value.apiUrl.isNotBlank()
                
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = if (isSuccess) {
                            "Соединение с API успешно установлено"
                        } else {
                            "URL API не указан"
                        }
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        errorMessage = "Ошибка тестирования: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Экспорт настроек
     */
    fun exportSettings(): String {
        return """
            API URL: ${_settings.value.apiUrl}
            API Key: ${_settings.value.apiKey.take(10)}... (скрыто)
            Sync Interval: ${_settings.value.syncInterval} секунд
            Auto Upload: ${_settings.value.autoUpload}
            Enable Flash: ${_settings.value.enableFlash}
            Enable Sound: ${_settings.value.enableSound}
            Barcode Formats: ${_settings.value.barcodeFormats.joinToString { it.displayName }}
        """.trimIndent()
    }
    
    /**
     * Очистка кэша приложения
     */
    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingCache = true, errorMessage = null) }
            
            try {
                // TODO: Реализовать очистку кэша
                kotlinx.coroutines.delay(1000)
                
                _uiState.update {
                    it.copy(
                        isClearingCache = false,
                        successMessage = "Кэш приложения очищен"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClearingCache = false,
                        errorMessage = "Ошибка очистки кэша: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Очистка ошибки
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Очистка сообщения об успехе
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null, connectionTestResult = null) }
    }
    
    /**
     * Парсинг форматов штрих-кодов из строки
     */
    private fun parseBarcodeFormats(formatsString: String?): Set<BarcodeFormat> {
        if (formatsString.isNullOrBlank()) {
            return setOf(
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.CODE_128,
                BarcodeFormat.QR_CODE
            )
        }
        
        return formatsString.split(",")
            .mapNotNull { formatName ->
                BarcodeFormat.entries.find { it.name == formatName }
            }
            .toSet()
    }
    
    /**
     * Сериализация форматов штрих-кодов в строку
     */
    private fun serializeBarcodeFormats(formats: Set<BarcodeFormat>): String {
        return formats.joinToString(",") { it.name }
    }
    
    private object PreferencesKeys {
        val API_URL = stringPreferencesKey("api_url")
        val API_KEY = stringPreferencesKey("api_key")
        val SYNC_INTERVAL = stringPreferencesKey("sync_interval")
        val AUTO_UPLOAD = stringPreferencesKey("auto_upload")
        val ENABLE_FLASH = stringPreferencesKey("enable_flash")
        val ENABLE_SOUND = stringPreferencesKey("enable_sound")
        val BARCODE_FORMATS = stringPreferencesKey("barcode_formats")
    }
}

/**
 * Состояние UI для экрана настроек
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isResetting: Boolean = false,
    val isTestingConnection: Boolean = false,
    val isClearingCache: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val connectionTestResult: String? = null
)