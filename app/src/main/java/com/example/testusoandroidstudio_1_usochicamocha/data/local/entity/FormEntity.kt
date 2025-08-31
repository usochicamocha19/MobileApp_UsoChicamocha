package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form

@Entity(tableName = "pending_forms")
data class FormEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val UUID: String,
    val timestamp: Long, // Fecha de creación local
    val machineId: Long, // Anteriormente equipoId, tipo Long
    val userId: Long,    // Anteriormente usuarioId, tipo Long
    val hourmeter: String, // Anteriormente horometro, tipo String
    val leakStatus: String, // Anteriormente estadoFugas
    val brakeStatus: String, // Anteriormente estadoFrenos
    val beltsPulleysStatus: String, // Anteriormente estadoCorreasPoleas
    val tireLanesStatus: String, // Anteriormente estadoLlantasCarriles
    val carIgnitionStatus: String, // Anteriormente estadoEncendido
    val electricalStatus: String, // Anteriormente estadoElectrico
    val mechanicalStatus: String, // Anteriormente estadoMecanico
    val temperatureStatus: String, // Anteriormente estadoTemperatura
    val oilStatus: String, // Anteriormente estadoAceite
    val hydraulicStatus: String, // Anteriormente estadoHidraulico
    val coolantStatus: String, // Anteriormente estadoRefrigerante
    val structuralStatus: String, // Anteriormente estadoEstructural
    val expirationDateFireExtinguisher: String, // Anteriormente vigenciaExtintor
    val observations: String, // Anteriormente observaciones
    val greasingAction: String, // Nuevo campo
    val greasingObservations: String, // Nuevo campo
    val isUnexpected: Boolean, // <--- NUEVO CAMPO AÑADIDO
    var isSynced: Boolean = false
)

fun FormEntity.toDomain(): Form {
    return Form(
        localId = localId,
        UUID = this.UUID,
        timestamp = timestamp,
        machineId = machineId,
        userId = userId,
        hourmeter = hourmeter,
        leakStatus = leakStatus,
        brakeStatus = brakeStatus,
        beltsPulleysStatus = beltsPulleysStatus,
        tireLanesStatus = tireLanesStatus,
        carIgnitionStatus = carIgnitionStatus,
        electricalStatus = electricalStatus,
        mechanicalStatus = mechanicalStatus,
        temperatureStatus = temperatureStatus,
        oilStatus = oilStatus,
        hydraulicStatus = hydraulicStatus,
        coolantStatus = coolantStatus,
        structuralStatus = structuralStatus,
        expirationDateFireExtinguisher = expirationDateFireExtinguisher,
        observations = observations,
        greasingAction = this.greasingAction,
        greasingObservations = this.greasingObservations,
        isUnexpected = this.isUnexpected, // <--- NUEVO CAMPO MAPEADO
        isSynced = isSynced
    )
}
