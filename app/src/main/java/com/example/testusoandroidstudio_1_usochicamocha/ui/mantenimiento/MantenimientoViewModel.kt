package com.example.testusoandroidstudio_1_usochicamocha.ui.mantenimiento

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// AÑADIDO: Importaciones de WorkManager y los Workers
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.ImageSyncWorker
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.SaveMaintenanceFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MantenimientoViewModel @Inject constructor(
    private val saveMaintenanceFormUseCase: SaveMaintenanceFormUseCase,
    private val getLocalMachinesUseCase: GetLocalMachinesUseCase,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    // AÑADIDO: Inyectamos WorkManager
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MantenimientoUiState())
    val uiState: StateFlow<MantenimientoUiState> = _uiState.asStateFlow()


    init {
        loadMachines()
        loadOils()
    }
    private fun loadOils() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { oils ->
                _uiState.update {
                    it.copy(
                        allOils = oils,
                        motorOils = oils.filter { oil -> oil.type.equals("motor", ignoreCase = true) },
                        hydraulicOils = oils.filter { oil -> oil.type.equals("hidraulico", ignoreCase = true) }
                    )
                }
            }
        }
    }
    private fun loadMachines() {
        viewModelScope.launch {
            getLocalMachinesUseCase().collect { resource -> // El flow emite un Resource
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                // Extraemos la data del estado Success
                                machines = resource.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = resource.message ?: "Error al cargar máquinas",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun onFormEvent(event: MantenimientoFormEvent) {
        when (event) {
            is MantenimientoFormEvent.MachineSelected -> {
                _uiState.update { it.copy(selectedMachine = event.machine) }
            }
            is MantenimientoFormEvent.OilSelected -> { // <-- CAMBIA EL HANDLER
                _uiState.update { it.copy(selectedOil = event.oil) }
            }
            is MantenimientoFormEvent.QuantityChanged -> {
                _uiState.update { it.copy(quantity = event.quantity) }
            }
            is MantenimientoFormEvent.CurrentHourMeterChanged -> {
                _uiState.update { it.copy(currentHourMeter = event.hourMeter) }
            }
            is MantenimientoFormEvent.AverageHoursChangeChanged -> {
                _uiState.update { it.copy(averageHoursChange = event.hours) }
            }
            is MantenimientoFormEvent.MaintenanceTypeChanged -> {
                _uiState.update { it.copy(maintenanceType = event.type) }
            }
            MantenimientoFormEvent.Submit -> {
                submitForm()
            }
        }
    }

    private fun submitForm() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedMachine == null || state.maintenanceType == null || state.selectedOil == null) {
                _uiState.update { it.copy(error = "Debe seleccionar una máquina y un tipo de mantenimiento.") }
                return@launch
            }

            val form = Maintenance(
                machineId = state.selectedMachine.id,
                dateTime = Date().time,
                brand = state.selectedOil.name,
                brandId = state.selectedOil.id,
                quantity = state.quantity.toDoubleOrNull() ?: 0.0,
                currentHourMeter = state.currentHourMeter.toIntOrNull() ?: 0,
                averageHoursChange = state.averageHoursChange.toIntOrNull() ?: 0,
                type = state.maintenanceType
            )

            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = saveMaintenanceFormUseCase(form)

            result.onSuccess {
                // AÑADIDO: Iniciar el proceso de sincronización en segundo plano
                triggerImmediateSync()

                // Esto solo limpia los campos del formulario, pero mantiene
                // las listas de máquinas y aceites ya cargadas.
                _uiState.update { currentState ->
                    currentState.copy(
                        submissionSuccess = true,
                        isLoading = false,
                        selectedOil = null,
                        quantity = "",
                        currentHourMeter = "",
                        averageHoursChange = "",
                        selectedMachine = null,
                        maintenanceType = null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        error = exception.message ?: "Error al guardar el formulario.",
                        isLoading = false
                    )
                }
            }
        }
    }

    // AÑADIDO: Nueva función para encolar el trabajo de sincronización
    private fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // El formulario de mantenimiento es 'data', por lo que encolamos el SyncDataWorker.
        val dataSyncRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        // Aunque este formulario no maneja imágenes, es buena práctica encolar ambos
        // por si quedaron imágenes de otros formularios pendientes de sincronizar.
        val imageSyncRequest = OneTimeWorkRequestBuilder<ImageSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "immediate_data_sync_on_maintenance_save",
            ExistingWorkPolicy.REPLACE,
            dataSyncRequest
        )
        workManager.enqueueUniqueWork(
            "immediate_image_sync_on_maintenance_save",
            ExistingWorkPolicy.REPLACE,
            imageSyncRequest
        )

        Log.d("MantenimientoViewModel", "Trabajos de sincronización encolados tras guardar mantenimiento.")
    }


    fun onSubmissionSuccessHandled() {
        _uiState.update { it.copy(submissionSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class MantenimientoUiState(
    val machines: List<Machine> = emptyList(),
    val selectedMachine: Machine? = null,
    val selectedOil: Oil? = null,
    val quantity: String = "",
    val currentHourMeter: String = "",
    val averageHoursChange: String = "",
    val maintenanceType: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false,
    val motorOils: List<Oil> = emptyList(),
    val hydraulicOils: List<Oil> = emptyList(),
    val allOils: List<Oil> = emptyList()
)

sealed class MantenimientoFormEvent {
    data class MachineSelected(val machine: Machine) : MantenimientoFormEvent()
    data class OilSelected(val oil: Oil) : MantenimientoFormEvent()
    data class QuantityChanged(val quantity: String) : MantenimientoFormEvent()
    data class CurrentHourMeterChanged(val hourMeter: String) : MantenimientoFormEvent()
    data class AverageHoursChangeChanged(val hours: String) : MantenimientoFormEvent()
    data class MaintenanceTypeChanged(val type: String) : MantenimientoFormEvent()
    object Submit : MantenimientoFormEvent()
}
