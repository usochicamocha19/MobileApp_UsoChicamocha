package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OilDao {
    @Query("SELECT * FROM oils ORDER BY name ASC")
    fun getAllOils(): Flow<List<OilEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(oils: List<OilEntity>)

    @Query("DELETE FROM oils")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(oils: List<OilEntity>) {
        deleteAll()
        insertAll(oils)
    }
}