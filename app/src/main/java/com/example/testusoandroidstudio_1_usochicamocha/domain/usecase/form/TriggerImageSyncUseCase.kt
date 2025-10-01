package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.ImageSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Este caso de uso se encarga de disparar una ejecución inmediata
 * del ImageSyncWorker bajo demanda.
 */
class TriggerImageSyncUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        // Crea una petición de trabajo para que se ejecute UNA SOLA VEZ.
        val imageSyncRequest = OneTimeWorkRequestBuilder<ImageSyncWorker>().build()

        // Le dice a WorkManager que encole este trabajo.
        // ExistingWorkPolicy.REPLACE asegura que si ya hay un trabajo de imágenes
        // pendiente, se reemplace por este nuevo, evitando duplicados.
        WorkManager.getInstance(context).enqueueUniqueWork(
            "manual_image_sync", // Un nombre único para este trabajo manual
            ExistingWorkPolicy.REPLACE,
            imageSyncRequest
        )
    }
}
