package com.cloudterminal.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ModelTest {
    
    @Test
    fun `Product model should have correct properties`() {
        // Given
        val product = Product(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = 100.0,
            unit = "pcs"
        )
        
        // Then
        assertEquals("prod_123", product.id)
        assertEquals("5901234123457", product.barcode)
        assertEquals("Test Product", product.name)
        assertEquals("Test Description", product.description)
        assertEquals("Test Category", product.category)
        assertEquals(100.0, product.price, 0.001)
        assertEquals("pcs", product.unit)
    }
    
    @Test
    fun `Product model should handle nullable fields`() {
        // Given
        val product = Product(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Test Product",
            description = null,
            category = null,
            price = null,
            unit = null
        )
        
        // Then
        assertEquals("prod_123", product.id)
        assertEquals("5901234123457", product.barcode)
        assertEquals("Test Product", product.name)
        assertNull(product.description)
        assertNull(product.category)
        assertNull(product.price)
        assertNull(product.unit)
    }
    
    @Test
    fun `Product equals should work correctly`() {
        // Given
        val product1 = Product(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Test Product",
            description = "Desc",
            category = "Cat",
            price = 100.0,
            unit = "pcs"
        )
        
        val product2 = Product(
            id = "prod_123", // Same ID
            barcode = "5901234123458", // Different barcode
            name = "Different Product",
            description = "Different Desc",
            category = "Different Cat",
            price = 200.0,
            unit = "kg"
        )
        
        val product3 = Product(
            id = "prod_124", // Different ID
            barcode = "5901234123457", // Same barcode
            name = "Test Product",
            description = "Desc",
            category = "Cat",
            price = 100.0,
            unit = "pcs"
        )
        
        // Then
        assertEquals(product1, product2) // Same ID should be equal
        assertNotEquals(product1, product3) // Different ID should not be equal
    }
    
    @Test
    fun `Product hashCode should be based on id`() {
        // Given
        val product1 = Product(
            id = "prod_123",
            barcode = "5901234123457",
            name = "Product 1"
        )
        
        val product2 = Product(
            id = "prod_123", // Same ID
            barcode = "5901234123458", // Different barcode
            name = "Product 2" // Different name
        )
        
        // Then
        assertEquals(product1.hashCode(), product2.hashCode())
    }
    
    @Test
    fun `ScannedItem model should have correct properties`() {
        // Given
        val scannedAt = System.currentTimeMillis()
        val scannedItem = ScannedItem(
            barcode = "5901234123457",
            quantity = 5,
            productId = "prod_123",
            scannedAt = scannedAt
        )
        
        // Then
        assertEquals("5901234123457", scannedItem.barcode)
        assertEquals(5, scannedItem.quantity)
        assertEquals("prod_123", scannedItem.productId)
        assertEquals(scannedAt, scannedItem.scannedAt)
    }
    
    @Test
    fun `ScannedItem should have default values`() {
        // Given
        val scannedItem = ScannedItem(
            barcode = "5901234123457",
            quantity = 1
        )
        
        // Then
        assertEquals("5901234123457", scannedItem.barcode)
        assertEquals(1, scannedItem.quantity)
        assertNull(scannedItem.productId)
        assertTrue(scannedItem.scannedAt > 0) // Should be set to current time
    }
    
    @Test
    fun `ScannedItem copy should work correctly`() {
        // Given
        val original = ScannedItem(
            barcode = "5901234123457",
            quantity = 5,
            productId = "prod_123",
            scannedAt = 1000L
        )
        
        // When
        val copy = original.copy(
            quantity = 10,
            productId = "prod_456"
        )
        
        // Then
        assertEquals("5901234123457", copy.barcode) // Same
        assertEquals(10, copy.quantity) // Changed
        assertEquals("prod_456", copy.productId) // Changed
        assertEquals(1000L, copy.scannedAt) // Same
    }
    
    @Test
    fun `ScannedItem equals should work correctly`() {
        // Given
        val item1 = ScannedItem(
            barcode = "5901234123457",
            quantity = 5,
            productId = "prod_123",
            scannedAt = 1000L
        )
        
        val item2 = ScannedItem(
            barcode = "5901234123457", // Same barcode
            quantity = 5, // Same quantity
            productId = "prod_123", // Same productId
            scannedAt = 1000L // Same timestamp
        )
        
        val item3 = ScannedItem(
            barcode = "5901234123458", // Different barcode
            quantity = 5,
            productId = "prod_123",
            scannedAt = 1000L
        )
        
        // Then
        assertEquals(item1, item2)
        assertNotEquals(item1, item3)
    }
    
    @Test
    fun `BarcodeFormat enum should have correct values`() {
        // Test all barcode formats
        assertEquals(BarcodeFormat.EAN_13, BarcodeFormat.valueOf("EAN_13"))
        assertEquals(BarcodeFormat.EAN_8, BarcodeFormat.valueOf("EAN_8"))
        assertEquals(BarcodeFormat.UPC_A, BarcodeFormat.valueOf("UPC_A"))
        assertEquals(BarcodeFormat.UPC_E, BarcodeFormat.valueOf("UPC_E"))
        assertEquals(BarcodeFormat.CODE_39, BarcodeFormat.valueOf("CODE_39"))
        assertEquals(BarcodeFormat.CODE_93, BarcodeFormat.valueOf("CODE_93"))
        assertEquals(BarcodeFormat.CODE_128, BarcodeFormat.valueOf("CODE_128"))
        assertEquals(BarcodeFormat.QR_CODE, BarcodeFormat.valueOf("QR_CODE"))
        assertEquals(BarcodeFormat.DATA_MATRIX, BarcodeFormat.valueOf("DATA_MATRIX"))
        assertEquals(BarcodeFormat.PDF_417, BarcodeFormat.valueOf("PDF_417"))
    }
    
    @Test
    fun `BarcodeFormat should have display names`() {
        // Test display names
        assertEquals("EAN-13", BarcodeFormat.EAN_13.displayName)
        assertEquals("EAN-8", BarcodeFormat.EAN_8.displayName)
        assertEquals("UPC-A", BarcodeFormat.UPC_A.displayName)
        assertEquals("QR Code", BarcodeFormat.QR_CODE.displayName)
    }
    
    @Test
    fun `SyncStatus enum should have correct values`() {
        assertEquals(SyncStatus.PENDING, SyncStatus.valueOf("PENDING"))
        assertEquals(SyncStatus.SYNCING, SyncStatus.valueOf("SYNCING"))
        assertEquals(SyncStatus.COMPLETED, SyncStatus.valueOf("COMPLETED"))
        assertEquals(SyncStatus.FAILED, SyncStatus.valueOf("FAILED"))
    }
    
    @Test
    fun `SyncSession model should have correct properties`() {
        // Given
        val session = SyncSession(
            id = "session_123",
            name = "Morning Inventory",
            createdAt = 1000L,
            itemCount = 50,
            isCompleted = false,
            syncedAt = null,
            syncStatus = SyncStatus.PENDING
        )
        
        // Then
        assertEquals("session_123", session.id)
        assertEquals("Morning Inventory", session.name)
        assertEquals(1000L, session.createdAt)
        assertEquals(50, session.itemCount)
        assertFalse(session.isCompleted)
        assertNull(session.syncedAt)
        assertEquals(SyncStatus.PENDING, session.syncStatus)
    }
    
    @Test
    fun `SyncSession copy with status update`() {
        // Given
        val original = SyncSession(
            id = "session_123",
            name = "Test Session",
            createdAt = 1000L,
            itemCount = 10,
            isCompleted = false,
            syncedAt = null,
            syncStatus = SyncStatus.PENDING
        )
        
        // When
        val updated = original.copy(
            isCompleted = true,
            syncedAt = 2000L,
            syncStatus = SyncStatus.COMPLETED
        )
        
        // Then
        assertEquals("session_123", updated.id) // Same
        assertTrue(updated.isCompleted) // Changed
        assertEquals(2000L, updated.syncedAt) // Changed
        assertEquals(SyncStatus.COMPLETED, updated.syncStatus) // Changed
    }
    
    @Test
    fun `AppSettings model should have correct properties`() {
        // Given
        val settings = AppSettings(
            apiUrl = "https://api.example.com",
            apiKey = "test_key_123",
            syncInterval = 300,
            autoUpload = true,
            enableFlash = false,
            enableSound = true,
            barcodeFormats = setOf(BarcodeFormat.EAN_13, BarcodeFormat.CODE_128)
        )
        
        // Then
        assertEquals("https://api.example.com", settings.apiUrl)
        assertEquals("test_key_123", settings.apiKey)
        assertEquals(300, settings.syncInterval)
        assertTrue(settings.autoUpload)
        assertFalse(settings.enableFlash)
        assertTrue(settings.enableSound)
        assertEquals(2, settings.barcodeFormats.size)
        assertTrue(settings.barcodeFormats.contains(BarcodeFormat.EAN_13))
        assertTrue(settings.barcodeFormats.contains(BarcodeFormat.CODE_128))
    }
    
    @Test
    fun `AppSettings default values`() {
        // Given
        val settings = AppSettings()
        
        // Then
        assertEquals("", settings.apiUrl)
        assertEquals("", settings.apiKey)
        assertEquals(300, settings.syncInterval) // 5 minutes default
        assertTrue(settings.autoUpload) // Default true
        assertFalse(settings.enableFlash) // Default false
        assertTrue(settings.enableSound) // Default true
        assertTrue(settings.barcodeFormats.isNotEmpty()) // Should have defaults
    }
    
    @Test
    fun `AppSettings copy with partial update`() {
        // Given
        val original = AppSettings(
            apiUrl = "https://old.example.com",
            apiKey = "old_key",
            syncInterval = 300,
            autoUpload = true
        )
        
        // When
        val updated = original.copy(
            apiUrl = "https://new.example.com",
            apiKey = "new_key"
        )
        
        // Then
        assertEquals("https://new.example.com", updated.apiUrl) // Changed
        assertEquals("new_key", updated.apiKey) // Changed
        assertEquals(300, updated.syncInterval) // Same
        assertTrue(updated.autoUpload) // Same
    }
}