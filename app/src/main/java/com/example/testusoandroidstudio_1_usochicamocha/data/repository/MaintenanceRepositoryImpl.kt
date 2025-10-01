package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MaintenanceDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceDao: MaintenanceDao,
    private val apiService: ApiService
) : MaintenanceRepository {

    override suspend fun saveMaintenanceLocally(maintenance: Maintenance): Result<Unit> {
        return try {
            val entity = maintenance.toEntity()
            maintenanceDao.insert(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingMaintenanceForms(): Flow<List<Maintenance>> {
        return maintenanceDao.getPendingMaintenanceForms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMaintenanceForm(maintenance: Maintenance): Result<Unit> {
        return try {
            // --- CAMBIO: Llamamos a la nueva función de conversión ---
            val request = maintenance.toOilChangeRequest()
            val response = when (maintenance.type) {
                "motor" -> apiService.syncMotorOilChange(request)
                "hydraulic" -> apiService.syncHydraulicOilChange(request)
                else -> return Result.failure(IllegalArgumentException("Tipo de mantenimiento desconocido: ${maintenance.type}"))
            }

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error de API: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMaintenanceForm(id: Int): Result<Unit> {
        return try {
            maintenanceDao.deleteById(id)
            Result.success(Unit)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
}