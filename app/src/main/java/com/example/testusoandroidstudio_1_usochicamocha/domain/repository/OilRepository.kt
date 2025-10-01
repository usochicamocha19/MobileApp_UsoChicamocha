package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import kotlinx.coroutines.flow.Flow

interface OilRepository {
    suspend fun syncOils(): Result<Unit>
    fun getLocalOils(): Flow<List<Oil>>
}