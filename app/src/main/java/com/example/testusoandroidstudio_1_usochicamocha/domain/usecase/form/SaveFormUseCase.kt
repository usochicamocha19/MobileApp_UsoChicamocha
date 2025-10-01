package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import javax.inject.Inject

class SaveFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {

    /**
     * Invoca el caso de uso para guardar un formulario y sus imágenes asociadas.
     * @param form El objeto de dominio del formulario a guardar.
     * @param imageUris Una lista de strings con las URIs locales de las imágenes comprimidas.
     */
    suspend operator fun invoke(form: Form, imageUris: List<String>) {
        formRepository.saveFormLocally(form, imageUris)
    }
}

