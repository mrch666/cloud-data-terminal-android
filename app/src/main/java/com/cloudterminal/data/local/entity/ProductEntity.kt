package com.cloudterminal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val barcode: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val unit: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)