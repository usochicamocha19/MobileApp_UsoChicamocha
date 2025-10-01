package com.example.testusoandroidstudio_1_usochicamocha.ui.imprevisto

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.ui.form.* // Importamos los componentes reutilizables
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprevistoScreen(
    viewModel: ImprevistoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // (El código de los launchers para imágenes permanece igual)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let { viewModel.onImageSelected(it) } }
    )
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) { tempImageUri?.let { viewModel.onImageSelected(it) } } }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                // CORRECCIÓN: Creamos una variable local que no es nula
                val newUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                // Asignamos el valor a la variable de estado
                tempImageUri = newUri
                // Y lanzamos el launcher con la variable local segura
                takePictureLauncher.launch(newUri)
            }
        }
    )

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
            viewModel.onNavigationDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspección Imprevista") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            // --- MODIFICADO: Reducimos el espaciado general ---
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MachineSelector(
                    machines = uiState.machines,
                    selectedMachine = uiState.selectedMachine,
                    onMachineSelected = { viewModel.onMachineSelected(it) }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                OutlinedTextField(
                    value = uiState.horometro,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) { viewModel.onHorometroChange(newValue) }
                    },
                    label = { Text("Escriba el HOROMETRO Actual", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // --- Lista de StatusSelector con Checkbox ---
            val dividerModifier = Modifier.padding(vertical = 4.dp) // --- MODIFICADO: Modificador para los divisores internos

            item { StatusSelectorConCheckbox("Fugas en el Sistema", uiState.estadoFugas, uiState.estadoFugasChecked, { viewModel.onEstadoChange("Fugas", it) }, { viewModel.onEstadoCheckedChange("Fugas", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Sistema de Frenos", uiState.estadoFrenos, uiState.estadoFrenosChecked, { viewModel.onEstadoChange("Frenos", it) }, { viewModel.onEstadoCheckedChange("Frenos", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Estado de Correas y Poleas", uiState.estadoCorreasPoleas, uiState.estadoCorreasPoleasChecked, { viewModel.onEstadoChange("CorreasPoleas", it) }, { viewModel.onEstadoCheckedChange("CorreasPoleas", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Estado de Llantas y/o Carriles", uiState.estadoLlantasCarriles, uiState.estadoLlantasCarrilesChecked, { viewModel.onEstadoChange("LlantasCarriles", it) }, { viewModel.onEstadoCheckedChange("LlantasCarriles", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Sistema de Encendido", uiState.estadoEncendido, uiState.estadoEncendidoChecked, { viewModel.onEstadoChange("Encendido", it) }, { viewModel.onEstadoCheckedChange("Encendido", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Sistema Eléctrico en General", uiState.estadoElectrico, uiState.estadoElectricoChecked, { viewModel.onEstadoChange("Electrico", it) }, { viewModel.onEstadoCheckedChange("Electrico", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Sistema Mecánico en General", uiState.estadoMecanico, uiState.estadoMecanicoChecked, { viewModel.onEstadoChange("Mecanico", it) }, { viewModel.onEstadoCheckedChange("Mecanico", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Nivel de Temperatura", uiState.estadoTemperatura, uiState.estadoTemperaturaChecked, { viewModel.onEstadoChange("Temperatura", it) }, { viewModel.onEstadoCheckedChange("Temperatura", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Nivel de Aceite", uiState.estadoAceite, uiState.estadoAceiteChecked, { viewModel.onEstadoChange("Aceite", it) }, { viewModel.onEstadoCheckedChange("Aceite", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Nivel de Hidraulico", uiState.estadoHidraululico, uiState.estadoHidraulicoChecked, { viewModel.onEstadoChange("Hidraulico", it) }, { viewModel.onEstadoCheckedChange("Hidraulico", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Nivel de Refrigerante", uiState.estadoRefrigerante, uiState.estadoRefrigeranteChecked, { viewModel.onEstadoChange("Refrigerante", it) }, { viewModel.onEstadoCheckedChange("Refrigerante", it) }) }
            item { Divider(modifier = dividerModifier) }
            item { StatusSelectorConCheckbox("Estado Estructural en General", uiState.estadoEstructural, uiState.estadoEstructuralChecked, { viewModel.onEstadoChange("Estructural", it) }, { viewModel.onEstadoCheckedChange("Estructural", it) }) }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                ExtinguisherDatePicker(
                    selectedDate = uiState.vigenciaExtintor,
                    onDateSelected = { year, month -> viewModel.onExtinguisherDateChange(year, month) }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                OutlinedTextField(
                    value = uiState.observaciones,
                    onValueChange = { viewModel.onObservacionesChange(it) },
                    label = { Text("Observaciones y/o Aspectos a Revisar", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // --- AÑADIDO: La sección de engrasado ---
            item {
                GreasingSection(
                    greasedStatus = uiState.greasingStatus,
                    onGreasedStatusChange = { viewModel.onGreasedStatusChange(it) },
                    greasingAction = uiState.greasingAction,
                    onGreasingActionChange = { viewModel.onGreasingActionChange(it) },
                    greasingObservations = uiState.greasingObservations,
                    onGreasingObservationsChange = { viewModel.onGreasingObservationsChange(it) }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }


            item {
                ImageUploadSection(
                    selectedImageUris = uiState.selectedImageUris,
                    onPickImageClick = { pickImageLauncher.launch("image/*") },
                    onTakePhotoClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onRemoveImageClick = { viewModel.onImageRemoved(it) },
                    onViewImageClick = { viewModel.onPreviewImageClicked(it) }
                )
            }

            item {
                Button(
                    onClick = { viewModel.onSaveClick() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedMachine != null
                ) {
                    Text("Guardar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (uiState.previewingImageUri != null) {
        ImagePreviewDialog(
            uri = uiState.previewingImageUri!!,
            onDismiss = { viewModel.onDismissPreview() },
            onDelete = { viewModel.onImageRemoved(uiState.previewingImageUri!!) }
        )
    }
}


@Composable
fun StatusSelectorConCheckbox(
    label: String,
    selectedOption: String,
    isChecked: Boolean,
    onOptionSelected: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val options = listOf("Óptimo", "Regular", "Malo")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
            Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        }

        if (isChecked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    val backgroundColor = when (option) {
                        "Óptimo" -> Color(0xFF4CAF50)
                        "Regular" -> Color(0xFFFFA000)
                        "Malo" -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    }
                    val textColor = if (isSelected) Color.White else Color.Black

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) backgroundColor else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onOptionSelected(option) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = option,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
