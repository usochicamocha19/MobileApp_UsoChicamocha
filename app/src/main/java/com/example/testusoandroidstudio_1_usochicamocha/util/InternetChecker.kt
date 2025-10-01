package com.example.testusoandroidstudio_1_usochicamocha.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


suspend fun hasRealInternetAccess(): Boolean {
    // Ejecutamos la operación de red en el hilo de IO para no bloquear la UI.
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://clients3.google.com/generate_204")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("User-Agent", "Android")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 1500 // 1.5 segundos de timeout
            urlConnection.connect()
            // Si el código de respuesta es 204, significa que tenemos conexión real.
            return@withContext urlConnection.responseCode == 204
        } catch (e: Exception) {
            // Si ocurre cualquier excepción (ej. timeout, no hay ruta al host), no hay internet.
            return@withContext false
        }
    }
}