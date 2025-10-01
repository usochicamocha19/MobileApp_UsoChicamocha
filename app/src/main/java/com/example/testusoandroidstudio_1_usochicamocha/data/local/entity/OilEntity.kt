package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil

@Entity(tableName = "oils")
data class OilEntity(
    @PrimaryKey val id: Int,
    val type: String, // "motor" o "hydraulic"
    val name: String
)

fun OilEntity.toDomain(): Oil {
    return Oil(
        id = this.id,
        type = this.type,
        name = this.name
    )
}