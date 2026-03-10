package com.cloudterminal.domain.usecases

import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.repository.ScannedItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class SaveScannedItemUseCaseTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Test
    fun `invoke should save item with valid parameters`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val quantity = 5
        val productId = "prod_123"
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, productId)
        
        // Then
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = barcode,
                quantity = quantity,
                productId = productId
            )
        )
    }
    
    @Test
    fun `invoke should save item without productId`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val quantity = 1
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, null)
        
        // Then
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = barcode,
                quantity = quantity,
                productId = null
            )
        )
    }
    
    @Test
    fun `invoke should handle zero quantity`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val quantity = 0
        val productId = "prod_123"
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, productId)
        
        // Then - should still save even with zero quantity
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = barcode,
                quantity = quantity,
                productId = productId
            )
        )
    }
    
    @Test
    fun `invoke should handle negative quantity`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val quantity = -5
        val productId = "prod_123"
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, productId)
        
        // Then - should still save negative quantity (could be returns/removals)
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = barcode,
                quantity = quantity,
                productId = productId
            )
        )
    }
    
    @Test
    fun `invoke should trim barcode whitespace`() = runTest(testDispatcher) {
        // Given
        val barcode = " 5901234123457 "
        val trimmedBarcode = "5901234123457"
        val quantity = 1
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, null)
        
        // Then
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = trimmedBarcode,
                quantity = quantity,
                productId = null
            )
        )
    }
    
    @Test
    fun `invoke should not save empty barcode`() = runTest(testDispatcher) {
        // Given
        val barcode = ""
        val quantity = 1
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, null)
        
        // Then - should not save empty barcode
        verifyNoInteractions(repository)
    }
    
    @Test
    fun `invoke should not save whitespace-only barcode`() = runTest(testDispatcher) {
        // Given
        val barcode = "   "
        val quantity = 1
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, null)
        
        // Then - should not save whitespace-only barcode
        verifyNoInteractions(repository)
    }
    
    @Test
    fun `invoke should handle large quantity`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val quantity = Int.MAX_VALUE
        val productId = "prod_123"
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, productId)
        
        // Then
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = barcode,
                quantity = quantity,
                productId = productId
            )
        )
    }
    
    @Test
    fun `invoke should handle long barcode`() = runTest(testDispatcher) {
        // Given
        val barcode = "A".repeat(100) // 100 character barcode
        val quantity = 1
        
        val repository = mock<ScannedItemRepository>()
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, null)
        
        // Then
        verify(repository).saveScannedItem(
            ScannedItem(
                barcode = barcode,
                quantity = quantity,
                productId = null
            )
        )
    }
    
    @Test
    fun `invoke should handle special characters in barcode`() = runTest(testDispatcher) {
        // Test various special characters that might appear in barcodes
        val testCases = listOf(
            "590-1234-12345-7",
            "590/1234/12345/7",
            "590_1234_12345_7",
            "590.1234.12345.7",
            "590+1234+12345+7",
            "CODE-128-TEST",
            "QR_CODE_TEST",
            "DATA@MATRIX#123"
        )
        
        testCases.forEach { barcode ->
            // Given
            val quantity = 1
            
            val repository = mock<ScannedItemRepository>()
            val useCase = SaveScannedItemUseCase(repository)
            
            // When
            useCase(barcode, quantity, null)
            
            // Then
            verify(repository).saveScannedItem(
                ScannedItem(
                    barcode = barcode,
                    quantity = quantity,
                    productId = null
                )
            )
        }
    }
    
    @Test
    fun `invoke should handle repository exception`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val quantity = 1
        val exception = RuntimeException("Database error")
        
        val repository = mock<ScannedItemRepository> {
            onBlocking { saveScannedItem(any()) } doThrow exception
        }
        
        val useCase = SaveScannedItemUseCase(repository)
        
        // When
        useCase(barcode, quantity, null)
        
        // Then - should not throw, exception should be handled internally
        // Just verify the method was called
        verify(repository).saveScannedItem(any())
    }
}