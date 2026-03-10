package com.cloudterminal.presentation.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.data.repository.ScannedItemRepositoryImpl
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.models.ScannedItem as DomainScannedItem
import com.cloudterminal.domain.usecases.GetProductByBarcodeUseCase
import com.cloudterminal.domain.usecases.ProcessBarcodeUseCase
import com.cloudterminal.domain.usecases.SaveScannedItemUseCase
import com.google.mlkit.vision.barcode.Barcode as MlBarcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Обновленный ViewModel для экрана сканирования с интеграцией всех use cases
 */
@HiltViewModel
class ScannerViewModelUpdated @Inject constructor(
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val saveScannedItemUseCase: SaveScannedItemUseCase,
    private val processBarcodeUseCase: ProcessBarcodeUseCase,
    private val scannedItemRepository: ScannedItemRepositoryImpl
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScannerUiStateUpdated())
    val uiState: StateFlow<ScannerUiStateUpdated> = _uiState.asStateFlow()
    
    private val _scannedItems = MutableStateFlow<List<DomainScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<DomainScannedItem>> = _scannedItems.asStateFlow()
    
    private val _scanningStats = MutableStateFlow(ScanningStats())
    val scanningStats: StateFlow<ScanningStats> = _scanningStats.asStateFlow()
    
    init {
        // Загрузка сохраненных отсканированных элементов при инициализации
        loadScannedItems()
    }
    
    /**
     * Обработка отсканированного штрих-кода
     */
    fun onBarcodeScanned(barcode: MlBarcode) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Обработка штрих-кода через use case
                val processingResult = processBarcodeUseCase(barcode)
                
                when (processingResult) {
                    is com.cloudterminal.domain.usecases.BarcodeProcessingResult.Success -> {
                        handleSuccessfulScan(processingResult)
                    }
                    
                    is com.cloudterminal.domain.usecases.BarcodeProcessingResult.NotFound -> {
                        handleNotFoundScan(processingResult)
                    }
                    
                    is com.cloudterminal.domain.usecases.BarcodeProcessingResult.Error -> {
                        handleScanError(processingResult)
                    }
                }
                
                // Обновление статистики
                updateScanningStats()
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка обработки: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Обработка успешного сканирования (товар найден)
     */
    private suspend fun handleSuccessfulScan(
        result: com.cloudterminal.domain.usecases.BarcodeProcessingResult.Success
    ) {
        // Добавление в список отсканированных элементов
        val scannedItem = DomainScannedItem(
            barcode = result.barcodeValue,
            quantity = 1,
            productId = result.product.id,
            scannedAt = System.currentTimeMillis()
        )
        
        // Проверяем, не сканировали ли уже этот товар
        val existingItemIndex = _scannedItems.value.indexOfFirst { 
            it.barcode == result.barcodeValue 
        }
        
        if (existingItemIndex >= 0) {
            // Увеличиваем количество существующего элемента
            _scannedItems.update { items ->
                items.toMutableList().apply {
                    val existingItem = this[existingItemIndex]
                    this[existingItemIndex] = existingItem.copy(
                        quantity = existingItem.quantity + 1
                    )
                }
            }
        } else {
            // Добавляем новый элемент
            _scannedItems.update { it + scannedItem }
        }
        
        // Сохраняем в базу данных
        saveScannedItemUseCase(
            barcode = result.barcodeValue,
            quantity = 1,
            productId = result.product.id
        )
        
        _uiState.update {
            it.copy(
                isLoading = false,
                lastScannedProduct = result.product,
                scanSuccessMessage = "Товар найден: ${result.product.name}"
            )
        }
        
        // Воспроизведение звука успешного сканирования
        playSuccessSound()
    }
    
    /**
     * Обработка сканирования, когда товар не найден
     */
    private suspend fun handleNotFoundScan(
        result: com.cloudterminal.domain.usecases.BarcodeProcessingResult.NotFound
    ) {
        // Добавление в список отсканированных элементов (без productId)
        val scannedItem = DomainScannedItem(
            barcode = result.barcodeValue,
            quantity = 1,
            productId = null,
            scannedAt = System.currentTimeMillis()
        )
        
        _scannedItems.update { it + scannedItem }
        
        // Сохраняем в базу данных
        saveScannedItemUseCase(
            barcode = result.barcodeValue,
            quantity = 1,
            productId = null
        )
        
        _uiState.update {
            it.copy(
                isLoading = false,
                lastScannedProduct = null,
                scanSuccessMessage = "Товар не найден. Штрих-код: ${result.barcodeValue}"
            )
        }
        
        // Воспроизведение звука (но другого тона)
        playNotFoundSound()
    }
    
    /**
     * Обработка ошибки сканирования
     */
    private fun handleScanError(
        result: com.cloudterminal.domain.usecases.BarcodeProcessingResult.Error
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = result.error,
                lastScannedProduct = null
            )
        }
        
        // Воспроизведение звука ошибки
        playErrorSound()
    }
    
    /**
     * Переключение вспышки
     */
    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }
    
    /**
     * Очистка списка отсканированных элементов
     */
    fun clearScannedItems() {
        viewModelScope.launch {
            _scannedItems.update { emptyList() }
            _uiState.update { it.copy(scanSuccessMessage = null) }
        }
    }
    
    /**
     * Сохранение текущей сессии сканирования
     */
    fun saveCurrentSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // TODO: Реализовать сохранение сессии
                // Создание сессии сканирования
                // Сохранение всех отсканированных элементов с sessionId
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scanSuccessMessage = "Сессия сохранена (${_scannedItems.value.size} товаров)"
                    )
                }
                
                // Очистка списка после сохранения
                clearScannedItems()
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка сохранения сессии: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Загрузка сохраненных отсканированных элементов
     */
    private fun loadScannedItems() {
        viewModelScope.launch {
            try {
                // TODO: Загрузка элементов из репозитория
                // Пока используем пустой список
                _scannedItems.update { emptyList() }
            } catch (e: Exception) {
                // Игнорируем ошибки при загрузке
            }
        }
    }
    
    /**
     * Обновление статистики сканирования
     */
    private fun updateScanningStats() {
        _scanningStats.update { stats ->
            stats.copy(
                totalScans = stats.totalScans + 1,
                lastScanTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Удаление конкретного отсканированного элемента
     */
    fun removeScannedItem(barcode: String) {
        viewModelScope.launch {
            _scannedItems.update { items ->
                items.filterNot { it.barcode == barcode }
            }
        }
    }
    
    /**
     * Обновление количества для конкретного товара
     */
    fun updateItemQuantity(barcode: String, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity <= 0) {
                removeScannedItem(barcode)
                return@launch
            }
            
            _scannedItems.update { items ->
                items.map { item ->
                    if (item.barcode == barcode) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
            }
        }
    }
    
    /**
     * Получение общего количества отсканированных товаров
     */
    fun getTotalScannedQuantity(): Int {
        return _scannedItems.value.sumOf { it.quantity }
    }
    
    /**
     * Получение количества уникальных отсканированных товаров
     */
    fun getUniqueScannedCount(): Int {
        return _scannedItems.value.size
    }
    
    /**
     * Воспроизведение звуков (заглушки)
     */
    private fun playSuccessSound() {
        // TODO: Реализовать воспроизведение звука
    }
    
    private fun playNotFoundSound() {
        // TODO: Реализовать воспроизведение звука
    }
    
    private fun playErrorSound() {
        // TODO: Реализовать воспроизведение звука
    }
}

/**
 * Состояние UI для экрана сканирования
 */
data class ScannerUiStateUpdated(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val scanSuccessMessage: String? = null,
    val isFlashOn: Boolean = false,
    val lastScannedProduct: Product? = null,
    val isScannerActive: Boolean = true
)

/**
 * Статистика сканирования
 */
data class ScanningStats(
    val totalScans: Int = 0,
    val successfulScans: Int = 0,
    val notFoundScans: Int = 0,
    val errorScans: Int = 0,
    val lastScanTime: Long = 0,
    val scanSessionStartTime: Long = System.currentTimeMillis()
)