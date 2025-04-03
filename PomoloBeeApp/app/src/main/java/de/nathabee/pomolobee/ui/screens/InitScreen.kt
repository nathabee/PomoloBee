package de.nathabee.pomolobee.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.nathabee.pomolobee.ui.components.FolderPicker
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import de.nathabee.pomolobee.util.copyAssetsIfNotExists

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitScreen(onSetupComplete: (Uri) -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val coroutineScope = rememberCoroutineScope()

    val storageRootUri by viewModel.storageRootUri.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }

    if (showFolderPicker) {
        if (showFolderPicker) {
            FolderPicker(onFolderSelected = { selectedUri ->
                viewModel.setStorageRoot(selectedUri)
                onSetupComplete(selectedUri)
            })


        }

    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Storage Option") },
            text = { Text("No folder is selected. Use current or pick a new one?") },
            confirmButton = {
                TextButton(onClick = {
                    storageRootUri?.let {
                        coroutineScope.launch {
                            viewModel.setStorageRoot(it)
                            showDialog = false
                            onSetupComplete(it)
                        }
                    }
                }) {
                    Text("Use Existing")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    showFolderPicker = true
                }) {
                    Text("Select Folder")
                }
            }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Welcome to PomoloBee") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Configure your storage:")

            Text("📂 Current storage URI:", style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(
                value = storageRootUri?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { showFolderPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Folder")
            }

            Button(
                onClick = {
                    if (storageRootUri == null) {
                        showDialog = true
                    } else {
                        onSetupComplete(storageRootUri!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save and Continue")
            }
        }
    }
}
