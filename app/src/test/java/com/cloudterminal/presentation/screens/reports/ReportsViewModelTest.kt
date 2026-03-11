package com.cloudterminal.presentation.screens.reports

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
class ReportsViewModelTest {
    
    private lateinit var viewModel: ReportsViewModel
    private lateinit var productRepository: ProductRepository
    private lateinit var scannedItemRepository: ScannedItemRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        productRepository = mock()
        scannedItemRepository = mock()
        
        viewModel = ReportsViewModel(
            productRepository = productRepository,
            scannedItemRepository = scannedItemRepository
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        val expectedState = ReportsUiState(
            isLoading = true, // Начальная загрузка
            isExporting = false,
            errorMessage = null,
            successMessage = null,
            selectedPeriod = ReportPeriod.LAST_30_DAYS,
            totalProducts = 0,
            totalScans = 0
        )
        
        // When
        val actualState = viewModel.uiState.value
        
        // Then
        assert(actualState == expectedState)
    }
    
    @Test
    fun `loadReportsData should load data successfully`() = runTest {
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
        
        val testScannedItems = listOf(
            ScannedItem(
                barcode = "1234567890123",
                quantity = 2,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            ),
            ScannedItem(
                barcode = "9876543210987",
                quantity = 3,
                productId = "prod_2",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(testScannedItems)
        
        // When
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        val reports = viewModel.reports.value
        val stats = viewModel.scanStats.value
        
        assert(!uiState.isLoading)
        assert(uiState.errorMessage == null)
        assert(uiState.totalProducts == 2)
        assert(uiState.totalScans == 2)
        
        // Проверяем, что отчеты сгенерированы
        assert(reports.isNotEmpty())
        assert(reports.any { it.type == ReportType.SCAN_STATS })
        assert(reports.any { it.type == ReportType.PRODUCTS })
        assert(reports.any { it.type == ReportType.ACTIVITY })
        assert(reports.any { it.type == ReportType.EFFICIENCY })
        
        // Проверяем статистику
        assert(stats.totalScans == 2)
        assert(stats.uniqueProducts == 2)
        assert(stats.totalQuantity == 5)
    }
    
    @Test
    fun `loadReportsData should handle error`() = runTest {
        // Given
        val errorMessage = "Database error"
        whenever(productRepository.getAllProducts()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        
        assert(!uiState.isLoading)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains(errorMessage))
    }
    
    @Test
    fun `generateScanReport should create correct report`() = runTest {
        // Given
        val scannedItems = listOf(
            ScannedItem(
                barcode = "1234567890123",
                quantity = 2,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            ),
            ScannedItem(
                barcode = "9876543210987",
                quantity = 3,
                productId = "prod_2",
                scannedAt = System.currentTimeMillis()
            ),
            ScannedItem(
                barcode = "1234567890123", // Дубликат
                quantity = 1,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        // Загружаем данные
        whenever(productRepository.getAllProducts()).thenReturn(emptyList())
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(scannedItems)
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val reports = viewModel.reports.value
        val scanReport = reports.find { it.type == ReportType.SCAN_STATS }
        
        // Then
        assert(scanReport != null)
        assert(scanReport!!.title == "Отчет по сканированиям")
        assert(scanReport.data["total_scans"] == "3")
        assert(scanReport.data["unique_products"] == "2")
        assert(scanReport.data["total_quantity"] == "6")
    }
    
    @Test
    fun `generateProductsReport should create correct report`() = runTest {
        // Given
        val products = listOf(
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
                price = null, // Без цены
                unit = "шт."
            )
        )
        
        // Загружаем данные
        whenever(productRepository.getAllProducts()).thenReturn(products)
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(emptyList())
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val reports = viewModel.reports.value
        val productsReport = reports.find { it.type == ReportType.PRODUCTS }
        
        // Then
        assert(productsReport != null)
        assert(productsReport!!.title == "Отчет по товарам")
        assert(productsReport.data["total_products"] == "3")
        assert(productsReport.data["with_price"] == "2")
        assert(productsReport.data["without_price"] == "1")
        assert(productsReport.data["total_value"] == "300.00")
        assert(productsReport.data["categories_count"] == "2")
        assert(productsReport.data["avg_price"] == "150.00")
    }
    
    @Test
    fun `generateEfficiencyReport should calculate efficiency correctly`() = runTest {
        // Given
        val products = listOf(
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
        
        val scannedItems = listOf(
            ScannedItem(
                barcode = "1234567890123", // Найден
                quantity = 2,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            ),
            ScannedItem(
                barcode = "9999999999999", // Не найден
                quantity = 1,
                productId = null,
                scannedAt = System.currentTimeMillis()
            ),
            ScannedItem(
                barcode = "9876543210987", // Найден
                quantity = 3,
                productId = "prod_2",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        // Загружаем данные
        whenever(productRepository.getAllProducts()).thenReturn(products)
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(scannedItems)
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val reports = viewModel.reports.value
        val efficiencyReport = reports.find { it.type == ReportType.EFFICIENCY }
        
        // Then
        assert(efficiencyReport != null)
        assert(efficiencyReport!!.title == "Отчет по эффективности")
        assert(efficiencyReport.data["total_scans"] == "3")
        assert(efficiencyReport.data["found_products"] == "2")
        assert(efficiencyReport.data["not_found"] == "1")
        assert(efficiencyReport.data["efficiency"] == "66.7%")
        assert(efficiencyReport.data["coverage"] == "100.0%") // Все товары найдены
    }
    
    @Test
    fun `updateReportPeriod should update period`() = runTest {
        // Given
        val newPeriod = ReportPeriod.LAST_7_DAYS
        
        // When
        viewModel.updateReportPeriod(newPeriod)
        
        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.selectedPeriod == newPeriod)
    }
    
    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        // Создаем состояние с ошибкой через рефлексию
        val field = ReportsViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        val mutableStateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<ReportsUiState>
        mutableStateFlow.value = ReportsUiState(errorMessage = "Test error")
        
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
        // Создаем состояние с сообщением через рефлексию
        val field = ReportsViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        val mutableStateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<ReportsUiState>
        mutableStateFlow.value = ReportsUiState(successMessage = "Test success")
        
        // Проверяем, что сообщение есть
        assert(viewModel.uiState.value.successMessage != null)
        
        // When
        viewModel.clearSuccessMessage()
        
        // Then
        assert(viewModel.uiState.value.successMessage == null)
    }
    
    @Test
    fun `refreshReports should reload data`() = runTest {
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
            )
        )
        
        val testScannedItems = listOf(
            ScannedItem(
                barcode = "1234567890123",
                quantity = 1,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(testProducts)
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(testScannedItems)
        
        // Сначала загружаем данные
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Проверяем начальное состояние
        assert(viewModel.uiState.value.totalProducts == 1)
        assert(viewModel.uiState.value.totalScans == 1)
        
        // Обновляем данные
        val updatedProducts = testProducts + listOf(
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
        
        val updatedScannedItems = testScannedItems + listOf(
            ScannedItem(
                barcode = "9876543210987",
                quantity = 2,
                productId = "prod_2",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        whenever(productRepository.getAllProducts()).thenReturn(updatedProducts)
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(updatedScannedItems)
        
        // When
        viewModel.refreshReports()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.totalProducts == 2)
        assert(uiState.totalScans == 2)
    }
    
    @Test
    fun `exportToCsv should create CSV format`() = runTest {
        // Given
        val report = Report(
            id = "test_report",
            title = "Test Report",
            type = ReportType.SCAN_STATS,
            data = mapOf(
                "total_scans" to "10",
                "unique_products" to "5",
                "total_quantity" to "25"
            ),
            generatedAt = System.currentTimeMillis()
        )
        
        // When
        val csv = viewModel.javaClass.getDeclaredMethod(
            "exportToCsv", 
            Report::class.java
        ).apply { isAccessible = true }
        .invoke(viewModel, report) as String
        
        // Then
        val lines = csv.lines()
        assert(lines.size == 4) // Заголовок + 3 строки данных
        assert(lines[0] == "Параметр,Значение")
        assert(lines.contains("total_scans,10"))
        assert(lines.contains("unique_products,5"))
        assert(lines.contains("total_quantity,25"))
    }
    
    @Test
    fun `exportToHtml should create HTML format`() = runTest {
        // Given
        val report = Report(
            id = "test_report",
            title = "Test Report",
            type = ReportType.SCAN_STATS,
            data = mapOf(
                "total_scans" to "10",
                "unique_products" to "5"
            ),
            generatedAt = 1700000000000L
        )
        
        // When
        val html = viewModel.javaClass.getDeclaredMethod(
            "exportToHtml", 
            Report::class.java
        ).apply { isAccessible = true }
        .invoke(viewModel, report) as String
        
        // Then
        assert(html.contains("<!DOCTYPE html>"))
        assert(html.contains("<title>Test Report</title>"))
        assert(html.contains("<h1>Test Report</h1>"))
        assert(html.contains("total_scans"))
        assert(html.contains("10"))
        assert(html.contains("unique_products"))
        assert(html.contains("5"))
    }
    
    @Test
    fun `calculateStatistics should calculate correctly`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        val scannedItems = listOf(
            ScannedItem(
                barcode = "1234567890123",
                quantity = 2,
                productId = "prod_1",
                scannedAt = now - (2 * oneDayMs) // 2 дня назад
            ),
            ScannedItem(
                barcode = "9876543210987",
                quantity = 3,
                productId = "prod_2",
                scannedAt = now - oneDayMs // 1 день назад
            ),
            ScannedItem(
                barcode = "5555555555555",
                quantity = 1,
                productId = "prod_3",
                scannedAt = now // Сегодня
            )
        )
        
        // Загружаем данные
        whenever(productRepository.getAllProducts()).thenReturn(emptyList())
        whenever(scannedItemRepository.getScannedItems(null)).thenReturn(scannedItems)
        viewModel.loadReportsData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val stats = viewModel.scanStats.value
        
        // Then
        assert(stats.total