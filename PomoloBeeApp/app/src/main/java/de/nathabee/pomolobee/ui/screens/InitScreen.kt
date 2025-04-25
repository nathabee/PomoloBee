package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import de.nathabee.pomolobee.util.PermissionManager
import de.nathabee.pomolobee.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import de.nathabee.pomolobee.viewmodel.StartupStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitScreen(
    sharedViewModels: PomolobeeViewModels,
    onInitFinished: () -> Unit
) {
    val context = LocalContext.current
    val settingsViewModel = sharedViewModels.settings
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val coroutineScope = rememberCoroutineScope()

    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    val startupStatus by settingsViewModel.startupStatus.collectAsState()
    val initDone by settingsViewModel.initDone.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    val showInitUI by remember(startupStatus) {
        derivedStateOf {
            startupStatus == StartupStatus.MissingUri || startupStatus == StartupStatus.InvalidUri
        }
    }


    suspend fun initialize(uri: Uri) {
        isLoading = true
        settingsViewModel.setStorageRoot(uri)

        withContext(Dispatchers.IO) {
            StorageUtils.copyAssetsIfNotExists(context, uri)
            orchardViewModel.loadConfigFromStorage(uri, context)
            imageViewModel.loadImageCacheFromStorage(uri)
        }

        settingsViewModel.markInitDone()
        isLoading = false
        onInitFinished()
    }

    LaunchedEffect(startupStatus) {
        if (startupStatus == StartupStatus.MissingConfig || startupStatus == StartupStatus.InitConfig) {
            val uri = storageRootUri
            if (uri != null && !initDone && !isLoading) {
                initialize(uri)
            }
        } else if (startupStatus == StartupStatus.Ready && !initDone) {
            settingsViewModel.markInitDone()
            onInitFinished()
        }
    }

    // Launch folder picker
    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null && StorageUtils.hasAccessToUri(context, uri)) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            coroutineScope.launch {
                initialize(uri)
            }
        } else {
            Log.w("InitScreen", "⚠️ Picker returned null or inaccessible URI")
            val cacheReady = orchardViewModel.locations.value.isNotEmpty()
            settingsViewModel.updateStartupStatus(cacheReady)
        }
    }



    // Show loading spinner if needed
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Initializing storage...")
            }
        }
        return
    }

    // Init complete
    if (initDone) {
        onInitFinished()
        return
    }

    // Main Init UI fallback (for Missing/Invalid URI)
    if (showInitUI) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Welcome to PomoloBee") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PomoloBee needs a storage folder to continue.")
                Text("Please pick a folder to use for your workspace.\nThis will store all your images, configs and logs.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = {
                    folderPickerLauncher.launch(null)
                }) {
                    Text("Select Storage Folder")
                }
            }
        }
        return
    }

    // Auto initialize if config is present
    if (startupStatus == StartupStatus.MissingConfig || startupStatus == StartupStatus.InitConfig) {
        storageRootUri?.let { uri ->
            LaunchedEffect(uri) {
                initialize(uri)
            }
        }
        return
    }

    // Edge case: ready but not marked done
    if (startupStatus == StartupStatus.Ready) {
        settingsViewModel.markInitDone()
        onInitFinished()
    }
}
