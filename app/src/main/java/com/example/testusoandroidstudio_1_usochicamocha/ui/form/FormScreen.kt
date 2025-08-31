package com.example.testusoandroidstudio_1_usochicamocha.ui.form

import android.Manifest
import android.app.DatePickerDialog
import android.net.Uri
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: FormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- LÓGICA PARA SELECCIONAR IMÁGENES ---

    // 1. Launcher para la galería de fotos
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onImageSelected(it) }
        }
    )

    // 2. Launcher para la cámara
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { viewModel.onImageSelected(it) }
            }
        }
    )

    // 3. Launcher para pedir permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Si se concede el permiso, lanza la cámara
                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                tempImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                takePictureLauncher.launch(tempImageUri)
            }
        }
    )

    // Navega hacia atrás cuando el guardado se completa.
    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
            viewModel.onNavigationDone() // Resetea el estado para evitar re-navegación
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspección Maquinaria") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                if (newValue.all { it.isDigit() }) {
                                    viewModel.onHorometroChange(newValue)
                                }
                            },
                            label = { Text("Escriba el HOROMETRO Actual", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // --- Lista completa de todos los StatusSelector ---
            item { StatusSelector("Fugas en el Sistema", uiState.estadoFugas) { viewModel.onEstadoChange("Fugas", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema de Frenos", uiState.estadoFrenos) { viewModel.onEstadoChange("Frenos", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Estado de Correas y Poleas", uiState.estadoCorreasPoleas) { viewModel.onEstadoChange("CorreasPoleas", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Estado de Llantas y/o Carriles", uiState.estadoLlantasCarriles) { viewModel.onEstadoChange("LlantasCarriles", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema de Encendido", uiState.estadoEncendido) { viewModel.onEstadoChange("Encendido", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema Eléctrico en General", uiState.estadoElectrico) { viewModel.onEstadoChange("Electrico", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema Mecánico en General", uiState.estadoMecanico) { viewModel.onEstadoChange("Mecanico", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Temperatura", uiState.estadoTemperatura) { viewModel.onEstadoChange("Temperatura", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Aceite", uiState.estadoAceite) { viewModel.onEstadoChange("Aceite", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Hidraulico", uiState.estadoHidraulico) { viewModel.onEstadoChange("Hidraulico", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Refrigerante", uiState.estadoRefrigerante) { viewModel.onEstadoChange("Refrigerante", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Estado Estructural en General", uiState.estadoEstructural) { viewModel.onEstadoChange("Estructural", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                ExtinguisherDatePicker(
                    selectedDate = uiState.vigenciaExtintor,
                    onDateSelected = { year, month ->
                        viewModel.onExtinguisherDateChange(year, month)
                    }
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
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) } // Separador

            // Nueva sección de engrasado
            item {
                GreasingSection(
                    greasedStatus = uiState.greasingStatus, // Necesitarás añadir esto a tu UiState
                    onGreasedStatusChange = { viewModel.onGreasedStatusChange(it) }, // Y esto a tu ViewModel
                    greasingAction = uiState.greasingAction, // Necesitarás añadir esto a tu UiState
                    onGreasingActionChange = { viewModel.onGreasingActionChange(it) }, // Y esto a tu ViewModel
                    greasingObservations = uiState.greasingObservations, // Necesitarás añadir esto a tu UiState
                    onGreasingObservationsChange = { viewModel.onGreasingObservationsChange(it) } // Y esto a tu ViewModel
                )
            }
             item { Divider(modifier = Modifier.padding(vertical = 8.dp)) } // Separador


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
fun GreasingSection(
    greasedStatus: String, // "Sí", "No", "" (inicial)
    onGreasedStatusChange: (String) -> Unit,
    greasingAction: String, // "Total", "Parcial", "" (si greasedStatus es "No" o inicial)
    onGreasingActionChange: (String) -> Unit,
    greasingObservations: String,
    onGreasingObservationsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Engrasado", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        Text("¿Se engrasó la máquina?", fontSize = 16.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Sí", "No").forEach { option ->
                val isSelected = option == greasedStatus
                OutlinedButton(
                    onClick = { onGreasedStatusChange(option) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Text(option)
                }
            }
        }

        if (greasedStatus == "Sí") {
            Spacer(Modifier.height(8.dp))
            Text("Tipo de engrasado:", fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Total", "Parcial").forEach { option ->
                    val isSelected = option == greasingAction
                    val backgroundColor = when (option) {
                        "Total" -> Color(0xFF4CAF50) // Verde
                        "Parcial" -> Color(0xFFFFA000) // Naranja
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
                            .clickable { onGreasingActionChange(option) },
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
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = greasingObservations,
                onValueChange = onGreasingObservationsChange,
                label = { Text("Observaciones del Engrasado", fontWeight = FontWeight.Normal, fontSize = 16.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineSelector(
    machines: List<Machine>,
    selectedMachine: Machine?,
    onMachineSelected: (Machine) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedMachine?.let { "${it.name} - ${it.model} - ${it.internalIdentificationNumber}" } ?: "Seleccione una máquina"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Máquina", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            machines.forEach { machine ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = machine.name, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Model: ${machine.model} - ID: ${machine.internalIdentificationNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                    },
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
fun StatusSelector(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val options = listOf("Óptimo", "Regular", "Malo")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val backgroundColor = when (option) {
                    "Óptimo" -> Color(0xFF4CAF50) // Verde
                    "Regular" -> Color(0xFFFFA000) // Naranja
                    "Malo" -> Color(0xFFD32F2F) // Rojo
                    else -> Color.Gray
                }
                // Color de texto blanco para los fondos de color, negro/gris oscuro para el fondo claro no seleccionado
                val textColor = if (isSelected) Color.White else Color.Black

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) backgroundColor else Color.LightGray, // Borde más visible para no seleccionados
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.2f) // Fondo más claro para no seleccionados
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, // Más peso para no seleccionados
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExtinguisherDatePicker(
    selectedDate: String,
    onDateSelected: (year: Int, month: Int) -> Unit
) {
    val calendar = Calendar.getInstance()

    // Parsea la fecha actual o usa la de hoy como respaldo
    val (initialYear, initialMonth) = remember(selectedDate) {
        if (selectedDate.contains("-")) {
            val parts = selectedDate.split("-")
            Pair(
                parts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (parts[1].toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH)
            )
        } else {
            Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        MonthYearPickerDialog(
            onDismissRequest = { showDialog = false },
            onDateSelected = { year, month ->
                onDateSelected(year, month)
                showDialog = false
            },
            initialYear = initialYear,
            initialMonth = initialMonth
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Vigencia EXTINTOR", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { /* No-op, la fecha se selecciona a través del diálogo */ },
            readOnly = true,
            label = { Text("Fecha de Vencimiento (YYYY-MM)", fontWeight = FontWeight.Normal, fontSize = 16.sp) },
            trailingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Fecha")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true } // Hace que todo el campo sea clickeable
        )
    }
}

@Composable
fun MonthYearPickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int) -> Unit,
    initialYear: Int,
    initialMonth: Int
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    val months = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )

    Dialog(onDismissRequest = onDismissRequest) {
        // Tarjeta más ancha y con esquinas más redondeadas
        Card(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                // Más padding interno para dar más espacio
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de Año
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Año anterior")
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Año siguiente")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Grilla para seleccionar el Mes (3 columnas, 4 filas)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // CAMBIO: 3 columnas
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(months) { index, month ->
                        val isSelected = (selectedYear == initialYear && index == initialMonth)
                        OutlinedButton(
                            onClick = {
                                onDateSelected(selectedYear, index) // El mes es el índice (0-11)
                                onDismissRequest()
                            },
                            // Botones con altura fija para consistencia
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (isSelected) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(text = month, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageUploadSection(
    selectedImageUris: List<Uri>,
    onPickImageClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemoveImageClick: (Uri) -> Unit,
    onViewImageClick: (Uri) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Subir imágenes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) // Título en negrita

            if (selectedImageUris.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        SelectedImageItem(
                            uri = uri,
                            onRemoveClick = { onRemoveImageClick(uri) },
                            onViewClick = { onViewImageClick(uri) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onPickImageClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Subir foto")
                }
                OutlinedButton(
                    onClick = onTakePhotoClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Tomar foto")
                }
            }
        }
    }
}

@Composable
fun SelectedImageItem(
    uri: Uri,
    onRemoveClick: () -> Unit,
    onViewClick: () -> Unit
) {
    val fileName = uri.lastPathSegment ?: "imagen.jpg"

    ListItem(
        headlineContent = { Text(fileName, maxLines = 1) },
        leadingContent = {
            IconButton(onClick = onViewClick) {
                Icon(Icons.Default.Visibility, contentDescription = "Ver imagen")
            }
        },
        trailingContent = {
            TextButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun ImagePreviewDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Previsualización",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) // Título en negrita
                )
                Spacer(Modifier.height(8.dp))

                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen previsualizada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Volver")
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
