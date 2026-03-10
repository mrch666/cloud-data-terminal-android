package com.cloudterminal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity для хранения отсканированных элементов
 */
@Entity(tableName = "scanned_items")
data class ScannedItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val barcode: String,
    val quantity: Int = 1,
    val productId: String? = null,
    val sessionId: String? = null,
    val scannedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val syncError: String? = null
) {
    companion object {
        /**
         * Создание entity из доменной модели
         */
        fun fromDomain(
            barcode: String,
            quantity: Int = 1,
            productId: String? = null,
            sessionId: String? = null
        ): ScannedItemEntity {
            return ScannedItemEntity(
                barcode = barcode,
                quantity = quantity,
                productId = productId,
                sessionId = sessionId
            )
        }
    }
    
    /**
     * Преобразование в доменную модель
     */
    fun toDomain(): com.cloudterminal.domain.models.ScannedItem {
        return com.cloudterminal.domain.models.ScannedItem(
            barcode = barcode,
            quantity = quantity,
            productId = productId,
            scannedAt = scannedAt
        )
    }
}