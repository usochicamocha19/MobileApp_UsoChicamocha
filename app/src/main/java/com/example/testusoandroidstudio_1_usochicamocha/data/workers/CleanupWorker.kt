package com.example.testusoandroidstudio_1_usochicamocha.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker de limpieza simplificado que mantiene la base de datos ordenada.
 * Resetea locks colgados de formularios.
 */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val formDao: FormDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CleanupWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting cleanup operation...")
        
        return try {
            // Resetea formularios que quedaron en estado de sincronización
            val resetFormsCount = resetStuckSyncingForms()
            Log.d(TAG, "Reset $resetFormsCount stuck syncing forms")
            
            Log.d(TAG, "Cleanup operation completed successfully")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup operation", e)
            Result.retry() // Retry on error
        }
    }

    /**
     * Resetea formularios que quedaron en estado isSyncing = 1
     * (locks que no se liberaron correctamente)
     */
    private suspend fun resetStuckSyncingForms(): Int {
        return try {
            formDao.resetStuckSyncingForms()
            Log.d(TAG, "Reset stuck syncing forms")
            1 // Retorna el número de formularios reseteados
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting stuck syncing forms", e)
            0
        }
    }
}