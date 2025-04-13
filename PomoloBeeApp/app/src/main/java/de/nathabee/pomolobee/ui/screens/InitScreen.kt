package de.nathabee.pomolobee.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.ui.components.FolderPicker
import de.nathabee.pomolobee.util.ErrorLogger
import de.nathabee.pomolobee.util.PermissionManager
import de.nathabee.pomolobee.util.copyAssetsIfNotExists
import de.nathabee.pomolobee.viewmodel.StartupStatus

import de.nathabee.pomolobee.util.getFriendlyFolderName
import de.nathabee.pomolobee.viewmodel.InitViewModel
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitScreen(
    settingsViewModel: SettingsViewModel,
    orchardViewModel: OrchardViewModel,
    initViewModel: InitViewModel,
    onInitFinished: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }



     val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.all { it.value }
    }




    // Request permissions on start if needed
    LaunchedEffect(Unit) {
        Log.d("InitScreen", "🛂 Checking permissions...")
        if (!PermissionManager.allGranted(context)) {
            Log.d("InitScreen", "🟡 Permissions not granted — launching request")
            permissionLauncher.launch(PermissionManager.REQUIRED_PERMISSIONS)
        } else {
            Log.d("InitScreen", "✅ Permissions already granted")
            hasPermission = true
        }
    }


    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Requesting permissions...")
            }
        }
        return
    }

    // Now that permissions are granted...
    var showFolderPicker by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val initDone by initViewModel.initDone.collectAsState()

    val locations by orchardViewModel.locations.collectAsState()
    val cacheReady by remember { derivedStateOf { locations.isNotEmpty() } }



    val status by initViewModel.startupStatus.collectAsState()

    LaunchedEffect(hasPermission) {
        if (hasPermission && !initDone) {
            Log.d("InitScreen", "🧪 Bootstrapping initial startup status manually")
            val uri = storageRootUri
            val ready = locations.isNotEmpty()
            initViewModel.refreshStatus(context, uri, ready)
        }
    }




    suspend fun initialize(uri: Uri) {
        Log.d("InitScreen", "🚀 Initializing with URI = $uri")
        isLoading = true
        withContext(Dispatchers.IO) {
            settingsViewModel.setStorageRoot(uri)
            copyAssetsIfNotExists(context, uri)
            orchardViewModel.loadLocalConfig(uri, context)
            Log.d("InitScreen", "🧠 Orchard config loaded")
        }
        Log.d("InitScreen", "📦 Initial config load complete, now marking Ready")
        initViewModel.markInitDone() // ✅ explicitly after config load
        onInitFinished()
        isLoading = false


    }


    fun launchInitialize(uri: Uri) {
        coroutineScope.launch { initialize(uri) }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("\uD83D\uDC1D Setting up your PomoloBee workspace...")
            }
        }
        return
    }

        // Behavior based on status
        Log.d("InitScreen", "✅ Status calculated = ${status}")

    LaunchedEffect(status) {
        Log.d("InitScreen", "🔥 Triggered for status: $status")


        if (!initDone) {
            when (status) {
                StartupStatus.MissingUri -> showFolderPicker = true
                StartupStatus.InvalidUri -> showDialog = true

                StartupStatus.MissingConfig,
                StartupStatus.InitConfig -> {
                    storageRootUri?.let {
                        launchInitialize(it)
                    }
                }

                StartupStatus.Ready -> {
                    //initViewModel.markInitDone()
                    onInitFinished()
                }

                // 👇 catch-all fallback for unexpected status
                else -> {
                    Log.e("InitScreen", "⚠️ Unhandled startup status: $status")
                }
            }
        }
    }


    if (showFolderPicker) {
        FolderPicker(onFolderSelected = { selectedUri ->
            coroutineScope.launch {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                initialize(selectedUri)

            }
        })
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Storage Option") },
            text = { Text("No folder is selected. Use current or pick a new one?") },
            confirmButton = {
                TextButton(onClick = {
                    storageRootUri?.let {
                        launchInitialize(it)
                        showDialog = false
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
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Configure your storage:")

            Text("\uD83D\uDCC2 Current storage URI:", style = MaterialTheme.typography.labelSmall)
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
                    if (storageRootUri != null) {
                        launchInitialize(storageRootUri!!)
                    } else {
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
