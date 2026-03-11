package com.cloudterminal.presentation.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.repository.ProductRepository
import com.cloudterminal.domain.repository.ScannedItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана инвентаризации
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val scannedItemRepository: ScannedItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _scannedItems = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems.asStateFlow()
    
    init {
        // Загрузка данных при инициализации
        loadProducts()
        loadScannedItems()
    }
    
    /**
     * Загрузка товаров из репозитория
     */
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val productList = productRepository.getAllProducts()
                _products.update { productList }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalProducts = productList.size
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки товаров: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Загрузка отсканированных элементов
     */
    fun loadScannedItems() {
        viewModelScope.launch {
            try {
                val items = scannedItemRepository.getScannedItems(null)
                _scannedItems.update { items }
                
                _uiState.update {
                    it.copy(totalScannedItems = items.size)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при загрузке отсканированных элементов
            }
        }
    }
    
    /**
     * Поиск товаров
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchQuery = query) }
            
            try {
                val searchResults = if (query.isBlank()) {
                    productRepository.getAllProducts()
                } else {
                    productRepository.searchProducts(query)
                }
                
                _products.update { searchResults }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResultsCount = searchResults.size
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка поиска: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Фильтрация товаров по категории
     */
    fun filterByCategory(category: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
            
            try {
                val allProducts = productRepository.getAllProducts()
                val filteredProducts = if (category != null) {
                    allProducts.filter { it.category == category }
                } else {
                    allProducts
                }
                
                _products.update { filteredProducts }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        filteredProductsCount = filteredProducts.size
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка фильтрации: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Получение списка категорий
     */
    fun getCategories(): List<String> {
        return _products.value
            .mapNotNull { it.category }
            .distinct()
            .sorted()
    }
    
    /**
     * Сортировка товаров
     */
    fun sortProducts(sortType: SortType) {
        _uiState.update { it.copy(sortType = sortType) }
        
        val sortedProducts = when (sortType) {
            SortType.NAME_ASC -> _products.value.sortedBy { it.name }
            SortType.NAME_DESC -> _products.value.sortedByDescending { it.name }
            SortType.PRICE_ASC -> _products.value.sortedBy { it.price ?: 0.0 }
            SortType.PRICE_DESC -> _products.value.sortedByDescending { it.price ?: 0.0 }
            SortType.CATEGORY -> _products.value.sortedBy { it.category ?: "" }
        }
        
        _products.update { sortedProducts }
    }
    
    /**
     * Получение статистики по инвентарю
     */
    fun getInventoryStats(): InventoryStats {
        val productsList = _products.value
        
        return InventoryStats(
            totalProducts = productsList.size,
            totalValue = productsList.sumOf { it.price ?: 0.0 },
            categoriesCount = productsList.mapNotNull { it.category }.distinct().size,
            productsWithPrice = productsList.count { it.price != null },
            productsWithoutPrice = productsList.count { it.price == null }
        )
    }
    
    /**
     * Экспорт инвентаря
     */
    fun exportInventory(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            
            try {
                // TODO: Реализовать экспорт в выбранном формате
                when (format) {
                    ExportFormat.CSV -> exportToCsv()
                    ExportFormat.EXCEL -> exportToExcel()
                    ExportFormat.JSON -> exportToJson()
                }
                
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportMessage = "Инвентарь экспортирован в ${format.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Ошибка экспорта: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Импорт товаров
     */
    fun importProducts(filePath: String, format: ImportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importMessage = null) }
            
            try {
                // TODO: Реализовать импорт из файла
                when (format) {
                    ImportFormat.CSV -> importFromCsv(filePath)
                    ImportFormat.EXCEL -> importFromExcel(filePath)
                    ImportFormat.JSON -> importFromJson(filePath)
                }
                
                // Перезагрузка товаров после импорта
                loadProducts()
                
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importMessage = "Товары успешно импортированы"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = "Ошибка импорта: ${e.message}"
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
     * Очистка сообщения об экспорте/импорте
     */
    fun clearMessages() {
        _uiState.update { it.copy(exportMessage = null, importMessage = null) }
    }
    
    // Заглушки для методов экспорта/импорта
    private suspend fun exportToCsv() {
        // TODO: Реализовать экспорт в CSV
        kotlinx.coroutines.delay(1000) // Имитация работы
    }
    
    private suspend fun exportToExcel() {
        // TODO: Реализовать экспорт в Excel
        kotlinx.coroutines.delay(1000)
    }
    
    private suspend fun exportToJson() {
        // TODO: Реализовать экспорт в JSON
        kotlinx.coroutines.delay(1000)
    }
    
    private suspend fun importFromCsv(filePath: String) {
        // TODO: Реализовать импорт из CSV
        kotlinx.coroutines.delay(1000)
    }
    
    private suspend fun importFromExcel(filePath: String) {
        // TODO: Реализовать импорт из Excel
        kotlinx.coroutines.delay(1000)
    }
    
    private suspend fun importFromJson(filePath: String) {
        // TODO: Реализовать импорт из JSON
        kotlinx.coroutines.delay(1000)
    }
}

/**
 * Состояние UI для экрана инвентаризации
 */
data class InventoryUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
    val exportMessage: String? = null,
    val importMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sortType: SortType = SortType.NAME_ASC,
    val totalProducts: Int = 0,
    val totalScannedItems: Int = 0,
    val searchResultsCount: Int = 0,
    val filteredProductsCount: Int = 0
)

/**
 * Типы сортировки
 */
enum class SortType {
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    CATEGORY
}

/**
 * Форматы экспорта
 */
enum class ExportFormat {
    CSV,
    EXCEL,
    JSON
}

/**
 * Форматы импорта
 */
enum class ImportFormat {
    CSV,
    EXCEL,
    JSON
}

/**
 * Статистика инвентаря
 */
data class InventoryStats(
    val totalProducts: Int,
    val totalValue: Double,
    val categoriesCount: Int,
    val productsWithPrice: Int,
    val productsWithoutPrice: Int
)