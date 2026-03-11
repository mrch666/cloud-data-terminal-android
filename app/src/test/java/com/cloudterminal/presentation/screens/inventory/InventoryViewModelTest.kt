package com.cloudterminal.presentation.screens.inventory

import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.repository.ProductRepository
import com.cloudterminal.domain.repository.ScannedItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {
    
    private lateinit var viewModel: InventoryViewModel
    private lateinit var productRepository: ProductRepository
    private lateinit var scannedItemRepository: ScannedItemRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        productRepository = mock()
        scannedItemRepository = mock()
        
        viewModel = InventoryViewModel(
            productRepository = productRepository,
            scannedItemRepository = scannedItemRepository
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given
        val expectedState = InventoryUiState(
            isLoading = true, // Начальная загрузка
            isExporting = false,
            isImporting = false,
            errorMessage = null,
            exportMessage = null,
            importMessage = null,
            searchQuery = "",
            selectedCategory = null,
            sortType = SortType.NAME_ASC,
            totalProducts = 0,
            totalScannedItems = 0,
            searchResultsCount = 0,
            filteredProductsCount = 0
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
    fun `searchProducts with empty query should return all products`() = runTest {
        // Given
        val allProducts = listOf(
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
        
        whenever(productRepository.getAllProducts()).thenReturn(allProducts)
        whenever(productRepository.searchProducts("")).thenReturn(allProducts)
        
        // When
        viewModel.searchProducts("")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val products = viewModel.products.value
        
        assert(!uiState.isLoading)
        assert(uiState.searchQuery == "")
        assert(uiState.searchResultsCount == 2)
        assert(products.size == 2)
    }
    
    @Test
    fun `filterByCategory should filter products correctly`() = runTest {
        // Given
        val allProducts = listOf(
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
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(allProducts)
        
        // When
        viewModel.filterByCategory("Electronics")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val products = viewModel.products.value
        
        assert(!uiState.isLoading)
        assert(uiState.selectedCategory == "Electronics")
        assert(uiState.filteredProductsCount == 2)
        assert(products.size == 2)
        assert(products.all { it.category == "Electronics" })
    }
    
    @Test
    fun `filterByCategory with null should return all products`() = runTest {
        // Given
        val allProducts = listOf(
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
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(allProducts)
        
        // When
        viewModel.filterByCategory(null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val products = viewModel.products.value
        
        assert(!uiState.isLoading)
        assert(uiState.selectedCategory == null)
        assert(uiState.filteredProductsCount == 2)
        assert(products.size == 2)
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
        
        // Загружаем продукты
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val categories = viewModel.getCategories()
        
        // Then
        assert(categories.size == 3) // Electronics, Food, Clothing (без null)
        assert(categories == listOf("Clothing", "Electronics", "Food")) // Отсортировано
    }
    
    @Test
    fun `sortProducts should sort by name ascending`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_3",
                barcode = "5555555555555",
                name = "Banana",
                description = "Fruit",
                category = "Food",
                price = 1.99,
                unit = "кг"
            ),
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Apple",
                description = "Fruit",
                category = "Food",
                price = 2.99,
                unit = "кг"
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Cherry",
                description = "Fruit",
                category = "Food",
                price = 3.99,
                unit = "кг"
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.sortProducts(SortType.NAME_ASC)
        
        // Then
        val products = viewModel.products.value
        assert(products[0].name == "Apple")
        assert(products[1].name == "Banana")
        assert(products[2].name == "Cherry")
    }
    
    @Test
    fun `sortProducts should sort by name descending`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Apple",
                description = "Fruit",
                category = "Food",
                price = 2.99,
                unit = "кг"
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Banana",
                description = "Fruit",
                category = "Food",
                price = 1.99,
                unit = "кг"
            ),
            Product(
                id = "prod_3",
                barcode = "5555555555555",
                name = "Cherry",
                description = "Fruit",
                category = "Food",
                price = 3.99,
                unit = "кг"
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.sortProducts(SortType.NAME_DESC)
        
        // Then
        val products = viewModel.products.value
        assert(products[0].name == "Cherry")
        assert(products[1].name == "Banana")
        assert(products[2].name == "Apple")
    }
    
    @Test
    fun `sortProducts should sort by price ascending`() = runTest {
        // Given
        val testProducts = listOf(
            Product(
                id = "prod_1",
                barcode = "1234567890123",
                name = "Product 1",
                description = "Description 1",
                category = "Category A",
                price = 300.0,
                unit = "шт."
            ),
            Product(
                id = "prod_2",
                barcode = "9876543210987",
                name = "Product 2",
                description = "Description 2",
                category = "Category B",
                price = 100.0,
                unit = "кг"
            ),
            Product(
                id = "prod_3",
                barcode = "5555555555555",
                name = "Product 3",
                description = "Description 3",
                category = "Category C",
                price = 200.0,
                unit = "шт."
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.sortProducts(SortType.PRICE_ASC)
        
        // Then
        val products = viewModel.products.value
        assert(products[0].price == 100.0)
        assert(products[1].price == 200.0)
        assert(products[2].price == 300.0)
    }
    
    @Test
    fun `sortProducts should sort by price descending`() = runTest {
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
                price = 300.0,
                unit = "кг"
            ),
            Product(
                id = "prod_3",
                barcode = "5555555555555",
                name = "Product 3",
                description = "Description 3",
                category = "Category C",
                price = 200.0,
                unit = "шт."
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.sortProducts(SortType.PRICE_DESC)
        
        // Then
        val products = viewModel.products.value
        assert(products[0].price == 300.0)
        assert(products[1].price == 200.0)
        assert(products[2].price == 100.0)
    }
    
    @Test
    fun `sortProducts should sort by category`() = runTest {
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
                category = "Clothing",
                price