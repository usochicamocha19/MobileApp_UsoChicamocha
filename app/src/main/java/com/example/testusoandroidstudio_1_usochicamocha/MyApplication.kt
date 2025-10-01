package com.example.testusoandroidstudio_1_usochicamocha

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.ImageSyncWorker
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundSync()
        triggerImmediateSync()
    }

    private fun scheduleBackgroundSync() {
        // 1. Definimos las restricciones comunes: solo se ejecuta si hay internet.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 2. Creamos la petición para el WORKER DE DATOS (TEXTO)
        val syncDataRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // 3. Planificamos el trabajo de datos de forma única.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "background_sync_worker",
            ExistingPeriodicWorkPolicy.KEEP, // Mantiene el trabajo existente si ya está planificado
            syncDataRequest
        )

        // 4. AÑADIDO: Creamos la petición para el WORKER DE IMÁGENES
        // Se ejecuta con menos frecuencia para ahorrar batería, ya que es una tarea más pesada.
        val imageSyncRequest = PeriodicWorkRequestBuilder<ImageSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // 5. AÑADIDO: Planificamos el trabajo de imágenes de forma única.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "background_image_sync_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            imageSyncRequest
        )
    }

    private fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Creamos una petición de UNA SOLA VEZ para los datos
        val oneTimeSyncDataRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        // Creamos una petición de UNA SOLA VEZ para las imágenes
        val oneTimeImageSyncRequest = OneTimeWorkRequestBuilder<ImageSyncWorker>()
            .setConstraints(constraints)
            .build()

        // Encolamos el trabajo de forma única para evitar duplicados si la app se reinicia rápido
        // ExistingWorkPolicy.KEEP: si ya hay un trabajo en cola, no hace nada.
        WorkManager.getInstance(this).enqueueUniqueWork(
            "immediate_data_sync",
            ExistingWorkPolicy.KEEP,
            oneTimeSyncDataRequest
        )
        WorkManager.getInstance(this).enqueueUniqueWork(
            "immediate_image_sync",
            ExistingWorkPolicy.KEEP,
            oneTimeImageSyncRequest
        )
    }
}
