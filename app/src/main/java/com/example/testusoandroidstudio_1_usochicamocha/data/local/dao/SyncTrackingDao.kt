package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.SyncTrackingEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para tracking de sincronización de formularios.
 * Ayuda a prevenir duplicados mediante registro de intentos de sync.
 */
@Dao
interface SyncTrackingDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracking(tracking: SyncTrackingEntity)
    
    @Delete
    suspend fun deleteTracking(tracking: SyncTrackingEntity)
    
    /**
     * Obtiene el tracking de un formulario específico
     */
    @Query("SELECT * FROM sync_tracking WHERE formUuid = :uuid")
    suspend fun getTracking(uuid: String): SyncTrackingEntity?
    
    /**
     * Obtiene todos los tracking activos (STARTED)
     */
    @Query("SELECT * FROM sync_tracking WHERE status = 'STARTED'")
    fun getActiveTracking(): Flow<List<SyncTrackingEntity>>
    
    /**
     * Limpia entries antiguos para evitar crecimiento ilimitado
     */
    @Query("DELETE FROM sync_tracking WHERE startTime < :cutoffTime")
    suspend fun deleteOldEntries(cutoffTime: Long)
    
    /**
     * Cuenta cuántos intentos de sync hay para un formulario
     */
    @Query("SELECT COUNT(*) FROM sync_tracking WHERE formUuid = :uuid")
    suspend fun getSyncAttemptsCount(uuid: String): Int
    
    /**
     * Obtiene el tracking más reciente de un formulario
     */
    @Query("SELECT * FROM sync_tracking WHERE formUuid = :uuid ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestTracking(uuid: String): SyncTrackingEntity?
    
    /**
     * Limpia todos los tracking (para testing)
     */
    @Query("DELETE FROM sync_tracking")
    suspend fun clearAllTracking()
}