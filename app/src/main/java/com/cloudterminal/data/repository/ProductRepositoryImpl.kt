package com.cloudterminal.data.repository

import com.cloudterminal.data.local.dao.ProductDao
import com.cloudterminal.data.local.entity.ProductEntity
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {
    
    override suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toDomain()
    }
    
    override suspend fun saveProducts(products: List<Product>) {
        val entities = products.map { it.toEntity() }
        productDao.insertProducts(entities)
    }
    
    override suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts().map { it.toDomain() }
    }
    
    override suspend fun searchProducts(query: String): List<Product> {
        return productDao.searchProducts("%$query%").map { it.toDomain() }
    }
    
    override suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }
    
    override suspend fun getProductsCount(): Int {
        return productDao.getProductsCount()
    }
    
    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            barcode = barcode,
            name = name,
            description = description,
            category = category,
            price = price,
            unit = unit,
            lastUpdated = lastUpdated
        )
    }
    
    private fun ProductEntity.toDomain(): Product {
        return Product(
            id = id,
            barcode = barcode,
            name = name,
            description = description,
            category = category,
            price = price,
            unit = unit,
            lastUpdated = lastUpdated
        )
    }
}