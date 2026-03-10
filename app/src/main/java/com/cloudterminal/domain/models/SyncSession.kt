package com.cloudterminal.domain.models

data class SyncSession(
    val id: String,
    val name: String,
    val createdAt: Long,
    val itemCount: Int = 0,
    val isCompleted: Boolean = false,
    val syncedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)