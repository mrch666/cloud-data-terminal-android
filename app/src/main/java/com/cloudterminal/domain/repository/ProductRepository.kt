package com.cloudterminal.domain.repository

import com.cloudterminal.domain.models.Product

interface ProductRepository {
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun saveProducts(products: List<Product>)
    suspend fun getAllProducts(): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    suspend fun deleteAllProducts()
    suspend fun getProductsCount(): Int
}