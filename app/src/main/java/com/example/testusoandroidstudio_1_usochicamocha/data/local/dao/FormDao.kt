package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.PendingFormWithImageCount
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: FormEntity)

    @Query("UPDATE pending_forms SET isSynced = 1, serverId = :serverId WHERE uuid = :uuid")
    suspend fun markAsSynced(uuid: String, serverId: Long)

    /**
     * AÑADIDO: Se restaura esta función para que el FormRepositoryImpl pueda
     * cumplir con el método getPendingForms() de su interfaz. Esta consulta es
     * usada por el SyncDataWorker para obtener los formularios de texto a sincronizar.
     */
    @Query("SELECT * FROM pending_forms WHERE isSynced = 0 ORDER BY timestamp DESC")
    fun getPendingFormsFlow(): Flow<List<FormEntity>>

    @Query("""
        SELECT 
            pf.*, 
            COUNT(pi.localId) as totalImageCount, 
            SUM(CASE WHEN pi.isSynced = 1 THEN 1 ELSE 0 END) as syncedImageCount
        FROM pending_forms pf
        LEFT JOIN pending_images pi ON pf.UUID = pi.formUUID
        GROUP BY pf.localId
        HAVING pf.isSynced = 0 OR (COUNT(pi.localId) > SUM(CASE WHEN pi.isSynced = 1 THEN 1 ELSE 0 END))
        ORDER BY pf.timestamp DESC
    """)
    fun getPendingFormsWithImageCount(): Flow<List<PendingFormWithImageCount>>
}

