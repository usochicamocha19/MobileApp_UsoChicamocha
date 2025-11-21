package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncPendingImagesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.SyncMaintenanceFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.GetPendingFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.GetPendingMaintenanceFormsUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Caso de uso que realiza sincronizaciones aleatorias de diferentes tipos de datos.
 * Coordina la sincronización de formularios, mantenimientos, máquinas, aceites e imágenes.
 */
class SyncAllUseCase @Inject constructor(
    private val syncFormUseCase: SyncFormUseCase,
    private val syncMaintenanceFormsUseCase: SyncMaintenanceFormsUseCase,
    private val syncMachinesUseCase: SyncMachinesUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val syncPendingImagesUseCase: SyncPendingImagesUseCase,
    private val getPendingFormsUseCase: GetPendingFormsUseCase,
    private val getPendingMaintenanceFormsUseCase: GetPendingMaintenanceFormsUseCase
) {

    /**
     * Ejecuta una operación de sincronización aleatoria.
     * Selecciona aleatoriamente entre diferentes tipos de sincronización disponibles.
     * MODIFICADO: Evita ejecutar sincronización de imágenes para prevenir duplicación.
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            Log.d("RandomSyncUseCase", "Iniciando sincronización aleatoria...")

            // Definir las operaciones de sincronización disponibles
            // EXCLUIMOS syncPendingImages() para evitar conflictos con otros workers
            val syncOperations = listOf(
                suspend { syncForms() },
                suspend { syncMaintenanceForms() },
                suspend { syncMachines() },
                suspend { syncOils() }
            )

            // Seleccionar una operación aleatoria
            val randomIndex = Random.nextInt(syncOperations.size)
            val selectedOperation = syncOperations[randomIndex]

            Log.d("RandomSyncUseCase", "Operación seleccionada: ${getOperationName(randomIndex)}")

            // Ejecutar la operación seleccionada
            val result = selectedOperation()

            if (result.isSuccess) {
                Log.d("RandomSyncUseCase", "Sincronización aleatoria completada exitosamente")
            } else {
                Log.e("RandomSyncUseCase", "Sincronización aleatoria fallida: ${result.exceptionOrNull()?.message}")
            }

            result

        } catch (e: Exception) {
            Log.e("RandomSyncUseCase", "Error general en sincronización aleatoria", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza imágenes pendientes.
     */
    private suspend fun syncPendingImages(): Result<Unit> {
        return try {
            Log.d("RandomSyncUseCase", "Sincronizando imágenes pendientes...")
            syncPendingImagesUseCase()
        } catch (e: Exception) {
            Log.e("RandomSyncUseCase", "Error sincronizando imágenes", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza formularios pendientes.
     */
    private suspend fun syncForms(): Result<Unit> {
        return try {
            Log.d("RandomSyncUseCase", "Obteniendo formularios pendientes...")
            val pendingForms = getPendingFormsUseCase().first()

            if (pendingForms.isEmpty()) {
                Log.d("RandomSyncUseCase", "No hay formularios pendientes para sincronizar")
                return Result.success(Unit)
            }

            Log.d("RandomSyncUseCase", "Sincronizando ${pendingForms.size} formularios...")

            // Sincronizar cada formulario
            var successCount = 0
            for (form in pendingForms) {
                val result = syncFormUseCase(form)
                if (result.isSuccess) {
                    successCount++
                    Log.d("RandomSyncUseCase", "Formulario sincronizado exitosamente: ${form.localId}")
                } else {
                    Log.e("RandomSyncUseCase", "Error sincronizando formulario ${form.localId}: ${result.exceptionOrNull()?.message}")
                }
            }

            Log.d("RandomSyncUseCase", "Sincronización de formularios completada. Éxitos: $successCount de ${pendingForms.size}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("RandomSyncUseCase", "Error obteniendo formularios pendientes", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza formularios de mantenimiento pendientes.
     */
    private suspend fun syncMaintenanceForms(): Result<Unit> {
        return try {
            Log.d("RandomSyncUseCase", "Obteniendo formularios de mantenimiento pendientes...")
            val pendingMaintenanceForms = getPendingMaintenanceFormsUseCase().first()

            if (pendingMaintenanceForms.isEmpty()) {
                Log.d("RandomSyncUseCase", "No hay formularios de mantenimiento pendientes para sincronizar")
                return Result.success(Unit)
            }

            Log.d("RandomSyncUseCase", "Sincronizando ${pendingMaintenanceForms.size} formularios de mantenimiento...")

            // Sincronizar cada formulario de mantenimiento
            var successCount = 0
            for (maintenance in pendingMaintenanceForms) {
                val result = syncMaintenanceFormsUseCase(maintenance)
                if (result.isSuccess) {
                    successCount++
                    Log.d("RandomSyncUseCase", "Formulario de mantenimiento sincronizado exitosamente: ${maintenance.id}")
                } else {
                    Log.e("RandomSyncUseCase", "Error sincronizando formulario de mantenimiento ${maintenance.id}: ${result.exceptionOrNull()?.message}")
                }
            }

            Log.d("RandomSyncUseCase", "Sincronización de formularios de mantenimiento completada. Éxitos: $successCount de ${pendingMaintenanceForms.size}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("RandomSyncUseCase", "Error obteniendo formularios de mantenimiento pendientes", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza máquinas.
     */
    private suspend fun syncMachines(): Result<Unit> {
        return try {
            Log.d("RandomSyncUseCase", "Sincronizando máquinas...")
            syncMachinesUseCase()
        } catch (e: Exception) {
            Log.e("RandomSyncUseCase", "Error sincronizando máquinas", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza aceites.
     */
    private suspend fun syncOils(): Result<Unit> {
        return try {
            Log.d("RandomSyncUseCase", "Sincronizando aceites...")
            syncOilsUseCase()
        } catch (e: Exception) {
            Log.e("RandomSyncUseCase", "Error sincronizando aceites", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el nombre de la operación basado en el índice.
     */
    private fun getOperationName(index: Int): String {
        return when (index) {
            0 -> "SyncPendingImages"
            1 -> "SyncForms"
            2 -> "SyncMaintenanceForms"
            3 -> "SyncMachines"
            4 -> "SyncOils"
            else -> "Unknown"
        }
    }
}