package com.example.testusoandroidstudio_1_usochicamocha.ui.mantenimiento

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.ui.theme.AppUsoChicamochaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoScreen(
    viewModel: MantenimientoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efecto para navegar hacia atrás cuando el formulario se guarda con éxito
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            Toast.makeText(context, "Cambio aceite guardado localmente.", Toast.LENGTH_SHORT).show()
            viewModel.onSubmissionSuccessHandled() // Resetea el flag
            onNavigateBack()
        }
    }

    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError() // Limpia el error después de mostrarlo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de cambio aceite") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Usamos LazyColumn para un mejor rendimiento y para evitar overflows de pantalla
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MachineSelectorMantenimiento(
                    machines = uiState.machines,
                    selectedMachine = uiState.selectedMachine,
                    onMachineSelected = { viewModel.onFormEvent(MantenimientoFormEvent.MachineSelected(it)) },
                    isEnabled = !uiState.isLoading
                )
            }

            item {
                MaintenanceTypeSelector(
                    selectedType = uiState.maintenanceType,
                    onTypeSelected = { viewModel.onFormEvent(MantenimientoFormEvent.MaintenanceTypeChanged(it)) },
                    isEnabled = !uiState.isLoading
                )
            }

            item {
                MaintenanceDetailsCard(
                    uiState = uiState,
                    onFormEvent = viewModel::onFormEvent, // Pasamos la referencia a la función
                    isEnabled = !uiState.isLoading
                )
            }

            item {
                Button(
                    onClick = { viewModel.onFormEvent(MantenimientoFormEvent.Submit) },
                    enabled = uiState.selectedMachine != null &&
                            uiState.maintenanceType != null &&
                            uiState.selectedOil != null &&
                            !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("GUARDAR", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineSelectorMantenimiento(
    machines: List<Machine>,
    selectedMachine: Machine?,
    onMachineSelected: (Machine) -> Unit,
    isEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedMachine?.let { "${it.name} - ${it.model} - ${it.internalIdentificationNumber}" } ?: "Seleccione una máquina"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (isEnabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Máquina") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            machines.forEach { machine ->
                DropdownMenuItem(
                    text = { Text("${machine.name} - ${machine.model} - ${machine.internalIdentificationNumber}") },
                    onClick = {
                        onMachineSelected(machine)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MaintenanceTypeSelector(
    selectedType: String?,
    onTypeSelected: (String) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("TIPO DE CAMBIO:", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val motorButtonColors = if (selectedType == "motor") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                OutlinedButton(
                    onClick = { onTypeSelected("motor") },
                    modifier = Modifier.weight(1f),
                    colors = motorButtonColors,
                    enabled = isEnabled
                ) {
                    Text("ACEITE MOTOR", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))

                val hydraulicButtonColors = if (selectedType == "hydraulic") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                OutlinedButton(
                    onClick = { onTypeSelected("hydraulic") },
                    modifier = Modifier.weight(1f),
                    colors = hydraulicButtonColors,
                    enabled = isEnabled
                ) {
                    Text("HIDRÁULICO", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun MaintenanceDetailsCard(
    uiState: MantenimientoUiState,
    onFormEvent: (MantenimientoFormEvent) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Este campo NO cambia
            OutlinedTextField(
                value = uiState.currentHourMeter,
                onValueChange = { onFormEvent(MantenimientoFormEvent.CurrentHourMeterChanged(it)) },
                label = { Text("Horómetro actual") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = isEnabled
            )

            // --- INICIO DE LA MODIFICACIÓN ---

            // 1. Obtenemos la lista de aceites como antes.
            val availableOils = when (uiState.maintenanceType) {
                "motor" -> uiState.motorOils
                "hydraulic" -> uiState.hydraulicOils
                else -> emptyList()
            }



                OilSelector(
                    oils = availableOils,
                    selectedOil = uiState.selectedOil, // <-- PASA EL OBJETO "selectedOil"
                    onOilSelected = { oil -> // <-- RECIBE EL OBJETO "oil"
                        onFormEvent(MantenimientoFormEvent.OilSelected(oil)) // <-- ENVÍA EL NUEVO EVENTO
                    },
                    isEnabled = isEnabled && uiState.maintenanceType != null
                )


            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = { onFormEvent(MantenimientoFormEvent.QuantityChanged(it)) },
                label = { Text("Cantidad (Gl)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = isEnabled
            )

            // Este campo NO cambia
            OutlinedTextField(
                value = uiState.averageHoursChange,
                onValueChange = { onFormEvent(MantenimientoFormEvent.AverageHoursChangeChanged(it)) },
                label = { Text("Horas para siguiente cambio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = isEnabled
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMantenimientoScreen() {
    AppUsoChicamochaTheme {
        MantenimientoScreen(onNavigateBack = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OilSelector(
    oils: List<Oil>,
    selectedOil: Oil?,
    onOilSelected: (Oil) -> Unit,
    isEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedOil?.name ?: "Seleccione un aceite"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (isEnabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Marca del Aceite") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            oils.forEach { oil ->
                DropdownMenuItem(
                    text = { Text(oil.name) },
                    onClick = {
                        onOilSelected(oil) // <-- DEVUELVE EL OBJETO "oil" COMPLETO
                        expanded = false
                    }
                )
            }
        }
    }
}
