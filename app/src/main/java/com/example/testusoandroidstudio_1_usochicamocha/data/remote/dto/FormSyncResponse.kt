package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Representa la respuesta del servidor después de sincronizar un formulario.
 * Contiene el ID que el servidor le asignó al nuevo registro.
 */
data class FormSyncResponse(
    @SerializedName("id")
    val id: Long
)
