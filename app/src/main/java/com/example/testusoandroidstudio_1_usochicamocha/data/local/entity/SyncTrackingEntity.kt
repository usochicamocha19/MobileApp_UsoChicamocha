package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para tracking de sincronización de formularios.
 * Previene duplicados mediante registro de intentos de sync.
 * Usa syncAttemptId como primary key para coincidir con la BD existente.
 */
@Entity(
    tableName = "sync_tracking",
    indices = [
        Index(value = ["status"]),
        Index(value = ["startTime"])
    ]
)
data class SyncTrackingEntity(
    val formUuid: String,
    
    @PrimaryKey
    val syncAttemptId: String,
    
    val startTime: Long,
    
    val status: String, // Changed from SyncStatus enum to String for compatibility
    
    val workerId: String,
    
    val attemptCount: Int = 1,
    
    val errorMessage: String? = null
)

enum class SyncStatus {
    STARTED,
    SUCCESS,
    FAILED,
    SKIPPED
}