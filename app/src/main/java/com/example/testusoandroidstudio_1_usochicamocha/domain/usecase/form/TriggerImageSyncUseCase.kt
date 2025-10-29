package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.ImageSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Este caso de uso se encarga de disparar una ejecución inmediata
 * del ImageSyncWorker bajo demanda.
 * MODIFICADO: Verifica si ya hay trabajos en ejecución antes de crear nuevos.
 */
class TriggerImageSyncUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() {
        val workManager = WorkManager.getInstance(context)

        // Verificar si ya hay trabajos de sincronización de imágenes en ejecución
        val existingWork = workManager.getWorkInfosForUniqueWork("manual_image_sync").get()
        val hasRunningWork = existingWork.any { workInfo ->
            workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED
        }

        if (hasRunningWork) {
            Log.d("TriggerImageSyncUC", "Ya hay un trabajo de sincronización de imágenes en ejecución. Saltando.")
            return
        }

        // Verificar también el trabajo encadenado del SyncDataWorker
        val chainedWork = workManager.getWorkInfosForUniqueWork("chained_image_sync").get()
        val hasChainedRunningWork = chainedWork.any { workInfo ->
            workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED
        }

        if (hasChainedRunningWork) {
            Log.d("TriggerImageSyncUC", "Ya hay un trabajo encadenado de sincronización de imágenes en ejecución. Saltando.")
            return
        }

        // Crea una petición de trabajo para que se ejecute UNA SOLA VEZ.
        val imageSyncRequest = OneTimeWorkRequestBuilder<ImageSyncWorker>().build()

        // Le dice a WorkManager que encole este trabajo.
        // ExistingWorkPolicy.KEEP asegura que si ya hay un trabajo pendiente, no se reemplace
        workManager.enqueueUniqueWork(
            "manual_image_sync", // Un nombre único para este trabajo manual
            ExistingWorkPolicy.KEEP, // Mantiene trabajo existente si está corriendo
            imageSyncRequest
        )

        Log.d("TriggerImageSyncUC", "Trabajo de sincronización de imágenes manual encolado.")
    }
}
