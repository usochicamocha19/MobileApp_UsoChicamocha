package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Inspection

@Entity(
    tableName = "inspections",
    foreignKeys = [
        ForeignKey(
            entity = MachineEntity::class,
            parentColumns = ["id"],
            childColumns = ["machineId"],
            onDelete = ForeignKey.CASCADE // O la acción que prefieras (SET_NULL, RESTRICT, etc.)
        )
        // Si tienes una UserEntity, descomenta esto y asegúrate que UserEntity existe:
        /*
        ,ForeignKey(
            entity = UserEntity::class, // Asegúrate que UserEntity existe y tiene un @PrimaryKey id de tipo Long
            parentColumns = ["id"],     // Reemplaza "id" si la PK de UserEntity tiene otro nombre
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // O la acción que prefieras
        )
        */
    ],
    // Crear índices para las columnas que son claves foráneas o se usan frecuentemente en WHERE clauses
    indices = [Index(value = ["machineId"]), Index(value = ["userId"])]
)
data class InspectionEntity(
    @PrimaryKey val id: Long,
    val UUID: String, // Asumimos no nulo debido al mapeo en DTO
    val beltsPulleysStatus: String,
    val brakeStatus: String,
    val carIgnitionStatus: String,
    val coolantStatus: String,
    val dateStamp: String,
    val electricalStatus: String,
    val expirationDateFireExtinguisher: String,
    val hourmeter: String,
    val hydraulicStatus: String,
    val leakStatus: String,
    val mechanicalStatus: String,
    val observations: String,
    val oilStatus: String,
    val structuralStatus: String,
    val temperatureStatus: String,
    val tireLanesStatus: String,
    val machineId: Long,
    val userId: Long
)

// Mapeo a modelo de Dominio (Inspection)
fun InspectionEntity.toDomain(): Inspection {
    return Inspection(
        id = id,
        UUID = UUID,
        beltsPulleysStatus = beltsPulleysStatus,
        brakeStatus = brakeStatus,
        carIgnitionStatus = carIgnitionStatus,
        coolantStatus = coolantStatus,
        dateStamp = dateStamp,
        electricalStatus = electricalStatus,
        expirationDateFireExtinguisher = expirationDateFireExtinguisher,
        hourmeter = hourmeter,
        hydraulicStatus = hydraulicStatus,
        leakStatus = leakStatus,
        mechanicalStatus = mechanicalStatus,
        observations = observations,
        oilStatus = oilStatus,
        structuralStatus = structuralStatus,
        temperatureStatus = temperatureStatus,
        tireLanesStatus = tireLanesStatus,
        machineId = machineId,
        userId = userId
    )
}
