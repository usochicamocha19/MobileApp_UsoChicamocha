package com.example.testusoandroidstudio_1_usochicamocha.ui.form

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SaveFormUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class FormUiState(
    val horometro: String = "",
    val observaciones: String = "",
    val estadoFugas: String = "",
    val estadoFrenos: String = "",
    val estadoCorreasPoleas: String = "",
    val estadoLlantasCarriles: String = "",
    val estadoEncendido: String = "",
    val estadoElectrico: String = "",
    val estadoMecanico: String = "",
    val estadoTemperatura: String = "",
    val estadoAceite: String = "",
    val estadoHidraulico: String = "",
    val estadoRefrigerante: String = "",
    val estadoEstructural: String = "",
    val vigenciaExtintor: String = "",

    val previewingImageUri: Uri? = null,
    val selectedImageUris: List<Uri> = emptyList(),
    val machines: List<Machine> = emptyList(),
    val selectedMachine: Machine? = null,
    val saveCompleted: Boolean = false,
    // Nuevos campos para engrasado
    val greasingStatus: String = "", // "Sí", "No", o "" (inicial)
    val greasingAction: String = "", // "Total", "Parcial", o ""
    val greasingObservations: String = ""
)

@HiltViewModel
class FormViewModel @Inject constructor(
    private val saveFormUseCase: SaveFormUseCase,
    private val getLocalMachinesUseCase: GetLocalMachinesUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getLocalMachinesUseCase().collect { machines ->
                _uiState.update { it.copy(machines = machines) }
            }
        }
        // Inicializar vigenciaExtintor con el mes y año actual si está vacío
        if (_uiState.value.vigenciaExtintor.isEmpty()) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            onExtinguisherDateChange(year, month) // Llama al método existente para formatear
        }
    }

    fun onMachineSelected(machine: Machine) {
        _uiState.update { it.copy(selectedMachine = machine) }
    }

    fun onHorometroChange(value: String) {
        _uiState.update { it.copy(horometro = value) }
    }

    fun onObservacionesChange(value: String) {
        _uiState.update { it.copy(observaciones = value) }
    }

    // --- Inicio: Nuevas funciones para Engrasado ---
    fun onGreasedStatusChange(status: String) {
        _uiState.update {
            if (status == "No") {
                // Si es "No", resetea la acción de engrasado y las observaciones de engrasado
                it.copy(greasingStatus = status, greasingAction = "", greasingObservations = "")
            } else {
                it.copy(greasingStatus = status)
            }
        }
    }

    fun onGreasingActionChange(action: String) {
        // Solo permite cambiar la acción si el estado de engrasado es "Sí"
        if (_uiState.value.greasingStatus == "Sí") {
            _uiState.update { it.copy(greasingAction = action) }
        }
    }

    fun onGreasingObservationsChange(observations: String) {
        // Solo permite cambiar las observaciones si el estado de engrasado es "Sí"
        if (_uiState.value.greasingStatus == "Sí") {
            _uiState.update { it.copy(greasingObservations = observations) }
        }
    }
    // --- Fin: Nuevas funciones para Engrasado ---

    fun onExtinguisherDateChange(year: Int, month: Int) {
        val formattedMonth = String.format("%02d", month + 1)
        _uiState.update { it.copy(vigenciaExtintor = "$year-$formattedMonth") }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(selectedImageUris = it.selectedImageUris + uri) }
    }

    fun onImageRemoved(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedImageUris = currentState.selectedImageUris.filter { it != uri },
                previewingImageUri = if (currentState.previewingImageUri == uri) null else currentState.previewingImageUri
            )
        }
    }
    fun onPreviewImageClicked(uri: Uri) {
        _uiState.update { it.copy(previewingImageUri = uri) }
    }

    fun onDismissPreview() {
        _uiState.update { it.copy(previewingImageUri = null) }
    }

    fun onEstadoChange(fieldName: String, value: String) {
        _uiState.update { currentState ->
            when (fieldName) {
                "Fugas" -> currentState.copy(estadoFugas = value)
                "Frenos" -> currentState.copy(estadoFrenos = value)
                "CorreasPoleas" -> currentState.copy(estadoCorreasPoleas = value)
                "LlantasCarriles" -> currentState.copy(estadoLlantasCarriles = value)
                "Encendido" -> currentState.copy(estadoEncendido = value)
                "Electrico" -> currentState.copy(estadoElectrico = value)
                "Mecanico" -> currentState.copy(estadoMecanico = value)
                "Temperatura" -> currentState.copy(estadoTemperatura = value)
                "Aceite" -> currentState.copy(estadoAceite = value)
                "Hidraulico" -> currentState.copy(estadoHidraulico = value)
                "Refrigerante" -> currentState.copy(estadoRefrigerante = value)
                "Estructural" -> currentState.copy(estadoEstructural = value)
                else -> currentState
            }
        }
    }

    fun onSaveClick() {
        if (_uiState.value.selectedMachine == null) {
            Log.d("FormViewModel", "Intento de guardado sin seleccionar máquina.")
            return
        }
        val selectedMachineId = _uiState.value.selectedMachine?.id?.toLong() ?: return

        viewModelScope.launch {
            Log.d("FormViewModel", "Iniciando guardado de formulario...")
            val currentState = _uiState.value
            val currentUserId = tokenManager.getUserId().firstOrNull()?.toLong()

            if (currentUserId == null) {
                Log.e("FormViewModel", "Error: No se pudo obtener el ID del usuario.")
                return@launch
            }

            // Determinar el valor final para greasingAction
            val finalGreasingAction = when (currentState.greasingStatus) {
                "Sí" -> currentState.greasingAction // "Total" o "Parcial"
                "No" -> "No"
                else -> "" // Si no se seleccionó ni Sí ni No, podría ser un string vacío o manejarlo como error
            }

            val form = Form(
                localId = 0,
                UUID = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                machineId = selectedMachineId,
                userId = currentUserId,
                hourmeter = currentState.horometro,
                observations = currentState.observaciones,
                leakStatus = currentState.estadoFugas,
                brakeStatus = currentState.estadoFrenos,
                beltsPulleysStatus = currentState.estadoCorreasPoleas,
                tireLanesStatus = currentState.estadoLlantasCarriles,
                carIgnitionStatus = currentState.estadoEncendido,
                electricalStatus = currentState.estadoElectrico,
                mechanicalStatus = currentState.estadoMecanico,
                temperatureStatus = currentState.estadoTemperatura,
                oilStatus = currentState.estadoAceite,
                hydraulicStatus = currentState.estadoHidraulico,
                coolantStatus = currentState.estadoRefrigerante,
                structuralStatus = currentState.estadoEstructural,
                expirationDateFireExtinguisher = currentState.vigenciaExtintor,
                greasingAction = finalGreasingAction,
                greasingObservations = if (currentState.greasingStatus == "Sí") currentState.greasingObservations else "",
                isUnexpected = false, // <-- AÑADIDO: Se envía 'false' para el formulario normal
                isSynced = false
            )
            saveFormUseCase(form)
            Log.d("FormViewModel", "Formulario guardado localmente: $form")
            _uiState.update { it.copy(saveCompleted = true) }
        }
    }

    fun onNavigationDone() {
        _uiState.update { it.copy(saveCompleted = false) }
    }
}
