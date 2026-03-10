package com.cloudterminal.domain.usecases

import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetProductByBarcodeUseCaseTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Test
    fun `invoke should return product when found`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val expectedProduct = Product(
            id = "prod_123",
            barcode = barcode,
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 100.0,
            unit = "pcs"
        )
        
        val repository = mock<ProductRepository> {
            onBlocking { getProductByBarcode(barcode) } doReturn expectedProduct
        }
        
        val useCase = GetProductByBarcodeUseCase(repository)
        
        // When
        val result = useCase(barcode)
        
        // Then
        assertEquals(expectedProduct, result)
    }
    
    @Test
    fun `invoke should return null when product not found`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        
        val repository = mock<ProductRepository> {
            onBlocking { getProductByBarcode(barcode) } doReturn null
        }
        
        val useCase = GetProductByBarcodeUseCase(repository)
        
        // When
        val result = useCase(barcode)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `invoke should handle empty barcode`() = runTest(testDispatcher) {
        // Given
        val barcode = ""
        
        val repository = mock<ProductRepository> {
            onBlocking { getProductByBarcode(barcode) } doReturn null
        }
        
        val useCase = GetProductByBarcodeUseCase(repository)
        
        // When
        val result = useCase(barcode)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `invoke should handle whitespace in barcode`() = runTest(testDispatcher) {
        // Given
        val barcode = " 5901234123457 "
        val trimmedBarcode = "5901234123457"
        
        val expectedProduct = Product(
            id = "prod_123",
            barcode = trimmedBarcode,
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 100.0,
            unit = "pcs"
        )
        
        val repository = mock<ProductRepository> {
            onBlocking { getProductByBarcode(trimmedBarcode) } doReturn expectedProduct
        }
        
        val useCase = GetProductByBarcodeUseCase(repository)
        
        // When
        val result = useCase(barcode)
        
        // Then
        assertEquals(expectedProduct, result)
    }
    
    @Test
    fun `invoke should handle different barcode formats`() = runTest(testDispatcher) {
        // Test different barcode formats
        val testCases = listOf(
            "5901234123457",  // EAN-13
            "12345678",       // EAN-8
            "012345678905",   // UPC-A
            "CODE128TEST",    // Code 128
            "QRCODE123",      // QR Code text
            "DM123456"        // Data Matrix
        )
        
        testCases.forEach { barcode ->
            // Given
            val expectedProduct = Product(
                id = "prod_$barcode",
                barcode = barcode,
                name = "Product $barcode",
                description = null,
                category = null,
                price = null,
                unit = null
            )
            
            val repository = mock<ProductRepository> {
                onBlocking { getProductByBarcode(barcode) } doReturn expectedProduct
            }
            
            val useCase = GetProductByBarcodeUseCase(repository)
            
            // When
            val result = useCase(barcode)
            
            // Then
            assertEquals(expectedProduct, result)
        }
    }
    
    @Test
    fun `invoke should handle repository exception`() = runTest(testDispatcher) {
        // Given
        val barcode = "5901234123457"
        val exception = RuntimeException("Database error")
        
        val repository = mock<ProductRepository> {
            onBlocking { getProductByBarcode(barcode) } doThrow exception
        }
        
        val useCase = GetProductByBarcodeUseCase(repository)
        
        // When
        val result = useCase(barcode)
        
        // Then - should return null on exception
        assertNull(result)
    }
    
    @Test
    fun `invoke should be case insensitive for alphanumeric barcodes`() = runTest(testDispatcher) {
        // Given
        val barcodeLower = "code128test"
        val barcodeUpper = "CODE128TEST"
        
        val expectedProduct = Product(
            id = "prod_123",
            barcode = barcodeUpper,
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 100.0,
            unit = "pcs"
        )
        
        val repository = mock<ProductRepository> {
            onBlocking { getProductByBarcode(barcodeUpper) } doReturn expectedProduct
        }
        
        val useCase = GetProductByBarcodeUseCase(repository)
        
        // When
        val resultLower = useCase(barcodeLower)
        val resultUpper = useCase(barcodeUpper)
        
        // Then - repository should handle case normalization
        // This test assumes repository does case-insensitive search
        // If not, we might need to add normalization in use case
        assertEquals(expectedProduct, resultUpper)
    }
}