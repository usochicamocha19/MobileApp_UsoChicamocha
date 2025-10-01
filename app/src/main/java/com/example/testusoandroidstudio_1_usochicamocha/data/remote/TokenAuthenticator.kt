package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


class TokenAuthenticator @Inject constructor(
    // Usamos 'dagger.Lazy' para evitar una dependencia circular con AuthRepository.
    private val authRepository: dagger.Lazy<AuthRepository>,
    private val appLogger: AppLogger
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Usamos runBlocking porque la interfaz de Authenticator es síncrona.
        return runBlocking {
            appLogger.log("Respuesta 401 recibida. Iniciando autenticador...")
            // Intentamos refrescar el token.
            val tokenRefreshResult = authRepository.get().refreshTokenIfNecessary()

            if (tokenRefreshResult.isSuccess) {
                appLogger.log("Refresh token válido. Reintentando petición original.")
                // Si el refresco fue exitoso, obtenemos el nuevo token de acceso.
                val newAccessToken = tokenRefreshResult.getOrNull()
                // Reintentamos la petición que falló, pero ahora con el nuevo token.
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                appLogger.log("Refresh token inválido. No se reintentará la petición.")
                // Si el refresco falla (ej. el refresh token también expiró),
                // cerramos la sesión y no reintentamos la petición (devolvemos null).
                authRepository.get().logout()
                null
            }
        }
    }
}
