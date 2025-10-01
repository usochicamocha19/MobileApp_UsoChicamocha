package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.OilDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.OilRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OilRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val oilDao: OilDao
) : OilRepository {

    override suspend fun syncOils(): Result<Unit> {
        return try {
            val response = apiService.getOils()
            if (response.isSuccessful && response.body() != null) {
                val oilDtos = response.body()!!
                val oilEntities = oilDtos.map { it.toEntity() }
                oilDao.clearAndInsert(oilEntities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al obtener los aceites del servidor."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocalOils(): Flow<List<Oil>> {
        return oilDao.getAllOils().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}