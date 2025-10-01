package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/**
 * Representa el estado completo de una inspección pendiente para la UI.
 * Contiene el formulario y el estado de sincronización de sus imágenes.
 */
data class PendingFormStatus(
    val form: Form,
    val totalImageCount: Int,
    val syncedImageCount: Int
)