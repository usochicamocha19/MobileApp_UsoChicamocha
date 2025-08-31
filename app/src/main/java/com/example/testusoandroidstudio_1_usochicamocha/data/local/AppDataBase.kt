package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity

@Database(
    entities = [FormEntity::class, MachineEntity::class, LogEntity::class],
    version = 10, // <-- VERSIÓN INCREMENTADA
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
    abstract fun machineDao(): MachineDao
    abstract fun logDao(): LogDao

    companion object {
        val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Añadir las nuevas columnas a la tabla pending_forms
                // Se usa TEXT para String y INTEGER para Boolean (0=false, 1=true)
                // Se añade NOT NULL y un valor DEFAULT para evitar problemas con datos existentes.
                db.execSQL("ALTER TABLE pending_forms ADD COLUMN greasingAction TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pending_forms ADD COLUMN greasingObservations TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pending_forms ADD COLUMN isUnexpected INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
