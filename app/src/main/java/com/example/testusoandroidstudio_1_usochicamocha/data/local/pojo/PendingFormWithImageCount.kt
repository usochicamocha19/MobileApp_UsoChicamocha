package com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo
import androidx.room.Embedded
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity

data class PendingFormWithImageCount(
    @Embedded
    val formEntity: FormEntity,
    val totalImageCount: Int,
    val syncedImageCount: Int
)