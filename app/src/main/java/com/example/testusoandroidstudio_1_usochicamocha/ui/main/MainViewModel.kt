package com.example.testusoandroidstudio_1_usochicamocha.ui.main

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PendingFormStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
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
    private val getPendingFormsWithStatusUseCase: GetPendingFormsWithStatusUseCase,
    private val getPendingMaintenanceFormsUseCase: GetPendingMaintenanceFormsUseCase,
    private val localSyncCoordinator: LocalSyncCoordinator, // Inyectamos el coordinador
    private val triggerImageSyncUseCase: TriggerImageSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observePendingForms()
        observePendingMaintenanceForms()
        observeSyncStatuses()
    }

    private fun observePendingForms() {
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
     * Observa los estados de sincronización de manera independiente
     */
    private fun observeSyncStatuses() {
        // Machines
        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MACHINES_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingMachines = isRunning) }
        }.launchIn(viewModelScope)

        // Oils
        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.OILS_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingOils = isRunning) }
        }.launchIn(viewModelScope)

        // Forms
        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.FORMS_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingForms = isRunning) }
        }.launchIn(viewModelScope)

        // Maintenance
        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MAINTENANCE_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingMaintenance = isRunning) }
        }.launchIn(viewModelScope)
    }

    fun onSyncImagesClicked() {
        viewModelScope.launch {
            // Usamos el coordinador para imágenes también si es posible, 
            // o mantenemos el caso de uso directo si es específico para imágenes
            // Por ahora mantenemos el trigger directo pero idealmente debería ir al coordinador
            triggerImageSyncUseCase()
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

    // --- NUEVA LÓGICA USANDO COORDINATOR ---

    fun onSyncMachinesClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncMachinesMessage = null) }
            
            Log.d("MainViewModel", "🔄 Starting machines sync")
            
            // Solicitamos sincronización de datos maestros (solo máquinas)
            val result = localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MACHINES_ONLY)
            )
            
            if (result.isSuccess) {
                _uiState.update { it.copy(syncMachinesMessage = "Sincronización de máquinas iniciada en segundo plano") }
                Log.d("MainViewModel", "✅ Machines sync coordinated successfully")
            } else {
                _uiState.update { it.copy(syncMachinesMessage = "No se pudo iniciar la sincronización") }
                Log.e("MainViewModel", "❌ Failed to coordinate machines sync", result.exceptionOrNull())
            }
        }
    }

    fun clearSyncMachinesMessage() {
        _uiState.update { it.copy(syncMachinesMessage = null) }
    }

    fun onSyncFormsClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncFormsMessage = null) }
            
            val pendingTextForms = _uiState.value.pendingForms.filter { !it.form.isSynced }
            if (pendingTextForms.isEmpty()) {
                _uiState.update { it.copy(syncFormsMessage = "No hay inspecciones para sincronizar.") }
                return@launch
            }

            Log.d("MainViewModel", "🔄 Starting forms sync with ${pendingTextForms.size} pending forms")
            
            // Solicitamos sincronización de formularios
            val result = localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.FORMS_ONLY)
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(syncFormsMessage = "Sincronización de formularios iniciada en segundo plano") }
                Log.d("MainViewModel", "✅ Forms sync coordinated successfully")
            } else {
                _uiState.update { it.copy(syncFormsMessage = "Error al iniciar sincronización: ${result.exceptionOrNull()?.message}") }
                Log.e("MainViewModel", "❌ Failed to coordinate forms sync", result.exceptionOrNull())
            }
        }
    }

    fun clearSyncFormsMessage() {
        _uiState.update { it.copy(syncFormsMessage = null) }
    }

    fun onSyncMaintenanceClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncMaintenanceMessage = null) }

            val pendingMaintenance = _uiState.value.pendingMaintenanceForms
            if (pendingMaintenance.isEmpty()) {
                _uiState.update { it.copy(syncMaintenanceMessage = "No hay mantenimientos para sincronizar.") }
                return@launch
            }
            
            Log.d("MainViewModel", "🔄 Starting maintenance sync with ${pendingMaintenance.size} pending forms")

            // Solicitamos sincronización de mantenimientos
            val result = localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MAINTENANCE_ONLY)
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(syncMaintenanceMessage = "Sincronización de mantenimientos iniciada en segundo plano") }
                Log.d("MainViewModel", "✅ Maintenance sync coordinated successfully")
            } else {
                _uiState.update { it.copy(syncMaintenanceMessage = "Error al iniciar sincronización") }
                Log.e("MainViewModel", "❌ Failed to coordinate maintenance sync", result.exceptionOrNull())
            }
        }
    }

    fun clearSyncMaintenanceMessage() {
        _uiState.update { it.copy(syncMaintenanceMessage = null) }
    }

    fun onSyncOilsClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncOilsMessage = null) }
            
            Log.d("MainViewModel", "🔄 Starting oils sync")
            
            // Aceites son parte de Master Data (solo aceites)
            val result = localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.OILS_ONLY)
            )
            
            if (result.isSuccess) {
                _uiState.update { it.copy(syncOilsMessage = "Sincronización de aceites iniciada en segundo plano") }
                Log.d("MainViewModel", "✅ Oils sync coordinated successfully")
            } else {
                _uiState.update { it.copy(syncOilsMessage = "Error al iniciar sincronización") }
                Log.e("MainViewModel", "❌ Failed to coordinate oils sync", result.exceptionOrNull())
            }
        }
    }

    fun clearSyncOilsMessage() {
        _uiState.update { it.copy(syncOilsMessage = null) }
    }
}

