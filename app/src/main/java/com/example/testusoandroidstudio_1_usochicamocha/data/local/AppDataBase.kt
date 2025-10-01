
package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MaintenanceDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.OilDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity

@Database(
    entities = [FormEntity::class, MachineEntity::class, LogEntity::class, MaintenanceEntity::class, OilEntity::class, ImageEntity::class],
    version = 17,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
    abstract fun machineDao(): MachineDao
    abstract fun logDao(): LogDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun oilDao(): OilDao

    abstract fun imageDao(): ImageDao

    companion object {
        val MIGRATION_10_11: Migration = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `maintenance` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `machineId` INTEGER NOT NULL, `dateTime` TEXT NOT NULL, `brand` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `currentHourMeter` INTEGER NOT NULL, `averageHoursChange` INTEGER NOT NULL, `type` TEXT NOT NULL, `isSynced` INTEGER NOT NULL DEFAULT 0)")
            }
        }
    }
}
