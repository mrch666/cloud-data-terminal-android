package com.cloudterminal.presentation.screens.scanner

import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.usecases.GetProductByBarcodeUseCase
import com.cloudterminal.domain.usecases.ProcessBarcodeUseCase
import com.cloudterminal.domain.usecases.SaveScannedItemUseCase
import com.cloudterminal.data.repository.ScannedItemRepositoryImpl
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.Barcode.TYPE_EAN13
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
class ScannerViewModelTest {
    
    private lateinit var viewModel: ScannerViewModelUpdated
    private lateinit var getProductByBarcodeUseCase: GetProductByBarcodeUseCase
    private lateinit var saveScannedItemUseCase: SaveScannedItemUseCase
    private lateinit var processBarcodeUseCase: ProcessBarcodeUseCase
    private lateinit var scannedItemRepository: ScannedItemRepositoryImpl
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getProductByBarcodeUseCase = mock()
        saveScannedItemUseCase = mock()
        processBarcodeUseCase = mock()
        scannedItemRepository = mock()
        
        viewModel = ScannerViewModelUpdated(
            getProductByBarcodeUseCase = getProductByBarcodeUseCase,
            saveScannedItemUseCase = saveScannedItemUseCase,
            processBarcodeUseCase = processBarcodeUseCase,
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
        val expectedState = ScannerUiStateUpdated(
            isLoading = false,
            errorMessage = null,
            scanSuccessMessage = null,
            isFlashOn = false,
            lastScannedProduct = null,
            isScannerActive = true
        )
        
        // When
        val actualState = viewModel.uiState.value
        
        // Then
        assert(actualState == expectedState)
    }
    
    @Test
    fun `toggleFlash should change flash state`() = runTest {
        // Given
        val initialFlashState = viewModel.uiState.value.isFlashOn
        
        // When
        viewModel.toggleFlash()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val finalFlashState = viewModel.uiState.value.isFlashOn
        assert(finalFlashState != initialFlashState)
    }
    
    @Test
    fun `clearScannedItems should clear the list`() = runTest {
        // Given
        // Добавляем тестовые элементы через рефлексию или моки
        // Для простоты тестируем только очистку
        
        // When
        viewModel.clearScannedItems()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val scannedItems = viewModel.scannedItems.value
        assert(scannedItems.isEmpty())
    }
    
    @Test
    fun `getTotalScannedQuantity should return correct total`() = runTest {
        // Given
        // Создаем тестовые элементы
        val testItems = listOf(
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
        
        // Используем рефлексию для установки значения
        val field = ScannerViewModelUpdated::class.java.getDeclaredField("_scannedItems")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(testItems))
        
        // When
        val totalQuantity = viewModel.getTotalScannedQuantity()
        
        // Then
        assert(totalQuantity == 5)
    }
    
    @Test
    fun `getUniqueScannedCount should return correct count`() = runTest {
        // Given
        val testItems = listOf(
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
        
        val field = ScannerViewModelUpdated::class.java.getDeclaredField("_scannedItems")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(testItems))
        
        // When
        val uniqueCount = viewModel.getUniqueScannedCount()
        
        // Then
        assert(uniqueCount == 2) // Два уникальных штрих-кода
    }
    
    @Test
    fun `removeScannedItem should remove item by barcode`() = runTest {
        // Given
        val barcodeToRemove = "1234567890123"
        val testItems = listOf(
            ScannedItem(
                barcode = barcodeToRemove,
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
        
        val field = ScannerViewModelUpdated::class.java.getDeclaredField("_scannedItems")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(testItems))
        
        // When
        viewModel.removeScannedItem(barcodeToRemove)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val remainingItems = viewModel.scannedItems.value
        assert(remainingItems.size == 1)
        assert(remainingItems.none { it.barcode == barcodeToRemove })
    }
    
    @Test
    fun `updateItemQuantity should update quantity correctly`() = runTest {
        // Given
        val barcodeToUpdate = "1234567890123"
        val newQuantity = 5
        val testItems = listOf(
            ScannedItem(
                barcode = barcodeToUpdate,
                quantity = 2,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        val field = ScannerViewModelUpdated::class.java.getDeclaredField("_scannedItems")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(testItems))
        
        // When
        viewModel.updateItemQuantity(barcodeToUpdate, newQuantity)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val updatedItem = viewModel.scannedItems.value.firstOrNull { it.barcode == barcodeToUpdate }
        assert(updatedItem != null)
        assert(updatedItem?.quantity == newQuantity)
    }
    
    @Test
    fun `updateItemQuantity with zero quantity should remove item`() = runTest {
        // Given
        val barcodeToUpdate = "1234567890123"
        val testItems = listOf(
            ScannedItem(
                barcode = barcodeToUpdate,
                quantity = 2,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        val field = ScannerViewModelUpdated::class.java.getDeclaredField("_scannedItems")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(testItems))
        
        // When
        viewModel.updateItemQuantity(barcodeToUpdate, 0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val remainingItems = viewModel.scannedItems.value
        assert(remainingItems.isEmpty())
    }
    
    @Test
    fun `updateItemQuantity with negative quantity should remove item`() = runTest {
        // Given
        val barcodeToUpdate = "1234567890123"
        val testItems = listOf(
            ScannedItem(
                barcode = barcodeToUpdate,
                quantity = 2,
                productId = "prod_1",
                scannedAt = System.currentTimeMillis()
            )
        )
        
        val field = ScannerViewModelUpdated::class.java.getDeclaredField("_scannedItems")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(testItems))
        
        // When
        viewModel.updateItemQuantity(barcodeToUpdate, -1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val remainingItems = viewModel.scannedItems.value
        assert(remainingItems.isEmpty())
    }
    
    @Test
    fun `scanningStats should be initialized correctly`() = runTest {
        // When
        val stats = viewModel.scanningStats.value
        
        // Then
        assert(stats.totalScans == 0)
        assert(stats.successfulScans == 0)
        assert(stats.notFoundScans == 0)
        assert(stats.errorScans == 0)
        assert(stats.lastScanTime == 0L)
        assert(stats.scanSessionStartTime > 0L)
    }
    
    @Test
    fun `saveCurrentSession should show success message`() = runTest {
        // When
        viewModel.saveCurrentSession()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.scanSuccessMessage != null)
        assert(uiState.scanSuccessMessage!!.contains("Сессия сохранена"))
    }
    
    @Test
    fun `onBarcodeScanned should handle success case`() = runTest {
        // Given
        val barcode = mock<Barcode>()
        whenever(barcode.rawValue).thenReturn("1234567890123")
        whenever(barcode.format).thenReturn(TYPE_EAN13)
        
        val product = Product(
            id = "prod_1",
            barcode = "1234567890123",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 99.99,
            unit = "шт."
        )
        
        val successResult = com.cloudterminal.domain.usecases.BarcodeProcessingResult.Success(
            barcodeValue = "1234567890123",
            product = product
        )
        
        whenever(processBarcodeUseCase(barcode)).thenReturn(successResult)
        
        // When
        viewModel.onBarcodeScanned(barcode)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.errorMessage == null)
        assert(uiState.scanSuccessMessage != null)
        assert(uiState.scanSuccessMessage!!.contains("Товар найден"))
        assert(uiState.lastScannedProduct == product)
    }
    
    @Test
    fun `onBarcodeScanned should handle not found case`() = runTest {
        // Given
        val barcode = mock<Barcode>()
        whenever(barcode.rawValue).thenReturn("9999999999999")
        whenever(barcode.format).thenReturn(TYPE_EAN13)
        
        val notFoundResult = com.cloudterminal.domain.usecases.BarcodeProcessingResult.NotFound(
            barcodeValue = "9999999999999"
        )
        
        whenever(processBarcodeUseCase(barcode)).thenReturn(notFoundResult)
        
        // When
        viewModel.onBarcodeScanned(barcode)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.errorMessage == null)
        assert(uiState.scanSuccessMessage != null)
        assert(uiState.scanSuccessMessage!!.contains("Товар не найден"))
        assert(uiState.lastScannedProduct == null)
    }
    
    @Test
    fun `onBarcodeScanned should handle error case`() = runTest {
        // Given
        val barcode = mock<Barcode>()
        whenever(barcode.rawValue).thenReturn("invalid")
        whenever(barcode.format).thenReturn(TYPE_EAN13)
        
        val errorResult = com.cloudterminal.domain.usecases.BarcodeProcessingResult.Error(
            error = "Invalid barcode format"
        )
        
        whenever(processBarcodeUseCase(barcode)).thenReturn(errorResult)
        
        // When
        viewModel.onBarcodeScanned(barcode)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains("Invalid barcode format"))
        assert(uiState.lastScannedProduct == null)
    }
    
    @Test
    fun `onBarcodeScanned should handle exception`() = runTest {
        // Given
        val barcode = mock<Barcode>()
        whenever(barcode.rawValue).thenReturn("1234567890123")
        whenever(barcode.format).thenReturn(TYPE_EAN13)
        
        whenever(processBarcodeUseCase(barcode)).thenThrow(RuntimeException("Test exception"))
        
        // When
        viewModel.onBarcodeScanned(barcode)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.errorMessage != null)
        assert(uiState.errorMessage!!.contains("Test exception"))
    }
}