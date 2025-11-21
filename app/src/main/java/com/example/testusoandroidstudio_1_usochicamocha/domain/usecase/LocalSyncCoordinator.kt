package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.CleanupWorker
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinador de sincronización centralizado.
 * Evita duplicados y coordina todos los workers de sincronización.
 */
@Singleton
class LocalSyncCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    
    companion object {
        private const val TAG = "LocalSyncCoordinator"
        private const val COORDINATED_SYNC_WORK = "coordinated_sync_work"
        private const val CLEANUP_WORK = "periodic_cleanup_work"
        private const val SYNC_TIMEOUT_MS = 60000L // 1 minuto timeout
    }
    
    private val activeSyncOperations = ConcurrentHashMap<String, Long>()
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus
    
    sealed class SyncTrigger {
        data class FormSaved(val formType: String) : SyncTrigger() {
            override fun getWorkName(): String = "form_save_sync"
        }
        data class MaintenanceSaved(val maintenanceType: String) : SyncTrigger() {
            override fun getWorkName(): String = "maintenance_save_sync"
        }
        data class ManualSync(val syncType: SyncType) : SyncTrigger() {
            override fun getWorkName(): String = "manual_${syncType}_sync"
        }
        data class PeriodicSync(val interval: Long) : SyncTrigger() {
            override fun getWorkName(): String = "periodic_sync"
        }
        data class AppStartSync(val reason: String) : SyncTrigger() {
            override fun getWorkName(): String = "app_start_sync"
        }
        
        abstract fun getWorkName(): String
    }
    
    enum class SyncType {
        ALL_DATA,
        FORMS_ONLY,
        MAINTENANCE_ONLY,
        IMAGES_ONLY,
        MASTER_DATA
    }
    
    enum class SyncStatus {
        IDLE,
        COORDINATING,
        SYNCING,
        CLEANING,
        ERROR
    }
    
    /**
     * Punto de entrada principal para sincronización coordinada
     */
    suspend fun coordinateSync(trigger: SyncTrigger): Result<Unit> {
        val workName = trigger.getWorkName()
        val operationId = "coord_${workName}_${System.currentTimeMillis()}"
        
        return try {
            Log.d(TAG, "🎯 Coordinating sync triggered: $trigger")
            
            // 1. Verificar si hay un work con el mismo nombre ya en progreso
            val isWorkRunning = runBlocking {
                val existingWork = workManager.getWorkInfosForUniqueWork(workName).await()
                existingWork.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
            
            if (isWorkRunning) {
                Log.d(TAG, "⏭️  Work $workName already running, skipping")
                return Result.success(Unit)
            }
            
            // 2. Marcar operación como activa en memoria
            activeSyncOperations[workName] = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.COORDINATING
            
            // 3. Crear constraints para el work
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            // 4. Crear el work request apropiado
            val workRequest = createWorkRequestForTrigger(trigger, constraints)
            
            // 5. Encolar con nombre único
            workManager.enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE, // Reemplazar si existe uno anterior completado
                workRequest
            )
            
            Log.d(TAG, "✅ Coordinated sync enqueued: $workName")
            _syncStatus.value = SyncStatus.SYNCING
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error coordinating sync", e)
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        } finally {
            // Limpiar de memoria después de un tiempo
            activeSyncOperations.remove(trigger.getWorkName())
        }
    }
    
    /**
     * Programa limpieza periódica de la base de datos
     */
    fun schedulePeriodicCleanup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val periodicWork = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            CLEANUP_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
        
        Log.d(TAG, "🧹 Periodic cleanup scheduled")
    }
    
    /**
     * Ejecuta limpieza inmediata
     */
    suspend fun executeImmediateCleanup(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.CLEANING
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
                .setConstraints(constraints)
                .build()
            
            workManager.enqueueUniqueWork(
                "immediate_cleanup",
                ExistingWorkPolicy.KEEP,
                cleanupRequest
            )
            
            Log.d(TAG, "🧹 Immediate cleanup enqueued")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error scheduling cleanup", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el estado actual de la sincronización
     */
    fun getCurrentSyncStatus(): SyncStatus {
        return _syncStatus.value
    }
    
    /**
     * Cancela todas las operaciones de sincronización activas
     */
    fun cancelAllSyncOperations() {
        workManager.cancelAllWorkByTag(COORDINATED_SYNC_WORK)
        activeSyncOperations.clear()
        _syncStatus.value = SyncStatus.IDLE
        Log.d(TAG, "🚫 All sync operations cancelled")
    }
    
    private fun createWorkRequestForTrigger(trigger: SyncTrigger, constraints: Constraints): OneTimeWorkRequest {
        return when (trigger) {
            is SyncTrigger.FormSaved,
            is SyncTrigger.MaintenanceSaved,
            is SyncTrigger.ManualSync,
            is SyncTrigger.AppStartSync,
            is SyncTrigger.PeriodicSync -> {
                OneTimeWorkRequestBuilder<SyncDataWorker>()
                    .setConstraints(constraints)
                    .addTag(COORDINATED_SYNC_WORK)
                    .build()
            }
        }
    }
}