package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity
import com.google.gson.annotations.SerializedName

data class OilDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("name")
    val name: String
)

fun OilDto.toEntity(): OilEntity {
    return OilEntity(
        id = this.id,
        type = this.type,
        name = this.name
    )
}