package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
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
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.viewmodel.StartupStatus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitScreen(
    sharedViewModels: PomolobeeViewModels,
    onInitFinished: () -> Unit
) {


    //#########################################################################
    // VARIABLE
    //#########################################################################

    val context = LocalContext.current
    val settingsViewModel = sharedViewModels.settings
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val status by settingsViewModel.startupStatus.collectAsState()
    val initDone by settingsViewModel.initDone.collectAsState()
    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.all { it.value }
    }

    var hasHandledInitialStatus by remember { mutableStateOf(false) }
    //#########################################################################
    // FUNCTION
    //#########################################################################

    suspend fun initialize(uri: Uri) {
        Log.d("InitScreen", "🚀 Initializing with URI = $uri")
        isLoading = true
        withContext(Dispatchers.IO) {
            settingsViewModel.setStorageRoot(uri)
            StorageUtils.copyAssetsIfNotExists(context, uri)

            val orchardSuccess = orchardViewModel.loadConfigFromStorage(uri, context)
            val imageSuccess = imageViewModel.loadImageCacheFromStorage(uri)

            Log.d("InitScreen", "🧠 Orchard: $orchardSuccess | 📸 Images: $imageSuccess")
        }
        Log.d("InitScreen", "✅ Init complete, marking as done")
        settingsViewModel.markInitDone()
        onInitFinished()
        isLoading = false
    }

    fun launchInitialize(uri: Uri) {
        coroutineScope.launch { initialize(uri) }
    }

    //#########################################################################
    // Launch effect
    //#########################################################################


    // 🧠 All initialization logic is sequenced inside one block
    LaunchedEffect(Unit) {
        Log.d("InitScreen", "🚀 Init sequence started...")

        // 1. Request permissions
        if (!PermissionManager.allGranted(context)) {
            Log.d("InitScreen", "🟡 Requesting permissions...")
            permissionLauncher.launch(PermissionManager.REQUIRED_PERMISSIONS)
            return@LaunchedEffect
        } else {
            Log.d("InitScreen", "✅ Permissions already granted")
            hasPermission = true
        }

        // 2. Load storage URI from prefs
        val startupUri = settingsViewModel.getStartupStorageUri(context)
        Log.d("InitScreen", "📂 Startup URI from prefs: $startupUri")

        if (startupUri != null) {
            settingsViewModel.setStorageRoot(startupUri)
            val isCacheReady = orchardViewModel.locations.value.isNotEmpty()
            settingsViewModel.updateStartupStatus(isCacheReady)
        } else {
            settingsViewModel.updateStartupStatus(isCacheReady = false)
        }
    }

    // 🔥 React to the computed status


    LaunchedEffect(status) {
        if (!initDone && !hasHandledInitialStatus) {
            Log.d("InitScreen", "🔥 Handling status = $status")
            hasHandledInitialStatus = true

            when (status) {
                StartupStatus.MissingUri -> showFolderPicker = true
                StartupStatus.InvalidUri -> showDialog = true
                StartupStatus.MissingConfig,
                StartupStatus.InitConfig -> storageRootUri?.let { launchInitialize(it) }
                StartupStatus.Ready -> onInitFinished()
                else -> Log.w("InitScreen", "⚠️ Unknown status: $status")
            }
        }
    }


    //#########################################################################
    // DISPLAY
    //#########################################################################


    // 🖼️ UI display logic below
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

    if (showFolderPicker) {
        FolderPicker { selectedUri ->
            coroutineScope.launch {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                initialize(selectedUri)
            }
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
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
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
                    storageRootUri?.let { launchInitialize(it) } ?: run {
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