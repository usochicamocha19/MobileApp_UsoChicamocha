package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.OilRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalOilsUseCase @Inject constructor(
    private val repository: OilRepository
) {
    operator fun invoke(): Flow<List<Oil>> {
        return repository.getLocalOils()
    }
}