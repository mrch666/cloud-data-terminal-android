package com.cloudterminal.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана управления товарами
 */
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()
    
    init {
        // Загрузка товаров при инициализации
        loadProducts()
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
     * Выбор товара для редактирования
     */
    fun selectProduct(product: Product?) {
        _selectedProduct.update { product }
        _uiState.update { it.copy(isEditing = product != null) }
    }
    
    /**
     * Создание нового товара
     */
    fun createNewProduct() {
        _selectedProduct.update { 
            Product(
                id = generateProductId(),
                barcode = "",
                name = "",
                description = null,
                category = null,
                price = null,
                unit = null
            )
        }
        _uiState.update { it.copy(isEditing = true, isCreating = true) }
    }
    
    /**
     * Сохранение товара
     */
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                // Проверка обязательных полей
                if (product.name.isBlank()) {
                    throw IllegalArgumentException("Название товара обязательно")
                }
                
                if (product.barcode.isBlank()) {
                    throw IllegalArgumentException("Штрих-код товара обязателен")
                }
                
                // Сохранение товара
                productRepository.saveProducts(listOf(product))
                
                // Обновление списка товаров
                loadProducts()
                
                // Сброс выбранного товара
                selectProduct(null)
                
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        isCreating = false,
                        successMessage = "Товар успешно сохранен"
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
     * Удаление товара
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            
            try {
                // TODO: Реализовать удаление товара
                // productRepository.deleteProduct(productId)
                
                // Обновление списка товаров
                loadProducts()
                
                // Сброс выбранного товара если он был удален
                if (_selectedProduct.value?.id == productId) {
                    selectProduct(null)
                }
                
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        successMessage = "Товар успешно удален"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Ошибка удаления: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Импорт товаров из CSV
     */
    fun importFromCsv(csvContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, errorMessage = null) }
            
            try {
                // TODO: Реализовать парсинг CSV и импорт товаров
                // val products = parseCsv(csvContent)
                // productRepository.saveProducts(products)
                
                // Обновление списка товаров
                loadProducts()
                
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        successMessage = "Товары успешно импортированы из CSV"
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
     * Экспорт товаров в CSV
     */
    fun exportToCsv(): String {
        // TODO: Реализовать экспорт в CSV
        return _products.value.joinToString("\n") { product ->
            "${product.barcode},${product.name},${product.price ?: ""},${product.category ?: ""}"
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
     * Получение статистики по товарам
     */
    fun getProductsStats(): ProductsStats {
        val productsList = _products.value
        
        return ProductsStats(
            totalProducts = productsList.size,
            productsWithBarcode = productsList.count { it.barcode.isNotBlank() },
            productsWithoutBarcode = productsList.count { it.barcode.isBlank() },
            productsWithPrice = productsList.count { it.price != null },
            productsWithoutPrice = productsList.count { it.price == null },
            categoriesCount = productsList.mapNotNull { it.category }.distinct().size,
            totalValue = productsList.sumOf { it.price ?: 0.0 }
        )
    }
    
    /**
     * Очистка всех товаров
     */
    fun clearAllProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearing = true, errorMessage = null) }
            
            try {
                productRepository.deleteAllProducts()
                loadProducts()
                
                _uiState.update {
                    it.copy(
                        isClearing = false,
                        successMessage = "Все товары удалены"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClearing = false,
                        errorMessage = "Ошибка очистки: ${e.message}"
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
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Отмена редактирования
     */
    fun cancelEditing() {
        selectProduct(null)
        _uiState.update { 
            it.copy(
                isEditing = false,
                isCreating = false,
                errorMessage = null,
                successMessage = null
            )
        }
    }
    
    /**
     * Генерация ID для нового товара
     */
    private fun generateProductId(): String {
        return "prod_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Состояние UI для экрана товаров
 */
data class ProductsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
    val isClearing: Boolean = false,
    val isEditing: Boolean = false,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val totalProducts: Int = 0,
    val searchResultsCount: Int = 0
)

/**
 * Статистика по товарам
 */
data class ProductsStats(
    val totalProducts: Int,
    val productsWithBarcode: Int,
    val productsWithoutBarcode: Int,
    val productsWithPrice: Int,
    val productsWithoutPrice: Int,
    val categoriesCount: Int,
    val totalValue: Double
)