package com.cloudterminal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cloudterminal.domain.models.SyncStatus
import java.util.UUID

/**
 * Entity для хранения сессий сканирования
 */
@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0,
    val isCompleted: Boolean = false,
    val syncedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val description: String? = null,
    val location: String? = null,
    val tags: String? = null // JSON строка с тегами
) {
    companion object {
        /**
         * Создание entity из доменной модели
         */
        fun fromDomain(session: com.cloudterminal.domain.models.SyncSession): ScanSessionEntity {
            return ScanSessionEntity(
                id = session.id,
                name = session.name,
                createdAt = session.createdAt,
                itemCount = session.itemCount,
                isCompleted = session.isCompleted,
                syncedAt = session.syncedAt,
                syncStatus = session.syncStatus,
                description = null,
                location = null,
                tags = null
            )
        }
    }
    
    /**
     * Преобразование в доменную модель
     */
    fun toDomain(): com.cloudterminal.domain.models.SyncSession {
        return com.cloudterminal.domain.models.SyncSession(
            id = id,
            name = name,
            createdAt = createdAt,
            itemCount = itemCount,
            isCompleted = isCompleted,
            syncedAt = syncedAt,
            syncStatus = syncStatus
        )
    }
}