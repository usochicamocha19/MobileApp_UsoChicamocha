package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Caso de uso con la única responsabilidad de sincronizar las imágenes pendientes.
 * Se encarga de la orquestación: obtener imágenes, intentar subirlas y marcarlas como completadas.
 */
class SyncPendingImagesUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    // La función invoke permite que la clase sea llamada como si fuera una función.
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 1. Obtenemos solo las imágenes de formularios cuyo texto ya se ha sincronizado.
            val pendingImages = formRepository.getPendingImagesForSync().first()

            if (pendingImages.isEmpty()) {
                Log.d("SyncPendingImagesUC", "No hay imágenes pendientes para sincronizar.")
                return Result.success(Unit) // No hay nada que hacer, es un éxito.
            }

            Log.d("SyncPendingImagesUC", "Se encontraron ${pendingImages.size} imágenes para sincronizar.")

            // 2. Iteramos sobre cada imagen pendiente y la intentamos subir.
            var successCount = 0
            for (image in pendingImages) {
                val syncResult = formRepository.syncImage(image.serverId, image.localUri)
                if (syncResult.isSuccess) {
                    // 3. Si la subida es exitosa, la marcamos en la base de datos local.
                    formRepository.markImageAsSynced(image.localId)
                    successCount++
                } else {
                    // Si una imagen falla, registramos el error y continuamos con la siguiente.
                    Log.e("SyncPendingImagesUC", "Falló la sincronización para la imagen con ID local: ${image.localId}")
                }
            }

            Log.d("SyncPendingImagesUC", "Sincronización de lote completada. Éxitos: $successCount de ${pendingImages.size}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SyncPendingImagesUC", "Ocurrió un error general en el caso de uso.", e)
            Result.failure(e)
        }
    }
}
