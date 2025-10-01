package com.example.testusoandroidstudio_1_usochicamocha.ui.main

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PendingFormStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.LogoutUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.GetPendingFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.GetPendingFormsWithStatusUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.TriggerImageSyncUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.GetPendingMaintenanceFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.SyncMaintenanceFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val showLogoutDialog: Boolean = false,
    val logoutCompleted: Boolean = false,
    val pendingForms: List<PendingFormStatus> = emptyList(), // Modificado
    val isSyncingMachines: Boolean = false,
    val syncMachinesMessage: String? = null,
    val isSyncingForms: Boolean = false,
    val syncFormsMessage: String? = null,
    val pendingMaintenanceForms: List<Maintenance> = emptyList(),
    val isSyncingMaintenance: Boolean = false,
    val syncMaintenanceMessage: String? = null,
    val isSyncingOils: Boolean = false,
    val syncOilsMessage: String? = null,
    // AÑADIDO: Estado para controlar el nuevo botón de sincronización de imágenes
    val syncImagesMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val getPendingFormsWithStatusUseCase: GetPendingFormsWithStatusUseCase, // Modificado
    private val syncMachinesUseCase: SyncMachinesUseCase,
    private val syncFormUseCase: SyncFormUseCase,
    private val getPendingMaintenanceFormsUseCase: GetPendingMaintenanceFormsUseCase,
    private val syncMaintenanceFormsUseCase: SyncMaintenanceFormsUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val triggerImageSyncUseCase: TriggerImageSyncUseCase // Añadido
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observePendingForms()
        observePendingMaintenanceForms()
    }

    private fun observePendingForms() {
        // Modificado para usar el nuevo UseCase que trae el estado completo
        getPendingFormsWithStatusUseCase().onEach { formsWithStatus ->
            _uiState.update { it.copy(pendingForms = formsWithStatus) }
        }.launchIn(viewModelScope)
    }

    private fun observePendingMaintenanceForms() {
        getPendingMaintenanceFormsUseCase().onEach { maintenanceForms ->
            _uiState.update { it.copy(pendingMaintenanceForms = maintenanceForms) }
        }.launchIn(viewModelScope)
    }

    /**
     * AÑADIDO: Nueva función que se llamará desde la UI
     * para forzar la sincronización de imágenes.
     */
    fun onSyncImagesClicked() {
        viewModelScope.launch {
            triggerImageSyncUseCase()
            // Mostramos un mensaje temporal al usuario para darle feedback
            _uiState.update { it.copy(syncImagesMessage = "Sincronización de imágenes iniciada en segundo plano.") }
        }
    }

    fun clearSyncImagesMessage() {
        _uiState.update { it.copy(syncImagesMessage = null) }
    }


    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onConfirmLogout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(showLogoutDialog = false, logoutCompleted = true) }
        }
    }

    fun onLogoutCompleted() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }

    fun onSyncMachinesClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingMachines = true, syncMachinesMessage = null) }
            val result = syncMachinesUseCase()
            val message = if (result.isSuccess) "Máquinas sincronizadas con éxito" else result.exceptionOrNull()?.message ?: "Error desconocido"
            _uiState.update { it.copy(isSyncingMachines = false, syncMachinesMessage = message) }
        }
    }

    fun clearSyncMachinesMessage() {
        _uiState.update { it.copy(syncMachinesMessage = null) }
    }

    fun onSyncFormsClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingForms = true, syncFormsMessage = null) }

            // Obtenemos solo los formularios de texto que no están sincronizados
            val pendingTextForms = _uiState.value.pendingForms
                .filter { !it.form.isSynced }
                .map { it.form }

            if (pendingTextForms.isEmpty()) {
                _uiState.update { it.copy(isSyncingForms = false, syncFormsMessage = "No hay inspecciones para sincronizar.") }
                return@launch
            }

            var successCount = 0
            var errorCount = 0

            pendingTextForms.forEach { form ->
                val result = syncFormUseCase(form)
                if (result.isSuccess) successCount++ else errorCount++
            }

            val message = "Sincronización de inspecciones completa. Éxitos: $successCount, Fallos: $errorCount."
            _uiState.update { it.copy(isSyncingForms = false, syncFormsMessage = message) }
        }
    }

    fun clearSyncFormsMessage() {
        _uiState.update { it.copy(syncFormsMessage = null) }
    }

    fun onSyncMaintenanceClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingMaintenance = true, syncMaintenanceMessage = null) }

            val pendingMaintenance = _uiState.value.pendingMaintenanceForms
            if (pendingMaintenance.isEmpty()) {
                _uiState.update { it.copy(isSyncingMaintenance = false, syncMaintenanceMessage = "No hay mantenimientos para sincronizar.") }
                return@launch
            }

            var successCount = 0
            var errorCount = 0

            pendingMaintenance.forEach { form ->
                val result = syncMaintenanceFormsUseCase(form)
                if (result.isSuccess) successCount++ else errorCount++
            }

            val message = "Sincronización de mantenimientos completa. Éxitos: $successCount, Fallos: $errorCount."
            _uiState.update { it.copy(isSyncingMaintenance = false, syncMaintenanceMessage = message) }
        }
    }

    fun clearSyncMaintenanceMessage() {
        _uiState.update { it.copy(syncMaintenanceMessage = null) }
    }

    fun onSyncOilsClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingOils = true, syncOilsMessage = null) }
            val result = syncOilsUseCase()
            val message = if (result.isSuccess) "Aceites sincronizados" else result.exceptionOrNull()?.message ?: "Error"
            _uiState.update { it.copy(isSyncingOils = false, syncOilsMessage = message) }
        }
    }

    fun clearSyncOilsMessage() {
        _uiState.update { it.copy(syncOilsMessage = null) }
    }
}

