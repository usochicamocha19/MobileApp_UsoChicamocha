package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PendingFormStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingFormsWithStatusUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    operator fun invoke(): Flow<List<PendingFormStatus>> {
        return formRepository.getPendingFormsWithStatus()
    }
}
