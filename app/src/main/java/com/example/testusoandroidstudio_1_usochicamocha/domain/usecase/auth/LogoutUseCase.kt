package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
