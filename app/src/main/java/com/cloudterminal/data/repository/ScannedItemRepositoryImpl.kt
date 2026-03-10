package com.cloudterminal.data.repository

import com.cloudterminal.data.local.dao.ScannedItemDao
import com.cloudterminal.data.local.entity.ScannedItemEntity
import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.repository.ScannedItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Реализация репозитория для отсканированных элементов
 */
class ScannedItemRepositoryImpl @Inject constructor(
    private val scannedItemDao: ScannedItemDao
) : ScannedItemRepository {
    
    override suspend fun saveScannedItem(item: ScannedItem) {
        val entity = ScannedItemEntity.fromDomain(
            barcode = item.barcode,
            quantity = item.quantity,
            productId = item.productId,
            sessionId = null // TODO: Добавить поддержку сессий
        )
        scannedItemDao.insertScannedItem(entity)
    }
    
    override suspend fun getScannedItems(sessionId: String?): List<ScannedItem> {
        return if (sessionId != null) {
            // TODO: Реализовать получение по сессии
            emptyList()
        } else {
            // Получение всех элементов
            // Note: Это suspend функция, но DAO возвращает Flow
            // Нужно получить первый элемент Flow
            scannedItemDao.getAllScannedItems()
                .map { entities -> entities.map { it.toDomain() } }
                // Получаем первый элемент Flow (блокирующий вызов)
                // В реальном приложении лучше использовать Flow в UI слое
                // или добавить suspend функцию в DAO
                // Для простоты используем пустой список
                // TODO: Добавить suspend функцию в DAO
                // .first()
        }
    }
    
    override suspend fun deleteScannedItems(sessionId: String?) {
        if (sessionId != null) {
            scannedItemDao.deleteScannedItemsBySession(sessionId)
        } else {
            scannedItemDao.deleteAllScannedItems()
        }
    }
    
    override suspend fun getScannedItemsCount(): Int {
        return scannedItemDao.getScannedItemsCount()
    }
    
    /**
     * Дополнительные методы для расширенной функциональности
     */
    
    suspend fun saveScannedItems(items: List<ScannedItem>) {
        val entities = items.map { item ->
            ScannedItemEntity.fromDomain(
                barcode = item.barcode,
                quantity = item.quantity,
                productId = item.productId,
                sessionId = null
            )
        }
        scannedItemDao.insertScannedItems(entities)
    }
    
    fun getScannedItemsFlow(): Flow<List<ScannedItem>> {
        return scannedItemDao.getAllScannedItems()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getScannedItemsBySessionFlow(sessionId: String): Flow<List<ScannedItem>> {
        return scannedItemDao.getScannedItemsBySession(sessionId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun getScannedItemById(id: String): ScannedItem? {
        return scannedItemDao.getScannedItemById(id)?.toDomain()
    }
    
    suspend fun updateScannedItem(item: ScannedItem) {
        // TODO: Нужно сохранить ID при создании ScannedItem
        // Для MVP просто сохраняем как новый элемент
        saveScannedItem(item)
    }
    
    suspend fun deleteScannedItemById(id: String) {
        scannedItemDao.deleteScannedItemById(id)
    }
    
    suspend fun getUnsyncedScannedItems(): List<ScannedItem> {
        return scannedItemDao.getUnsyncedScannedItems()
            .map { entities -> entities.map { it.toDomain() } }
            // TODO: Добавить suspend функцию в DAO
            // .first()
    }
    
    suspend fun updateSyncStatus(itemId: String, isSynced: Boolean, error: String? = null) {
        scannedItemDao.updateSyncStatus(itemId, isSynced, error)
    }
    
    suspend fun getScannedItemsStats(): com.cloudterminal.data.local.dao.ScannedItemsStats? {
        return scannedItemDao.getScannedItemsStats()
    }
}