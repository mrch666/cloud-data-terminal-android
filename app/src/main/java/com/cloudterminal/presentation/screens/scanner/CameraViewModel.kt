package com.cloudterminal.presentation.screens.scanner

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.presentation.components.getAvailableCameras
import com.cloudterminal.presentation.components.isCameraAvailable
import com.google.mlkit.vision.barcode.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления состоянием камеры и сканирования
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    /**
     * Проверка доступности камеры
     */
    fun checkCameraAvailability(context: Context) {
        viewModelScope.launch {
            val isAvailable = isCameraAvailable(context)
            val availableCameras = getAvailableCameras(context)
            
            _uiState.update { state ->
                state.copy(
                    isCameraAvailable = isAvailable,
                    availableCameras = availableCameras,
                    cameraError = if (!isAvailable) "Камера не доступна" else null
                )
            }
        }
    }
    
    /**
     * Переключение камеры (передняя/задняя)
     */
    fun toggleCamera() {
        _uiState.update { state ->
            val newLensFacing = when (state.lensFacing) {
                CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
                CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
                else -> CameraSelector.LENS_FACING_BACK
            }
            
            // Проверяем, доступна ли выбранная камера
            val isAvailable = state.availableCameras.contains(newLensFacing)
            
            state.copy(
                lensFacing = if (isAvailable) newLensFacing else state.lensFacing,
                cameraError = if (!isAvailable) "Выбранная камера не доступна" else null
            )
        }
    }
    
    /**
     * Переключение вспышки
     */
    fun toggleFlash() {
        _uiState.update { state ->
            state.copy(isFlashOn = !state.isFlashOn)
        }
    }
    
    /**
     * Обработка отсканированного штрих-кода
     */
    fun onBarcodeScanned(barcode: Barcode) {
        viewModelScope.launch {
            val barcodeValue = barcode.rawValue ?: return@launch
            
            _uiState.update { state ->
                state.copy(
                    lastScannedBarcode = barcodeValue,
                    scanCount = state.scanCount + 1,
                    lastScanTime = System.currentTimeMillis()
                )
            }
            
            // Здесь можно добавить логику для обработки штрих-кода
            // Например, поиск товара в базе данных
        }
    }
    
    /**
     * Обработка ошибки камеры
     */
    fun onCameraError(error: Throwable) {
        _uiState.update { state ->
            state.copy(
                cameraError = "Ошибка камеры: ${error.message}",
                isCameraActive = false
            )
        }
    }
    
    /**
     * Сброс состояния сканирования
     */
    fun resetScanning() {
        _uiState.update { state ->
            state.copy(
                lastScannedBarcode = null,
                scanCount = 0,
                cameraError = null
            )
        }
    }
    
    /**
     * Включение/выключение камеры
     */
    fun setCameraActive(isActive: Boolean) {
        _uiState.update { state ->
            state.copy(isCameraActive = isActive)
        }
    }
    
    /**
     * Установка задержки между сканированиями
     */
    fun setScanDelay(delayMs: Long) {
        _uiState.update { state ->
            state.copy(scanDelayMs = delayMs.coerceAtLeast(0))
        }
    }
}

/**
 * Состояние UI для камеры
 */
data class CameraUiState(
    val isCameraAvailable: Boolean = false,
    val isCameraActive: Boolean = true,
    val isFlashOn: Boolean = false,
    val lensFacing: CameraSelector.LensFacing = CameraSelector.LENS_FACING_BACK,
    val availableCameras: List<CameraSelector.LensFacing> = emptyList(),
    val lastScannedBarcode: String? = null,
    val scanCount: Int = 0,
    val lastScanTime: Long = 0,
    val scanDelayMs: Long = 1000, // Задержка между сканированиями в мс
    val cameraError: String? = null,
    val isLoading: Boolean = false
)