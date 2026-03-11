package com.cloudterminal.presentation.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.data.barcode.BarcodeScannerService
import com.cloudterminal.domain.models.BarcodeFormat
import com.cloudterminal.domain.usecases.ProcessBarcodeUseCase
import com.google.mlkit.vision.barcode.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления сканированием штрих-кодов
 */
@HiltViewModel
class BarcodeScanningViewModel @Inject constructor(
    private val barcodeScannerService: BarcodeScannerService,
    private val processBarcodeUseCase: ProcessBarcodeUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BarcodeScanningUiState())
    val uiState: StateFlow<BarcodeScanningUiState> = _uiState.asStateFlow()
    
    init {
        // Инициализация сканера при создании ViewModel
        barcodeScannerService.initializeScanner()
    }
    
    /**
     * Обработка отсканированного штрих-кода
     */
    fun processBarcode(barcode: Barcode) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isProcessing = true, lastError = null)
            }
            
            try {
                val result = processBarcodeUseCase(barcode)
                
                _uiState.update { state ->
                    val newScanHistory = state.scanHistory + result
                    val newStats = updateStats(state.stats, result)
                    
                    state.copy(
                        isProcessing = false,
                        lastResult = result,
                        scanHistory = newScanHistory.takeLast(MAX_HISTORY_SIZE),
                        stats = newStats,
                        lastScanTime = System.currentTimeMillis()
                    )
                }
                
                // Обновление наиболее частого формата
                updateMostCommonFormat()
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isProcessing = false,
                        lastError = "Ошибка обработки: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Обработка нескольких штрих-кодов
     */
    fun processBarcodes(barcodes: List<Barcode>) {
        viewModelScope.launch {
            barcodes.forEach { barcode ->
                processBarcode(barcode)
                // Небольшая задержка между обработкой для избежания перегрузки
                kotlinx.coroutines.delay(100)
            }
        }
    }
    
    /**
     * Обновление статистики
     */
    private fun updateStats(
        currentStats: BarcodeScanningStats,
        result: com.cloudterminal.domain.usecases.BarcodeProcessingResult
    ): BarcodeScanningStats {
        return currentStats.copy(
            totalScans = currentStats.totalScans + 1,
            successfulScans = currentStats.successfulScans + if (result is com.cloudterminal.domain.usecases.BarcodeProcessingResult.Success) 1 else 0,
            notFoundScans = currentStats.notFoundScans + if (result is com.cloudterminal.domain.usecases.BarcodeProcessingResult.NotFound) 1 else 0,
            errorScans = currentStats.errorScans + if (result is com.cloudterminal.domain.usecases.BarcodeProcessingResult.Error) 1 else 0,
            lastScanTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Обновление наиболее частого формата
     */
    private fun updateMostCommonFormat() {
        val formatCounts = mutableMapOf<BarcodeFormat, Int>()
        
        _uiState.value.scanHistory.forEach { result ->
            when (result) {
                is com.cloudterminal.domain.usecases.BarcodeProcessingResult.Success -> {
                    val count = formatCounts.getOrDefault(result.barcodeFormat, 0)
                    formatCounts[result.barcodeFormat] = count + 1
                }
                is com.cloudterminal.domain.usecases.BarcodeProcessingResult.NotFound -> {
                    val count = formatCounts.getOrDefault(result.barcodeFormat, 0)
                    formatCounts[result.barcodeFormat] = count + 1
                }
                else -> {}
            }
        }
        
        val mostCommon = formatCounts.maxByOrNull { it.value }?.key
        
        _uiState.update { state ->
            state.copy(
                stats = state.stats.copy(mostCommonFormat = mostCommon)
            )
        }
    }
    
    /**
     * Сброс истории сканирования
     */
    fun resetScanHistory() {
        _uiState.update { state ->
            state.copy(
                scanHistory = emptyList(),
                lastResult = null,
                stats = BarcodeScanningStats(),
                lastError = null
            )
        }
    }
    
    /**
     * Очистка ошибки
     */
    fun clearError() {
        _uiState.update { state ->
            state.copy(lastError = null)
        }
    }
    
    /**
     * Установка активных форматов сканирования
     */
    fun setActiveFormats(formats: List<BarcodeFormat>) {
        _uiState.update { state ->
            state.copy(activeFormats = formats)
        }
        
        // Обновление форматов в сервисе сканирования
        val mlKitFormats = formats.map { format ->
            when (format) {
                BarcodeFormat.EAN_13 -> Barcode.FORMAT_EAN_13
                BarcodeFormat.EAN_8 -> Barcode.FORMAT_EAN_8
                BarcodeFormat.UPC_A -> Barcode.FORMAT_UPC_A
                BarcodeFormat.UPC_E -> Barcode.FORMAT_UPC_E
                BarcodeFormat.CODE_39 -> Barcode.FORMAT_CODE_39
                BarcodeFormat.CODE_93 -> Barcode.FORMAT_CODE_93
                BarcodeFormat.CODE_128 -> Barcode.FORMAT_CODE_128
                BarcodeFormat.QR_CODE -> Barcode.FORMAT_QR_CODE
                BarcodeFormat.DATA_MATRIX -> Barcode.FORMAT_DATA_MATRIX
                BarcodeFormat.PDF_417 -> Barcode.FORMAT_PDF417
            }
        }
        
        barcodeScannerService.initializeScanner(mlKitFormats)
    }
    
    /**
     * Получение эффективности сканирования
     */
    fun getScanningEfficiency(): Float {
        val stats = _uiState.value.stats
        return if (stats.totalScans > 0) {
            stats.successfulScans.toFloat() / stats.totalScans.toFloat()
        } else {
            0f
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        barcodeScannerService.cleanup()
    }
    
    companion object {
        private const val MAX_HISTORY_SIZE = 100
    }
}

/**
 * Состояние UI для сканирования штрих-кодов
 */
data class BarcodeScanningUiState(
    val isProcessing: Boolean = false,
    val lastResult: com.cloudterminal.domain.usecases.BarcodeProcessingResult? = null,
    val scanHistory: List<com.cloudterminal.domain.usecases.BarcodeProcessingResult> = emptyList(),
    val stats: BarcodeScanningStats = BarcodeScanningStats(),
    val activeFormats: List<BarcodeFormat> = listOf(
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.CODE_128,
        BarcodeFormat.QR_CODE
    ),
    val lastError: String? = null,
    val lastScanTime: Long = 0
)

/**
 * Статистика сканирования (локальная копия из доменного слоя)
 */
data class BarcodeScanningStats(
    val totalScans: Int = 0,
    val successfulScans: Int = 0,
    val notFoundScans: Int = 0,
    val errorScans: Int = 0,
    val lastScanTime: Long = 0,
    val mostCommonFormat: BarcodeFormat? = null
)