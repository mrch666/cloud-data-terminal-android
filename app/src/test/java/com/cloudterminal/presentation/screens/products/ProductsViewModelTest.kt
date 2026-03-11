package com.cloudterminal.presentation.screens.products

import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {
    
    private lateinit var viewModel: ProductsViewModel
    private lateinit var productRepository: ProductRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        productRepository = mock()
        viewModel = ProductsViewModel(productRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given
        val expectedState = ProductsUiState(
            isLoading = true, // Начальная загрузка
            isSaving = false,
            isDeleting = false,
            isImporting = false,
            isExporting = false,
            isClearing = false,
            isEditing = false,
            isCreating = false,
            errorMessage = null,
            successMessage = null,
            searchQuery = "",
            totalProducts = 0,
            searchResultsCount = 0
        )
        
        // When
        val actualState = viewModel.uiState.value
        
        // Then
        assert(actualState == expectedState)
    }
    
    @Test
    fun `loadProducts should load products successfully`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Product 1",
                description = "Description 1",
                category = "Category A",
                price = 100.0,
                unit = "шт."
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Product 2",
                description = "Description 2",
                category = "Category B",
                price = 200.0,
                unit = "кг"
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        
        // When
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val products = viewModel.products.value
        
        assert(!uiState.isLoading)
        assert(uiState.errorMessage == null)
        assert(uiState.totalProducts == 2)
        assert(products.size == 2)
        assert(products == testProducts)
    }
    
    @Test
    fun `loadProducts should handle error`() = runTest {
        // Given
        val errorMessage = "Database error"
        whenever(productRepository.getAllProducts()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isLoading)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains(errorMessage))
        assert(uiState.totalProducts == 0)
    }
    
    @Test
    fun `searchProducts should return correct results`() = runTest {
        // Given
        val allProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Apple iPhone",
                description = "Smartphone",
                category = "Electronics",
                price = 999.99,
                unit = "шт."
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Samsung Galaxy",
                description = "Android phone",
                category = "Electronics",
                price = 799.99,
                unit = "шт."
            ),
            Product(
                id = "prod_3",
                barcode = "5555555555555",
                name = "Banana",
                description = "Fruit",
                category = "Food",
                price = 1.99,
                unit = "кг"
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(allProducts)
        whenever(productRepository.searchProducts("apple")).thenReturn(
            listOf(allProducts[0])
        )
        
        // When
        viewModel.searchProducts("apple")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val products = viewModel.products.value
        
        assert(!uiState.isLoading)
        assert(uiState.searchQuery == "apple")
        assert(uiState.searchResultsCount == 1)
        assert(products.size == 1)
        assert(products[0].name.contains("Apple", ignoreCase = true))
    }
    
    @Test
    fun `createNewProduct should set editing state`() = runTest {
        // When
        viewModel.createNewProduct()
        
        // Then
        val uiState = viewModel.uiState.value
        val selectedProduct = viewModel.selectedProduct.value
        
        assert(uiState.isEditing)
        assert(uiState.isCreating)
        assert(selectedProduct != null)
        assert(selectedProduct?.name == "")
        assert(selectedProduct?.barcode == "")
    }
    
    @Test
    fun `selectProduct should set editing state`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        // When
        viewModel.selectProduct(product)
        
        // Then
        val uiState = viewModel.uiState.value
        val selectedProduct = viewModel.selectedProduct.value
        
        assert(uiState.isEditing)
        assert(!uiState.isCreating)
        assert(selectedProduct == product)
    }
    
    @Test
    fun `selectProduct with null should clear editing state`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        // Сначала выбираем продукт
        viewModel.selectProduct(product)
        
        // When
        viewModel.selectProduct(null)
        
        // Then
        val uiState = viewModel.uiState.value
        val selectedProduct = viewModel.selectedProduct.value
        
        assert(!uiState.isEditing)
        assert(!uiState.isCreating)
        assert(selectedProduct == null)
    }
    
    @Test
    fun `saveProduct should save successfully`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        val testProducts = listOf(product)
        whenever(productRepository.saveProducts(listOf(product))).thenReturn(Unit)
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        
        // When
        viewModel.saveProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isSaving)
        assert(!uiState.isEditing)
        assert(!uiState.isCreating)
        assert(uiState.errorMessage == null)
        assert(uiState.successMessage != null)
        assert(uiState.successMessage!!.contains("Товар успешно сохранен"))
    }
    
    @Test
    fun `saveProduct should validate required fields`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "", // Пустой штрих-код
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        // When
        viewModel.saveProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isSaving)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains("Штрих-код товара обязателен"))
    }
    
    @Test
    fun `saveProduct should handle repository error`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        val errorMessage = "Database error"
        whenever(productRepository.saveProducts(listOf(product))).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.saveProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isSaving)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains(errorMessage))
    }
    
    @Test
    fun `getCategories should return unique sorted categories`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Product 1",
                description = "Description 1",
                category = "Electronics",
                price = 100.0,
                unit = "шт."
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Product 2",
                description = "Description 2",
                category = "Food",
                price = 200.0,
                unit = "кг"
            ),
            Product(
                id = "prod_3",
                barcode = "5555555555555",
                name = "Product 3",
                description = "Description 3",
                category = "Electronics",
                price = 300.0,
                unit = "шт."
            ),
            Product(
                id = "prod_4",
                barcode = "1111111111111",
                name = "Product 4",
                description = "Description 4",
                category = "Clothing",
                price = 50.0,
                unit = "шт."
            ),
            Product(
                id = "prod_5",
                barcode = "2222222222222",
                name = "Product 5",
                description = "Description 5",
                category = null, // Без категории
                price = 75.0,
                unit = "шт."
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val categories = viewModel.getCategories()
        
        // Then
        assert(categories.size == 3) // Electronics, Food, Clothing (без null)
        assert(categories == listOf("Clothing", "Electronics", "Food")) // Отсортировано
    }
    
    @Test
    fun `getProductsStats should calculate correct statistics`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Product 1",
                description = "Description 1",
                category = "Electronics",
                price = 100.0,
                unit = "шт."
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Product 2",
                description = "Description 2",
                category = "Food",
                price = 200.0,
                unit = "кг"
            ),
            Product(
                id = "prod_3",
                barcode = "", // Без штрих-кода
                name = "Product 3",
                description = "Description 3",
                category = "Electronics",
                price = null, // Без цены
                unit = "шт."
            ),
            Product(
                id = "prod_4",
                barcode = "1111111111111",
                name = "Product 4",
                description = "Description 4",
                category = "Clothing",
                price = 50.0,
                unit = "шт."
            ),
            Product(
                id = "prod_5",
                barcode = "2222222222222",
                name = "Product 5",
                description = "Description 5",
                category = null, // Без категории
                price = 75.0,
                unit = "шт."
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val stats = viewModel.getProductsStats()
        
        // Then
        assert(stats.totalProducts == 5)
        assert(stats.productsWithBarcode == 4)
        assert(stats.productsWithoutBarcode == 1)
        assert(stats.productsWithPrice == 4)
        assert(stats.productsWithoutPrice == 1)
        assert(stats.categoriesCount == 3) // Electronics, Food, Clothing
        assert(stats.totalValue == 425.0) // 100 + 200 + 50 + 75
    }
    
    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "", // Пустой штрих-код
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        // Создаем ошибку
        viewModel.saveProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Проверяем, что ошибка есть
        assert(viewModel.uiState.value.errorMessage != null)
        
        // When
        viewModel.clearError()
        
        // Then
        assert(viewModel.uiState.value.errorMessage == null)
    }
    
    @Test
    fun `clearSuccessMessage should clear success message`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        whenever(productRepository.saveProducts(listOf(product))).thenReturn(Unit)
        whenever(productRepository.getAllProducts()).thenReturn(listOf(product))
        
        // Создаем успешное сообщение
        viewModel.saveProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Проверяем, что сообщение есть
        assert(viewModel.uiState.value.successMessage != null)
        
        // When
        viewModel.clearSuccessMessage()
        
        // Then
        assert(viewModel.uiState.value.successMessage == null)
    }
    
    @Test
    fun `cancelEditing should reset editing state`() = runTest {
        // Given
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        // Устанавливаем состояние редактирования
        viewModel.selectProduct(product)
        
        // Проверяем, что редактирование активно
        assert(viewModel.uiState.value.isEditing)
        assert(viewModel.selectedProduct.value != null)
        
        // When
        viewModel.cancelEditing()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isEditing)
        assert(!uiState.isCreating)
        assert(uiState.errorMessage == null)
        assert(uiState.successMessage == null)
        assert(viewModel.selectedProduct.value == null)
    }
    
    @Test
    fun `exportToCsv should return CSV format`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Product 1",
                description = "Description 1",
                category = "Category A",
                price = 100.0,
                unit = "шт."
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Product 2",
                description = "Description 2",
                category = "Category B",
                price = 200.0,
                unit = "кг"
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val csv = viewModel.exportToCsv()
        
        // Then
        assert(csv.contains("1234567890123,Product 1,100.0,Category A"))
        assert(csv.contains("9876543210987,Product 2,200.0,Category B"))
        assert(csv.lines().size == 2) // Две строки данных
    }
}