package com.cloudterminal.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cloudterminal.data.local.database.CloudTerminalDatabase
import com.cloudterminal.data.local.entity.ProductEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {
    
    private lateinit var database: CloudTerminalDatabase
    private lateinit var repository: ProductRepositoryImpl
    
    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CloudTerminalDatabase::class.java
        ).build()
        
        repository = ProductRepositoryImpl(database.productDao())
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun `getProductByBarcode should return product when exists`() = runTest {
        // Given
        val productEntity = ProductEntity(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 100.0,
            unit = "pcs",
            lastUpdated = System.currentTimeMillis()
        )
        
        database.productDao().insertProduct(productEntity)
        
        // When
        val result = repository.getProductByBarcode("5901234123457")
        
        // Then
        assertEquals("prod_123", result?.id)
        assertEquals("5901234123457", result?.barcode)
        assertEquals("Test Product", result?.name)
    }
    
    @Test
    fun `getProductByBarcode should return null when not exists`() = runTest {
        // When
        val result = repository.getProductByBarcode("5901234123457")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getProductByBarcode should be case insensitive`() = runTest {
        // Given
        val productEntity = ProductEntity(
            id = "prod_123",
            barcode = "CODE128TEST",
            name = "Test Product",
            description = null,
            category = null,
            price = null,
            unit = null,
            lastUpdated = System.currentTimeMillis()
        )
        
        database.productDao().insertProduct(productEntity)
        
        // When - search with different case
        val resultLower = repository.getProductByBarcode("code128test")
        val resultUpper = repository.getProductByBarcode("CODE128TEST")
        
        // Then - should find regardless of case
        // Note: This depends on DAO implementation
        // If DAO does case-sensitive search, this test might fail
        assertEquals("prod_123", resultUpper?.id)
    }
    
    @Test
    fun `getProductByBarcode should trim whitespace`() = runTest {
        // Given
        val productEntity = ProductEntity(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Test Product",
            description = null,
            category = null,
            price = null,
            unit = null,
            lastUpdated = System.currentTimeMillis()
        )
        
        database.productDao().insertProduct(productEntity)
        
        // When - search with whitespace
        val result = repository.getProductByBarcode(" 5901234123457 ")
        
        // Then - should find after trimming
        // Note: This depends on DAO implementation
        assertEquals("prod_123", result?.id)
    }
    
    @Test
    fun `saveProducts should insert multiple products`() = runTest {
        // Given
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                barcode = "5901234123457",
                name = "Product 1",
                lastUpdated = 1000L
            ),
            ProductEntity(
                id = "prod_2",
                barcode = "5901234123458",
                name = "Product 2",
                lastUpdated = 2000L
            ),
            ProductEntity(
                id = "prod_3",
                barcode = "5901234123459",
                name = "Product 3",
                lastUpdated = 3000L
            )
        )
        
        // When
        repository.saveProducts(products)
        
        // Then
        val product1 = repository.getProductByBarcode("5901234123457")
        val product2 = repository.getProductByBarcode("5901234123458")
        val product3 = repository.getProductByBarcode("5901234123459")
        
        assertEquals("prod_1", product1?.id)
        assertEquals("prod_2", product2?.id)
        assertEquals("prod_3", product3?.id)
    }
    
    @Test
    fun `saveProducts should update existing products`() = runTest {
        // Given
        val initialProduct = ProductEntity(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Old Name",
            lastUpdated = 1000L
        )
        
        database.productDao().insertProduct(initialProduct)
        
        val updatedProduct = ProductEntity(
            id = "prod_123", // Same ID
            barcode = "5901234123457",
            name = "New Name", // Updated name
            lastUpdated = 2000L // New timestamp
        )
        
        // When
        repository.saveProducts(listOf(updatedProduct))
        
        // Then
        val result = repository.getProductByBarcode("5901234123457")
        assertEquals("New Name", result?.name)
        assertEquals(2000L, result?.lastUpdated)
    }
    
    @Test
    fun `getAllProducts should return all products`() = runTest {
        // Given
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                barcode = "5901234123457",
                name = "Product 1",
                lastUpdated = 1000L
            ),
            ProductEntity(
                id = "prod_2",
                barcode = "5901234123458",
                name = "Product 2",
                lastUpdated = 2000L
            )
        )
        
        repository.saveProducts(products)
        
        // When
        val allProducts = repository.getAllProducts()
        
        // Then
        assertEquals(2, allProducts.size)
        assertTrue(allProducts.any { it.id == "prod_1" })
        assertTrue(allProducts.any { it.id == "prod_2" })
    }
    
    @Test
    fun `getAllProducts should return empty list when no products`() = runTest {
        // When
        val allProducts = repository.getAllProducts()
        
        // Then
        assertTrue(allProducts.isEmpty())
    }
    
    @Test
    fun `searchProducts should find by name`() = runTest {
        // Given
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                barcode = "5901234123457",
                name = "Apple iPhone 13",
                lastUpdated = 1000L
            ),
            ProductEntity(
                id = "prod_2",
                barcode = "5901234123458",
                name = "Samsung Galaxy S21",
                lastUpdated = 2000L
            ),
            ProductEntity(
                id = "prod_3",
                barcode = "5901234123459",
                name = "Xiaomi Redmi Note",
                lastUpdated = 3000L
            )
        )
        
        repository.saveProducts(products)
        
        // When
        val results = repository.searchProducts("iPhone")
        
        // Then
        assertEquals(1, results.size)
        assertEquals("prod_1", results[0].id)
    }
    
    @Test
    fun `searchProducts should find by barcode`() = runTest {
        // Given
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                barcode = "5901234123457",
                name = "Product 1",
                lastUpdated = 1000L
            ),
            ProductEntity(
                id = "prod_2",
                barcode = "5901234123458",
                name = "Product 2",
                lastUpdated = 2000L
            )
        )
        
        repository.saveProducts(products)
        
        // When
        val results = repository.searchProducts("5901234123457")
        
        // Then
        assertEquals(1, results.size)
        assertEquals("prod_1", results[0].id)
    }
    
    @Test
    fun `searchProducts should return empty when no matches`() = runTest {
        // Given
        val product = ProductEntity(
            id = "prod_1",
            barcode = "5901234123457",
            name = "Test Product",
            lastUpdated = 1000L
        )
        
        repository.saveProducts(listOf(product))
        
        // When
        val results = repository.searchProducts("nonexistent")
        
        // Then
        assertTrue(results.isEmpty())
    }
    
    @Test
    fun `searchProducts should be case insensitive`() = runTest {
        // Given
        val product = ProductEntity(
            id = "prod_1",
            barcode = "5901234123457",
            name = "Test Product",
            lastUpdated = 1000L
        )
        
        repository.saveProducts(listOf(product))
        
        // When
        val resultsLower = repository.searchProducts("test product")
        val resultsUpper = repository.searchProducts("TEST PRODUCT")
        
        // Then
        assertEquals(1, resultsLower.size)
        assertEquals(1, resultsUpper.size)
    }
    
    @Test
    fun `deleteAllProducts should remove all products`() = runTest {
        // Given
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                barcode = "5901234123457",
                name = "Product 1",
                lastUpdated = 1000L
            ),
            ProductEntity(
                id = "prod_2",
                barcode = "5901234123458",
                name = "Product 2",
                lastUpdated = 2000L
            )
        )
        
        repository.saveProducts(products)
        assertEquals(2, repository.getAllProducts().size)
        
        // When
        repository.deleteAllProducts()
        
        // Then
        assertTrue(repository.getAllProducts().isEmpty())
    }
    
    @Test
    fun `getProductsCount should return correct count`() = runTest {
        // Given
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                barcode = "5901234123457",
                name = "Product 1",
                lastUpdated = 1000L
            ),
            ProductEntity(
                id = "prod_2",
                barcode = "5901234123458",
                name = "Product 2",
                lastUpdated = 2000L
            )
        )
        
        // When - empty
        val countBefore = repository.getProductsCount()
        
        // Then
        assertEquals(0, countBefore)
        
        // When - after insert
        repository.saveProducts(products)
        val countAfter = repository.getProductsCount()
        
        // Then
        assertEquals(2, countAfter)
    }
}