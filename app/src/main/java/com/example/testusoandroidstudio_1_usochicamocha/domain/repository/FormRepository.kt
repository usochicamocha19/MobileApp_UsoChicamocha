package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PendingFormStatus
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que define las operaciones disponibles para la gestión de formularios.
 * Actúa como un puente entre la capa de dominio y la capa de datos.
 */
interface FormRepository {

    /**
     * AÑADIDO: Declara la nueva función que obtiene el estado detallado de los formularios.
     * El UseCase necesita que esta función exista aquí para poder llamarla.
     */
    fun getPendingFormsWithStatus(): Flow<List<PendingFormStatus>>
    // --- Operaciones de Formularios ---
    fun getPendingForms(): Flow<List<Form>>
    suspend fun saveFormLocally(form: Form, imageUris: List<String>)
    suspend fun syncForm(form: Form): Result<Unit>

    // --- Operaciones de Imágenes ---
    fun getPendingImagesForSync(): Flow<List<ImageForSync>>
    suspend fun syncImage(formId: Long, imageUri: String): Result<Unit>
    suspend fun markImageAsSynced(localId: Int)
}

