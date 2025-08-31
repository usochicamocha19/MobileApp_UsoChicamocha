package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormDto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


class FormRepositoryImpl @Inject constructor(
    private val formDao: FormDao,
    private val apiService: ApiService

) : FormRepository {

    // --- IMPLEMENTACIÓN DEL MÉTODO ---
    override fun getPendingForms(): Flow<List<Form>> {
        return formDao.getPendingForms().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun saveFormLocally(form: Form) {
        val formEntity = form.toEntity()
        formDao.insertForm(formEntity)
    }

    override suspend fun syncForm(form: Form): Result<Unit> {
        return try {
            // 1. Convertimos el objeto de dominio a un DTO para la red.
            val formDto = form.toDto()

            // 2. Hacemos la llamada a la API.
            val response = apiService.syncForm(formDto)

            if (response.isSuccessful) {
                // 3. Si la API responde OK, marcamos el formulario como sincronizado en la BD local.
                formDao.markAsSynced(form.UUID)
                Result.success(Unit)
            } else {
                // Si la API responde con un error (4xx, 5xx), devolvemos un fallo.
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si ocurre cualquier otra excepción (ej. no hay internet), la capturamos.
            Result.failure(e)
        }
    }

    private fun Form.toDto(): FormDto {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val isoDateString = sdf.format(Date(this.timestamp))

        return FormDto(
            UUID = this.UUID,
            dateStamp = isoDateString,
            hourMeter = this.hourmeter, // CAMBIADO hourmeter a hourMeter en el DTO
            leakStatus = this.leakStatus,
            brakeStatus = this.brakeStatus,
            beltsPulleysStatus = this.beltsPulleysStatus,
            tireLanesStatus = this.tireLanesStatus,
            carIgnitionStatus = this.carIgnitionStatus,
            electricalStatus = this.electricalStatus,
            mechanicalStatus = this.mechanicalStatus,
            temperatureStatus = this.temperatureStatus,
            oilStatus = this.oilStatus,
            hydraulicStatus = this.hydraulicStatus,
            coolantStatus = this.coolantStatus,
            structuralStatus = this.structuralStatus,
            expirationDateFireExtinguisher = this.expirationDateFireExtinguisher,
            observations = this.observations,
            userId = this.userId,
            machineId = this.machineId,
            greasingAction = this.greasingAction, 
            greasingObservations = this.greasingObservations, 
            isUnexpected = this.isUnexpected
        )
    }
}