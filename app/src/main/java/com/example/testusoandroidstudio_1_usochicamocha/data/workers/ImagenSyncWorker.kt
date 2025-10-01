package com.example.testusoandroidstudio_1_usochicamocha.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.SessionStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.ValidateSessionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncPendingImagesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker dedicado exclusivamente a la sincronización de imágenes en segundo plano.
 * Se ejecuta periódicamente para subir las imágenes de los formularios que ya han sido sincronizados.
 */
@HiltWorker
class ImageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val syncPendingImagesUseCase: SyncPendingImagesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ImageSyncWorker", "Iniciando trabajo de sincronización de imágenes...")

        // 1. Validar la sesión antes de consumir datos de red.
        val sessionStatus = validateSessionUseCase()
        if (sessionStatus == SessionStatus.EXPIRED) {
            Log.w("ImageSyncWorker", "Sesión expirada. El trabajo falló y no se reintentará.")
            return Result.failure() // Falla definitivamente si no hay sesión.
        }
        Log.d("ImageSyncWorker", "Sesión válida. Procediendo con la sincronización de imágenes.")

        // 2. Ejecutar el caso de uso que contiene toda la lógica.
        val result = syncPendingImagesUseCase()

        // 3. Devolver el resultado a WorkManager.
        return if (result.isSuccess) {
            Log.d("ImageSyncWorker", "Trabajo de sincronización de imágenes completado con éxito.")
            Result.success()
        } else {
            Log.e("ImageSyncWorker", "El trabajo falló. Se reintentará más tarde.")
            Result.retry() // Si algo falla (ej. sin red), WorkManager lo reintentará.
        }
    }
}
