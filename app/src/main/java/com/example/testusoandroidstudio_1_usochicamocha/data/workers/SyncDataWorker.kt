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
        try{
        Log.d("SyncDataWorker", "Iniciando trabajo de sincronización...")

        val sessionStatus = validateSessionUseCase()
        if (sessionStatus == SessionStatus.EXPIRED) {
            Log.d("SyncDataWorker", "Sesión expirada. No se puede sincronizar.")
            return Result.failure()
        }

        try {
            var didSyncSomething = false

            // 1. FORMULARIOS
            val pendingForms = getPendingFormsUseCase().first()
            if (pendingForms.isNotEmpty()) {
                Log.d("SyncDataWorker", "Sincronizando ${pendingForms.size} inspecciones pendientes...")
                pendingForms.forEach { form -> syncFormUseCase(form) }
                didSyncSomething = true
            }

            // 2. MANTENIMIENTOS
            val pendingMaintenance = getPendingMaintenanceFormsUseCase().first()
            if (pendingMaintenance.isNotEmpty()) {
                Log.d("SyncDataWorker", "Sincronizando ${pendingMaintenance.size} mantenimientos pendientes...")
                pendingMaintenance.forEach { maintenance -> syncMaintenanceFormsUseCase(maintenance) }
                didSyncSomething = true
            }

            // 3. IMÁGENES (solo después de formularios y mantenimientos)
            val imageWork = OneTimeWorkRequestBuilder<ImageSyncWorker>().build()
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "chained_image_sync",
                ExistingWorkPolicy.REPLACE,
                imageWork
            )
            Log.d("SyncDataWorker", "Trabajo de sincronización de imágenes encolado.")
            didSyncSomething = true

            // 4. DATOS MAESTROS (solo si no había formularios ni mantenimientos ni imágenes pendientes)
            if (!didSyncSomething) {
                Log.d("SyncDataWorker", "No había formularios, mantenimientos ni imágenes. Sincronizando datos maestros...")
                syncMachinesUseCase()
                syncOilsUseCase()
                Log.d("SyncDataWorker", "Datos maestros sincronizados.")
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("SyncDataWorker", "Error durante la sincronización: ${e.message}", e)
            return Result.retry()
        }
        enqueueAgain(applicationContext)
        return Result.success()
    } catch (e: Exception) {
        enqueueAgain(applicationContext)
        return Result.retry()
    }
    }
    private fun enqueueAgain(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "always_sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
