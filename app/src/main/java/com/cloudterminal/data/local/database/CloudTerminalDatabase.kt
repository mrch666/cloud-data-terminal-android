package com.cloudterminal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cloudterminal.data.local.dao.ProductDao
import com.cloudterminal.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CloudTerminalDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    
    companion object {
        const val DATABASE_NAME = "cloud_terminal.db"
    }
}