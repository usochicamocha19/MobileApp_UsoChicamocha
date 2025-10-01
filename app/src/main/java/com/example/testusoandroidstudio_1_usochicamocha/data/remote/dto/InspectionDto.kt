package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.InspectionEntity
import com.google.gson.annotations.SerializedName

data class InspectionDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("UUID")
    val UUID: String?,
    @SerializedName("belts_pulleys_status")
    val beltsPulleysStatus: String?,
    @SerializedName("brake_status")
    val brakeStatus: String?,
    @SerializedName("car_ignition_status")
    val carIgnitionStatus: String?,
    @SerializedName("coolant_status")
    val coolantStatus: String?,
    @SerializedName("date_stamp")
    val dateStamp: String?,
    @SerializedName("electrical_status")
    val electricalStatus: String?,
    @SerializedName("expiration_date_fire_extinguisher")
    val expirationDateFireExtinguisher: String?,
    @SerializedName("hourmeter")
    val hourmeter: String?,
    @SerializedName("hydraulic_status")
    val hydraulicStatus: String?,
    @SerializedName("leak_status")
    val leakStatus: String?,
    @SerializedName("mechanical_status")
    val mechanicalStatus: String?,
    @SerializedName("observations")
    val observations: String?,
    @SerializedName("oil_status")
    val oilStatus: String?,
    @SerializedName("structural_status")
    val structuralStatus: String?,
    @SerializedName("temperature_status")
    val temperatureStatus: String?,
    @SerializedName("tire_lanes_status")
    val tireLanesStatus: String?,
    @SerializedName("machine_id")
    val machineId: Long,
    @SerializedName("user_id")
    val userId: Long
)


fun InspectionDto.toEntity(): InspectionEntity {
    return InspectionEntity(
        id = id,
        UUID = UUID ?: "",
        beltsPulleysStatus = beltsPulleysStatus ?: "N/A",
        brakeStatus = brakeStatus ?: "N/A",
        carIgnitionStatus = carIgnitionStatus ?: "N/A",
        coolantStatus = coolantStatus ?: "N/A",
        dateStamp = dateStamp ?: "",
        electricalStatus = electricalStatus ?: "N/A",
        expirationDateFireExtinguisher = expirationDateFireExtinguisher ?: "N/A",
        hourmeter = hourmeter ?: "0",
        hydraulicStatus = hydraulicStatus ?: "N/A",
        leakStatus = leakStatus ?: "N/A",
        mechanicalStatus = mechanicalStatus ?: "N/A",
        observations = observations ?: "",
        oilStatus = oilStatus ?: "N/A",
        structuralStatus = structuralStatus ?: "N/A",
        temperatureStatus = temperatureStatus ?: "N/A",
        tireLanesStatus = tireLanesStatus ?: "N/A",
        machineId = machineId,
        userId = userId
    )
}
