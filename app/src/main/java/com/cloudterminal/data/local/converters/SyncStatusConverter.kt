package com.cloudterminal.data.local.converters

import androidx.room.TypeConverter
import com.cloudterminal.domain.models.SyncStatus

/**
 * Конвертер для преобразования SyncStatus в строку и обратно
 */
class SyncStatusConverter {
    
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return when (status) {
            SyncStatus.PENDING -> "PENDING"
            SyncStatus.SYNCING -> "SYNCING"
            SyncStatus.COMPLETED -> "COMPLETED"
            SyncStatus.FAILED -> "FAILED"
        }
    }
    
    @TypeConverter
    fun toSyncStatus(statusString: String): SyncStatus {
        return when (statusString) {
            "PENDING" -> SyncStatus.PENDING
            "SYNCING" -> SyncStatus.SYNCING
            "COMPLETED" -> SyncStatus.COMPLETED
            "FAILED" -> SyncStatus.FAILED
            else -> SyncStatus.PENDING
        }
    }
}