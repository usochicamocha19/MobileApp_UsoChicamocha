package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migración de base de datos de versión 1 a 2.
 * Agrega la tabla de tracking de sincronización para prevenir duplicados.
 * Maneja el caso donde la tabla ya existe con estructura diferente.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Verificar si la tabla ya existe
        val tableExistsQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='sync_tracking'"
        val cursor = database.query(tableExistsQuery)
        val tableExists = cursor.count > 0
        cursor.close()
        
        if (tableExists) {
            // La tabla ya existe, verificar si tiene la estructura correcta
            val pragmaQuery = "PRAGMA table_info(sync_tracking)"
            val pragmaCursor = database.query(pragmaQuery)
            val columns = mutableMapOf<String, String>()
            
            while (pragmaCursor.moveToNext()) {
                val columnName = pragmaCursor.getString(1)
                val columnType = pragmaCursor.getString(2)
                val isNotNull = pragmaCursor.getInt(3) == 1
                val defaultValue = pragmaCursor.getString(4)
                val isPrimaryKey = pragmaCursor.getInt(5) == 1
                
                columns[columnName] = "type=$columnType, notNull=$isNotNull, default=$defaultValue, pk=$isPrimaryKey"
            }
            pragmaCursor.close()
            
            // Si la tabla existe pero no tiene todas las columnas, agregarlas
            val hasFormUuid = columns.containsKey("formUuid")
            val hasSyncAttemptId = columns.containsKey("syncAttemptId")
            val hasStartTime = columns.containsKey("startTime")
            val hasStatus = columns.containsKey("status")
            val hasWorkerId = columns.containsKey("workerId")
            val hasAttemptCount = columns.containsKey("attemptCount")
            val hasErrorMessage = columns.containsKey("errorMessage")
            
            if (!hasFormUuid) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN formUuid TEXT NOT NULL DEFAULT ''")
            }
            if (!hasSyncAttemptId) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN syncAttemptId TEXT NOT NULL DEFAULT ''")
            }
            if (!hasStartTime) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN startTime INTEGER NOT NULL DEFAULT 0")
            }
            if (!hasStatus) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN status TEXT NOT NULL DEFAULT 'STARTED'")
            }
            if (!hasWorkerId) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN workerId TEXT NOT NULL DEFAULT ''")
            }
            if (!hasAttemptCount) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN attemptCount INTEGER NOT NULL DEFAULT 1")
            }
            if (!hasErrorMessage) {
                database.execSQL("ALTER TABLE sync_tracking ADD COLUMN errorMessage TEXT")
            }
            
            // Crear índices si no existen
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_tracking_startTime` ON `sync_tracking` (`startTime`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_tracking_status` ON `sync_tracking` (`status`)")
            
        } else {
            // La tabla no existe, crearla desde cero
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `sync_tracking` (
                    `formUuid` TEXT NOT NULL,
                    `syncAttemptId` TEXT NOT NULL,
                    `startTime` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    `workerId` TEXT NOT NULL,
                    `attemptCount` INTEGER NOT NULL DEFAULT 1,
                    `errorMessage` TEXT
                )
            """.trimIndent())
            
            // Crear índices para consultas rápidas
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_tracking_startTime` ON `sync_tracking` (`startTime`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_tracking_status` ON `sync_tracking` (`status`)")
        }
    }
}