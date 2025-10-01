package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.OilRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import javax.inject.Inject

class SyncOilsUseCase @Inject constructor(
    private val oilRepository: OilRepository,
    private val logger: AppLogger
) {
    suspend operator fun invoke(): Result<Unit> {
        logger.log("Iniciando sincronización de aceites...")
        val result = oilRepository.syncOils()
        if (result.isSuccess) {
            logger.log("Sincronización de aceites exitosa.")
        } else {
            logger.log("Sincronización de aceites fallida: ${result.exceptionOrNull()?.message}")
        }
        return result
    }
}