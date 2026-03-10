package com.cloudterminal.domain.models

data class ScannedItem(
    val barcode: String,
    val quantity: Int = 1,
    val productId: String? = null,
    val scannedAt: Long = System.currentTimeMillis()
) {
    val productName: String? = null // Will be populated from product
    
    fun copyWithProduct(product: Product?): ScannedItem {
        return this.copy(
            productId = product?.id
        )
    }
}