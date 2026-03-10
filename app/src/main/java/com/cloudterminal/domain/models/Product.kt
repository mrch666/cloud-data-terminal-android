package com.cloudterminal.domain.models

data class Product(
    val id: String,
    val barcode: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val unit: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Product
        
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
}