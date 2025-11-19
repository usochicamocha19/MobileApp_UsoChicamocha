package com.example.testusoandroidstudio_1_usochicamocha.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.SessionStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.ValidateSessionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.GetPendingFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.GetPendingMaintenanceFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.SyncMaintenanceFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker para sincronización de datos que procesa formularios, mantenimientos, imágenes y datos maestros.
 * Corregido para evitar duplicaciones y loops recursivos.
 */
@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val getPendingFormsUseCase: GetPendingFormsUseCase,
    private val syncFormUseCase: SyncFormUseCase,
    private val getPendingMaintenanceFormsUseCase: GetPendingMaintenanceFormsUseCase,
    private val syncMaintenanceFormsUseCase: SyncMaintenanceFormsUseCase,
    private val syncMachinesUseCase: SyncMachinesUseCase,
    private val syncOilsUseCase: SyncOilsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncDataWorker", "=== SYNC SESSION START ===")
        
        return try {
            // 1. Validar sesión
            val sessionStatus = validateSessionUseCase()
            if (sessionStatus == SessionStatus.EXPIRED) {
                Log.d("SyncDataWorker", "Sesión expirada. No se puede sincronizar.")
                return Result.failure()
            }
            
            var formsSynced = 0
            var maintenanceSynced = 0
            var totalErrors = 0

            // 2. FORMULARIOS
            val pendingForms = getPendingFormsUseCase().first()
            Log.d("SyncDataWorker", "Found ${pendingForms.size} pending forms to sync")
            
            if (pendingForms.isNotEmpty()) {
                pendingForms.forEach { form ->
                    try {
                        val result = syncFormUseCase(form)
                        if (result.isSuccess) {
                            formsSynced++
                            Log.d("SyncDataWorker", "✅ Form synced successfully: ${form.UUID}")
                        } else {
                            totalErrors++
                            Log.e("SyncDataWorker", "❌ Form sync failed: ${form.UUID} - ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        totalErrors++
                        Log.e("SyncDataWorker", "❌ Exception syncing form ${form.UUID}", e)
                    }
                }
            }

            // 3. MANTENIMIENTOS
            val pendingMaintenance = getPendingMaintenanceFormsUseCase().first()
            Log.d("SyncDataWorker", "Found ${pendingMaintenance.size} pending maintenance forms to sync")
            
            if (pendingMaintenance.isNotEmpty()) {
                pendingMaintenance.forEach { maintenance ->
                    try {
                        val result = syncMaintenanceFormsUseCase(maintenance)
                        if (result.isSuccess) {
                            maintenanceSynced++
                            Log.d("SyncDataWorker", "✅ Maintenance synced successfully: ${maintenance.id}")
                        } else {
                            totalErrors++
                            Log.e("SyncDataWorker", "❌ Maintenance sync failed: ${maintenance.id} - ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        totalErrors++
                        Log.e("SyncDataWorker", "❌ Exception syncing maintenance ${maintenance.id}", e)
                    }
                }
            }

            // 4. IMÁGENES (encolar una sola vez)
            try {
                val imageWork = OneTimeWorkRequestBuilder<ImageSyncWorker>().build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "chained_image_sync",
                    ExistingWorkPolicy.KEEP,
                    imageWork
                )
                Log.d("SyncDataWorker", "Image sync worker enqueued (KEEP policy)")
            } catch (e: Exception) {
                Log.e("SyncDataWorker", "Failed to enqueue image sync worker", e)
            }

            // 5. DATOS MAESTROS (solo si no había otros datos pendientes)
            if (pendingForms.isEmpty() && pendingMaintenance.isEmpty()) {
                Log.d("SyncDataWorker", "No forms or maintenance pending. Syncing master data...")
                try {
                    syncMachinesUseCase()
                    syncOilsUseCase()
                    Log.d("SyncDataWorker", "✅ Master data synced successfully")
                } catch (e: Exception) {
                    Log.e("SyncDataWorker", "❌ Error syncing master data", e)
                    totalErrors++
                }
            }

            // Log summary
            Log.d("SyncDataWorker", "=== SYNC SESSION COMPLETE ===")
            Log.d("SyncDataWorker", "Forms synced: $formsSynced, Maintenance synced: $maintenanceSynced, Total errors: $totalErrors")
            
            // Return success if we processed anything, retry only on critical errors
            return if (pendingForms.isNotEmpty() || pendingMaintenance.isNotEmpty()) {
                if (totalErrors == 0) {
                    Log.d("SyncDataWorker", "All sync operations completed successfully")
                    Result.success()
                } else {
                    Log.w("SyncDataWorker", "Some sync operations failed, but session completed")
                    Result.success() // Don't retry, let next periodic sync handle failures
                }
            } else {
                Log.d("SyncDataWorker", "No data to sync, session completed")
                Result.success()
            }

        } catch (e: Exception) {
            Log.e("SyncDataWorker", "Critical error during sync session: ${e.message}", e)
            return Result.retry() // Only retry on critical failures
        }
    }
}
