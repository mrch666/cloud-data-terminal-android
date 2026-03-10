package com.cloudterminal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cloudterminal.data.local.dao.ProductDao
import com.cloudterminal.data.local.dao.ScanSessionDao
import com.cloudterminal.data.local.dao.ScannedItemDao
import com.cloudterminal.data.local.entity.ProductEntity
import com.cloudterminal.data.local.entity.ScanSessionEntity
import com.cloudterminal.data.local.entity.ScannedItemEntity
import com.cloudterminal.data.local.converters.SyncStatusConverter

/**
 * Обновленная база данных приложения с поддержкой миграций
 */
@Database(
    entities = [
        ProductEntity::class,
        ScannedItemEntity::class,
        ScanSessionEntity::class
    ],
    version = 2, // Увеличиваем версию для добавления новых таблиц
    exportSchema = true // Включаем экспорт схемы для миграций
)
@TypeConverters(SyncStatusConverter::class)
abstract class CloudTerminalDatabaseUpdated : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun scannedItemDao(): ScannedItemDao
    abstract fun scanSessionDao(): ScanSessionDao
    
    companion object {
        const val DATABASE_NAME = "cloud_terminal.db"
        
        // Версии базы данных:
        // Version 1: Только таблица products (начальная версия)
        // Version 2: Добавлены scanned_items и scan_sessions
        
        /**
         * Получение миграций для обновления базы данных
         */
        fun getMigrations() = arrayOf(
            // Миграция с версии 1 на 2
            androidx.room.migration.Migration(1, 2) { database ->
                // Создание таблицы scanned_items
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS scanned_items (
                        id TEXT PRIMARY KEY NOT NULL,
                        barcode TEXT NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1,
                        product_id TEXT,
                        session_id TEXT,
                        scanned_at INTEGER NOT NULL,
                        is_synced INTEGER NOT NULL DEFAULT 0,
                        sync_error TEXT
                    )
                """)
                
                // Создание индексов для scanned_items
                database.execSQL("CREATE INDEX idx_scanned_items_barcode ON scanned_items(barcode)")
                database.execSQL("CREATE INDEX idx_scanned_items_session_id ON scanned_items(session_id)")
                database.execSQL("CREATE INDEX idx_scanned_items_scanned_at ON scanned_items(scanned_at)")
                database.execSQL("CREATE INDEX idx_scanned_items_is_synced ON scanned_items(is_synced)")
                
                // Создание таблицы scan_sessions
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS scan_sessions (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        item_count INTEGER NOT NULL DEFAULT 0,
                        is_completed INTEGER NOT NULL DEFAULT 0,
                        synced_at INTEGER,
                        sync_status TEXT NOT NULL DEFAULT 'PENDING',
                        description TEXT,
                        location TEXT,
                        tags TEXT
                    )
                """)
                
                // Создание индексов для scan_sessions
                database.execSQL("CREATE INDEX idx_scan_sessions_created_at ON scan_sessions(created_at)")
                database.execSQL("CREATE INDEX idx_scan_sessions_is_completed ON scan_sessions(is_completed)")
                database.execSQL("CREATE INDEX idx_scan_sessions_sync_status ON scan_sessions(sync_status)")
                
                // Добавление foreign key constraint (опционально, если нужно)
                // database.execSQL("PRAGMA foreign_keys = ON")
                // database.execSQL("""
                //     ALTER TABLE scanned_items 
                //     ADD CONSTRAINT fk_scanned_items_session 
                //     FOREIGN KEY (session_id) 
                //     REFERENCES scan_sessions(id) 
                //     ON DELETE SET NULL
                // """)
            }
        )
        
        /**
         * Проверка целостности базы данных
         */
        fun checkDatabaseIntegrity(database: androidx.sqlite.db.SupportSQLiteDatabase): Boolean {
            val cursor = database.query("PRAGMA integrity_check")
            return try {
                cursor.moveToFirst()
                val result = cursor.getString(0)
                result == "ok"
            } finally {
                cursor.close()
            }
        }
        
        /**
         * Оптимизация базы данных
         */
        fun optimizeDatabase(database: androidx.sqlite.db.SupportSQLiteDatabase) {
            database.execSQL("VACUUM")
            database.execSQL("ANALYZE")
        }
        
        /**
         * Получение информации о базе данных
         */
        fun getDatabaseInfo(database: androidx.sqlite.db.SupportSQLiteDatabase): DatabaseInfo {
            val tablesCursor = database.query("SELECT name FROM sqlite_master WHERE type='table'")
            val tables = mutableListOf<String>()
            
            try {
                while (tablesCursor.moveToNext()) {
                    tables.add(tablesCursor.getString(0))
                }
            } finally {
                tablesCursor.close()
            }
            
            val versionCursor = database.query("PRAGMA user_version")
            val version = try {
                versionCursor.moveToFirst()
                versionCursor.getInt(0)
            } finally {
                versionCursor.close()
            }
            
            return DatabaseInfo(
                version = version,
                tables = tables,
                size = File("$DATABASE_NAME").length() // Это нужно будет адаптировать для Android
            )
        }
    }
}

/**
 * Информация о базе данных
 */
data class DatabaseInfo(
    val version: Int,
    val tables: List<String>,
    val size: Long
)