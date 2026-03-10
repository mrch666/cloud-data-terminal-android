package com.cloudterminal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cloudterminal.data.local.entity.ScannedItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с отсканированными элементами
 */
@Dao
interface ScannedItemDao {
    
    /**
     * Вставка отсканированного элемента
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedItem(item: ScannedItemEntity)
    
    /**
     * Вставка нескольких отсканированных элементов
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedItems(items: List<ScannedItemEntity>)
    
    /**
     * Обновление отсканированного элемента
     */
    @Update
    suspend fun updateScannedItem(item: ScannedItemEntity)
    
    /**
     * Получение отсканированного элемента по ID
     */
    @Query("SELECT * FROM scanned_items WHERE id = :id")
    suspend fun getScannedItemById(id: String): ScannedItemEntity?
    
    /**
     * Получение всех отсканированных элементов
     */
    @Query("SELECT * FROM scanned_items ORDER BY scanned_at DESC")
    fun getAllScannedItems(): Flow<List<ScannedItemEntity>>
    
    /**
     * Получение отсканированных элементов по сессии
     */
    @Query("SELECT * FROM scanned_items WHERE session_id = :sessionId ORDER BY scanned_at DESC")
    fun getScannedItemsBySession(sessionId: String): Flow<List<ScannedItemEntity>>
    
    /**
     * Получение отсканированных элементов по штрих-коду
     */
    @Query("SELECT * FROM scanned_items WHERE barcode = :barcode ORDER BY scanned_at DESC")
    fun getScannedItemsByBarcode(barcode: String): Flow<List<ScannedItemEntity>>
    
    /**
     * Получение несинхронизированных элементов
     */
    @Query("SELECT * FROM scanned_items WHERE is_synced = 0 ORDER BY scanned_at ASC")
    fun getUnsyncedScannedItems(): Flow<List<ScannedItemEntity>>
    
    /**
     * Получение количества отсканированных элементов
     */
    @Query("SELECT COUNT(*) FROM scanned_items")
    suspend fun getScannedItemsCount(): Int
    
    /**
     * Получение количества отсканированных элементов по сессии
     */
    @Query("SELECT COUNT(*) FROM scanned_items WHERE session_id = :sessionId")
    suspend fun getScannedItemsCountBySession(sessionId: String): Int
    
    /**
     * Получение количества несинхронизированных элементов
     */
    @Query("SELECT COUNT(*) FROM scanned_items WHERE is_synced = 0")
    suspend fun getUnsyncedScannedItemsCount(): Int
    
    /**
     * Удаление отсканированного элемента по ID
     */
    @Query("DELETE FROM scanned_items WHERE id = :id")
    suspend fun deleteScannedItemById(id: String)
    
    /**
     * Удаление отсканированных элементов по сессии
     */
    @Query("DELETE FROM scanned_items WHERE session_id = :sessionId")
    suspend fun deleteScannedItemsBySession(sessionId: String)
    
    /**
     * Удаление всех отсканированных элементов
     */
    @Query("DELETE FROM scanned_items")
    suspend fun deleteAllScannedItems()
    
    /**
     * Удаление старых отсканированных элементов (старше указанного времени)
     */
    @Query("DELETE FROM scanned_items WHERE scanned_at < :timestamp")
    suspend fun deleteOldScannedItems(timestamp: Long)
    
    /**
     * Обновление статуса синхронизации
     */
    @Query("UPDATE scanned_items SET is_synced = :isSynced, sync_error = :error WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean, error: String? = null)
    
    /**
     * Обновление статуса синхронизации для нескольких элементов
     */
    @Query("UPDATE scanned_items SET is_synced = :isSynced, sync_error = :error WHERE id IN (:ids)")
    suspend fun updateSyncStatusBatch(ids: List<String>, isSynced: Boolean, error: String? = null)
    
    /**
     * Получение статистики по отсканированным элементам
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN is_synced = 1 THEN 1 ELSE 0 END) as synced,
            SUM(CASE WHEN is_synced = 0 THEN 1 ELSE 0 END) as unsynced,
            MIN(scanned_at) as first_scan,
            MAX(scanned_at) as last_scan
        FROM scanned_items
    """)
    suspend fun getScannedItemsStats(): ScannedItemsStats?
    
    /**
     * Поиск отсканированных элементов
     */
    @Query("""
        SELECT * FROM scanned_items 
        WHERE barcode LIKE '%' || :query || '%' 
        OR product_id LIKE '%' || :query || '%'
        ORDER BY scanned_at DESC
    """)
    fun searchScannedItems(query: String): Flow<List<ScannedItemEntity>>
}

/**
 * Статистика по отсканированным элементам
 */
data class ScannedItemsStats(
    val total: Int,
    val synced: Int,
    val unsynced: Int,
    val firstScan: Long?,
    val lastScan: Long?
)