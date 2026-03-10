package com.cloudterminal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cloudterminal.data.local.entity.ScanSessionEntity
import com.cloudterminal.domain.models.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с сессиями сканирования
 */
@Dao
interface ScanSessionDao {
    
    /**
     * Вставка сессии сканирования
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanSession(session: ScanSessionEntity)
    
    /**
     * Вставка нескольких сессий сканирования
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanSessions(sessions: List<ScanSessionEntity>)
    
    /**
     * Обновление сессии сканирования
     */
    @Update
    suspend fun updateScanSession(session: ScanSessionEntity)
    
    /**
     * Получение сессии сканирования по ID
     */
    @Query("SELECT * FROM scan_sessions WHERE id = :id")
    suspend fun getScanSessionById(id: String): ScanSessionEntity?
    
    /**
     * Получение всех сессий сканирования
     */
    @Query("SELECT * FROM scan_sessions ORDER BY created_at DESC")
    fun getAllScanSessions(): Flow<List<ScanSessionEntity>>
    
    /**
     * Получение активных сессий (не завершенных)
     */
    @Query("SELECT * FROM scan_sessions WHERE is_completed = 0 ORDER BY created_at DESC")
    fun getActiveScanSessions(): Flow<List<ScanSessionEntity>>
    
    /**
     * Получение завершенных сессий
     */
    @Query("SELECT * FROM scan_sessions WHERE is_completed = 1 ORDER BY created_at DESC")
    fun getCompletedScanSessions(): Flow<List<ScanSessionEntity>>
    
    /**
     * Получение сессий по статусу синхронизации
     */
    @Query("SELECT * FROM scan_sessions WHERE sync_status = :status ORDER BY created_at DESC")
    fun getScanSessionsBySyncStatus(status: SyncStatus): Flow<List<ScanSessionEntity>>
    
    /**
     * Получение количества сессий
     */
    @Query("SELECT COUNT(*) FROM scan_sessions")
    suspend fun getScanSessionsCount(): Int
    
    /**
     * Получение количества активных сессий
     */
    @Query("SELECT COUNT(*) FROM scan_sessions WHERE is_completed = 0")
    suspend fun getActiveScanSessionsCount(): Int
    
    /**
     * Получение количества завершенных сессий
     */
    @Query("SELECT COUNT(*) FROM scan_sessions WHERE is_completed = 1")
    suspend fun getCompletedScanSessionsCount(): Int
    
    /**
     * Удаление сессии сканирования по ID
     */
    @Query("DELETE FROM scan_sessions WHERE id = :id")
    suspend fun deleteScanSessionById(id: String)
    
    /**
     * Удаление всех сессий сканирования
     */
    @Query("DELETE FROM scan_sessions")
    suspend fun deleteAllScanSessions()
    
    /**
     * Удаление старых сессий (старше указанного времени)
     */
    @Query("DELETE FROM scan_sessions WHERE created_at < :timestamp")
    suspend fun deleteOldScanSessions(timestamp: Long)
    
    /**
     * Обновление статуса завершения сессии
     */
    @Query("UPDATE scan_sessions SET is_completed = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: String, isCompleted: Boolean)
    
    /**
     * Обновление статуса синхронизации сессии
     */
    @Query("UPDATE scan_sessions SET sync_status = :status, synced_at = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus, syncedAt: Long? = null)
    
    /**
     * Обновление количества элементов в сессии
     */
    @Query("UPDATE scan_sessions SET item_count = :itemCount WHERE id = :id")
    suspend fun updateItemCount(id: String, itemCount: Int)
    
    /**
     * Увеличение количества элементов в сессии
     */
    @Query("UPDATE scan_sessions SET item_count = item_count + 1 WHERE id = :id")
    suspend fun incrementItemCount(id: String)
    
    /**
     * Уменьшение количества элементов в сессии
     */
    @Query("UPDATE scan_sessions SET item_count = item_count - 1 WHERE id = :id")
    suspend fun decrementItemCount(id: String)
    
    /**
     * Получение статистики по сессиям
     */
    @Query("""
        SELECT 
            COUNT(*) as total_sessions,
            SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) as completed_sessions,
            SUM(item_count) as total_items,
            AVG(item_count) as avg_items_per_session,
            MIN(created_at) as first_session,
            MAX(created_at) as last_session
        FROM scan_sessions
    """)
    suspend fun getScanSessionsStats(): ScanSessionsStats?
    
    /**
     * Поиск сессий по названию
     */
    @Query("SELECT * FROM scan_sessions WHERE name LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchScanSessions(query: String): Flow<List<ScanSessionEntity>>
    
    /**
     * Получение последней активной сессии
     */
    @Query("SELECT * FROM scan_sessions WHERE is_completed = 0 ORDER BY created_at DESC LIMIT 1")
    suspend fun getLastActiveScanSession(): ScanSessionEntity?
    
    /**
     * Получение сессий за период
     */
    @Query("SELECT * FROM scan_sessions WHERE created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    fun getScanSessionsByPeriod(startTime: Long, endTime: Long): Flow<List<ScanSessionEntity>>
}

/**
 * Статистика по сессиям сканирования
 */
data class ScanSessionsStats(
    val totalSessions: Int,
    val completedSessions: Int,
    val totalItems: Int,
    val avgItemsPerSession: Double?,
    val firstSession: Long?,
    val lastSession: Long?
)