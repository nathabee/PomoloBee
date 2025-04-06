package de.nathabee.pomolobee.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.ui.components.FolderPicker
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.util.copyAssetsIfNotExists
import de.nathabee.pomolobee.viewmodel.OrchardViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitScreen(
    settingsViewModel: SettingsViewModel,
    orchardViewModel: OrchardViewModel,
    onInitFinished: (Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }

    FolderPicker(onFolderSelected = { selectedUri ->
        coroutineScope.launch {
            // 🛡 Take access permission!
            context.contentResolver.takePersistableUriPermission(
                selectedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            settingsViewModel.setStorageRoot(selectedUri)
            copyAssetsIfNotExists(context, selectedUri)
            orchardViewModel.loadLocalConfig(selectedUri, context)
            onInitFinished(selectedUri)
        }
    })


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Storage Option") },
            text = { Text("No folder is selected. Use current or pick a new one?") },
            confirmButton = {
                TextButton(onClick = {
                    storageRootUri?.let {
                        coroutineScope.launch {
                            settingsViewModel.setStorageRoot(it)
                            showDialog = false
                            onInitFinished(it)
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
                    storageRootUri?.let { nonNullUri ->
                        onInitFinished(nonNullUri) // nonNullUri is Uri (not nullable here)
                    } ?: run {
                        showDialog = true
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save and Continue")
            }
        }
    }
}
