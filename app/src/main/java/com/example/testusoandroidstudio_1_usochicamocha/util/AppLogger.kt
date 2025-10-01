package com.example.testusoandroidstudio_1_usochicamocha.util

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Herramienta centralizada para guardar logs en la base de datos y en Logcat.
 */
@Singleton
class AppLogger @Inject constructor(
    private val logDao: LogDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "AppLogger"

    fun log(message: String) {
        scope.launch {
            // Log to database
            val logEntry = LogEntity(
                timestamp = System.currentTimeMillis(),
                message = message
            )
            logDao.insert(logEntry)

            // Log to console (Logcat)
            Log.d(TAG, message)
        }
    }
}
