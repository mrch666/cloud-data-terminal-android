package com.cloudterminal.domain.repository

import com.cloudterminal.domain.models.ScannedItem

interface ScannedItemRepository {
    suspend fun saveScannedItem(item: ScannedItem)
    suspend fun getScannedItems(sessionId: String? = null): List<ScannedItem>
    suspend fun deleteScannedItems(sessionId: String? = null)
    suspend fun getScannedItemsCount(): Int
}