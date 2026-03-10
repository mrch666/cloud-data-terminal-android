package com.cloudterminal.presentation.screens.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.domain.models.Barcode
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.usecases.ScanBarcodeUseCase
import com.cloudterminal.domain.usecases.GetProductByBarcodeUseCase
import com.cloudterminal.domain.usecases.SaveScannedItemUseCase
import com.google.mlkit.vision.barcode.common.Barcode as MlBarcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFlashOn: Boolean = false,
    val cameraProvider: ProcessCameraProvider? = null,
    val preview: Preview? = null,
    val imageAnalysis: ImageAnalysis? = null,
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
)

data class ScannedItem(
    val barcode: String,
    val quantity: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val productName: String? = null,
    val productId: String? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val saveScannedItemUseCase: SaveScannedItemUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    
    private val _scannedItems = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems.asStateFlow()
    
    private var cameraProvider: ProcessCameraProvider? = null
    
    fun startCamera() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // TODO: Initialize camera
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка камеры: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun onBarcodeScanned(barcode: MlBarcode) {
        viewModelScope.launch {
            val barcodeValue = barcode.rawValue ?: return@launch
            
            // Проверяем, не сканировали ли уже этот штрих-код
            val existingItem = _scannedItems.value.find { it.barcode == barcodeValue }
            
            if (existingItem != null) {
                // Увеличиваем количество
                _scannedItems.update { items ->
                    items.map { item ->
                        if (item.barcode == barcodeValue) {
                            item.copy(quantity = item.quantity + 1)
                        } else {
                            item
                        }
                    }
                }
            } else {
                // Ищем товар в базе
                val product = getProductByBarcodeUseCase(barcodeValue)
                
                val newItem = ScannedItem(
                    barcode = barcodeValue,
                    quantity = 1,
                    productName = product?.name,
                    productId = product?.id
                )
                
                _scannedItems.update { it + newItem }
                
                // Сохраняем в базу
                saveScannedItemUseCase(
                    barcode = barcodeValue,
                    quantity = 1,
                    productId = product?.id
                )
            }
            
            // Воспроизводим звук сканирования
            playScanSound()
        }
    }
    
    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
        // TODO: Реализовать включение/выключение вспышки
    }
    
    fun clearScannedItems() {
        _scannedItems.update { emptyList() }
    }
    
    fun saveCurrentSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // TODO: Сохранить сессию сканирования
                _uiState.update { it.copy(isLoading = false) }
                clearScannedItems()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка сохранения: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun playScanSound() {
        // TODO: Воспроизвести звук сканирования
    }
    
    override fun onCleared() {
        super.onCleared()
        cameraProvider?.unbindAll()
    }
}