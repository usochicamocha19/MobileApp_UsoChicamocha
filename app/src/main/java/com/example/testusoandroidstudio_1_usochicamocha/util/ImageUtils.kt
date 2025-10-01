package com.example.testusoandroidstudio_1_usochicamocha.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * CORRECCIÓN: Convertido de una 'class' a un 'object'.
 * Un 'object' en Kotlin es un Singleton, lo que nos permite llamar a sus métodos
 * directamente (ej. ImageUtils.compressAndSaveImage(...)) sin necesidad de inyectarlo.
 * Esto lo convierte en una verdadera clase de utilidad.
 */
object ImageUtils {

    /**
     * CORRECCIÓN: El nombre del método ahora coincide con lo que el ViewModel espera.
     * También, como esto ya no es una clase inyectada, el método debe aceptar
     * el 'context' como parámetro para poder trabajar con archivos.
     */
    fun compressAndSaveImage(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            var quality = 100
            val outputStream = ByteArrayOutputStream()

            // Comprime repetidamente bajando la calidad hasta que el tamaño sea menor a 200KB
            do {
                outputStream.reset()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                quality -= 5
            } while (outputStream.size() > 200 * 1024 && quality > 10)

            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

