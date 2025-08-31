package com.example.testusoandroidstudio_1_usochicamocha.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.theme.AppUsoChicamochaTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    networkStatus: Boolean,
    viewModel: MainViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToForm: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToImprevisto: () -> Unit,
    onNavigateToMantenimiento: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            onLogout()
            viewModel.onLogoutCompleted()
        }
    }

    LaunchedEffect(uiState.syncMachinesMessage) {
        uiState.syncMachinesMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncMachinesMessage()
        }
    }

    LaunchedEffect(uiState.syncFormsMessage) {
        uiState.syncFormsMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncFormsMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Menú Principal") },
                    actions = {
                        IconButton(onClick = onNavigateToLogs) {
                            Icon(Icons.Filled.History, contentDescription = "Ver Logs")
                        }
                        IconButton(onClick = { viewModel.onLogoutClick() }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvailableFormsCard(onNavigateToForm = onNavigateToForm,
                onNavigateToImprevisto = onNavigateToImprevisto,
                onNavigateToMantenimiento = onNavigateToMantenimiento)

            PendingFormsCard(
                pendingForms = uiState.pendingForms,
                isSyncingMachines = uiState.isSyncingMachines,
                isSyncingForms = uiState.isSyncingForms,
                onSyncMachinesClicked = { viewModel.onSyncMachinesClicked() },
                onSyncFormsClicked = { viewModel.onSyncFormsClicked() } // <-- Conectamos la nueva función
            )
        }
        if (uiState.showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = { viewModel.onConfirmLogout() },
                onDismiss = { viewModel.onDismissLogoutDialog() }
            )
        }
    }
}

@Composable
fun AvailableFormsCard(
    onNavigateToForm: () -> Unit,
    onNavigateToImprevisto: () -> Unit,
    onNavigateToMantenimiento: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Formularios disponibles", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onNavigateToForm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Inspección maquinas", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavigateToImprevisto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Imprevisto maquinas", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavigateToMantenimiento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mantenimiento", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PendingFormsCard(
    pendingForms: List<Form>,
    isSyncingMachines: Boolean,
    isSyncingForms: Boolean,
    onSyncMachinesClicked: () -> Unit,
    onSyncFormsClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Formularios pendientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                // --- BOTÓN PARA SINCRONIZAR FORMULARIOS ---
                Button(
                    onClick = onSyncFormsClicked,
                    enabled = !isSyncingForms,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (isSyncingForms) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar Formularios")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // --- BOTÓN PARA SINCRONIZAR MÁQUINAS ---
                Button(
                    onClick = onSyncMachinesClicked,
                    enabled = !isSyncingMachines,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (isSyncingMachines) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(text = "🔁🚜")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (pendingForms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay formularios pendientes.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pendingForms) { form ->
                        PendingFormItem(form = form)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingFormItem(form: Form) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(form.timestamp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Formulario", fontWeight = FontWeight.Bold)
        Text("Creado ${formattedDate}")
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Sesión") },
        text = { Text("¿Seguro desea salir? Esto cerrará la sesión actual.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Salir")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppUsoChicamochaTheme {
        MainScreen(
            networkStatus = true,
            onLogout = {},
            onNavigateToForm = {},
            onNavigateToLogs = {},
            onNavigateToImprevisto = {},
            onNavigateToMantenimiento = {}
        )
    }
}
