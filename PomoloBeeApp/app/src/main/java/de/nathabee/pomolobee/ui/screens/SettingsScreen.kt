package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.ui.platform.LocalContext

import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.ui.components.FolderPicker
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import de.nathabee.pomolobee.util.copyAssetsIfNotExists
import de.nathabee.pomolobee.util.getFriendlyFolderName


@Composable
fun SettingsScreen(
    navController: NavController? = null,
    orchardViewModel: OrchardViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    //val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val scope = rememberCoroutineScope()

    val lastSyncDate by settingsViewModel.lastSyncDate.collectAsState()
    val apiEndpoint by settingsViewModel.apiEndpoint.collectAsState()
    val syncMode by settingsViewModel.syncMode.collectAsState()
    val mediaEndpoint by settingsViewModel.mediaEndpoint.collectAsState()
    val isDebug by settingsViewModel.isDebug.collectAsState()
    val apiVersion by settingsViewModel.apiVersion.collectAsState()


    var apiInput by remember { mutableStateOf(apiEndpoint ?: "") }
    var mediaInput by remember { mutableStateOf(mediaEndpoint ?: "") }
    var selectedSyncMode by remember { mutableStateOf(syncMode ?: "local") }
    var connectionStatus by remember { mutableStateOf<String?>(null) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    var showFolderPicker by remember { mutableStateOf(false) }

    if (showFolderPicker) {
        FolderPicker(onFolderSelected = { selectedUri ->
            showFolderPicker = false
            scope.launch {
                settingsViewModel.setStorageRoot(selectedUri)
                copyAssetsIfNotExists(context, selectedUri)
                orchardViewModel.loadLocalConfig(selectedUri, context)
// ↓ Add after if you want to force recomposition
                settingsViewModel.invalidate()

            }
        })
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Last sync display
        Text("🕒 Last sync: ${lastSyncDate?.let { Date(it).toLocaleString() } ?: "Never"}")
        Spacer(modifier = Modifier.height(8.dp))

        // Cached fields and fruits
        val fieldCount by orchardViewModel.fieldCount.collectAsState()
        val fruitCount by orchardViewModel.fruitCount.collectAsState()

        Text("🧭 Fields Cached: $fieldCount")
        Text("🍏 Fruits Cached: $fruitCount")


        Spacer(modifier = Modifier.height(16.dp))

        // Sync mode selection
        Text("🌐 Sync Mode:")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("cloud", "local").forEach { mode ->
                FilterChip(
                    selected = selectedSyncMode == mode,
                    onClick = {
                        selectedSyncMode = mode
                        scope.launch { settingsViewModel.updateSyncMode(mode) }
                    },
                    label = { Text(mode.uppercase()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // API Endpoint input
        Text("🔌 API Endpoint:")
        TextField(
            value = apiInput,
            onValueChange = { apiInput = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Media Endpoint input
        Text("🖼 Media Endpoint:")
        TextField(
            value = mediaInput,
            onValueChange = { mediaInput = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save settings button
        Button(
            onClick = {
                scope.launch {
                    settingsViewModel.updateApiEndpoint(apiInput)
                    settingsViewModel.updateMediaEndpoint(mediaInput)
                }
            }
        ) {
            Text("💾 Save Settings")
        }


        Spacer(modifier = Modifier.height(24.dp))



        // Test connection button
        Button(onClick = {
            settingsViewModel.performConnectionTest { success ->
                connectionStatus = if (success) "✅ Connection OK" else "❌ Connection failed"
            }
        }) {
            Text("🔌 Test Connection")
        }
        connectionStatus?.let { Text(it) }

        Spacer(modifier = Modifier.height(16.dp))

        // Sync now button
        Button(onClick = {
            settingsViewModel.performLocalSync(context) { msg ->
                syncMessage = msg
            }

        }) {
            Text("🔄 Sync Now")
        }
        syncMessage?.let { Text(it) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("🧪 API Version: ${apiVersion ?: "Unknown"}")
        Text("🐞 Debug: ${if (isDebug) "ON" else "OFF"}")

        // ✅ Change storage folder button
        Button(
            onClick = { showFolderPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📂 Change Storage Folder")
        }


        Text("📂 Storage Location: ${storageRootUri?.let { getFriendlyFolderName(context, it) } ?: "Not set"}")



        Spacer(modifier = Modifier.height(16.dp))

    }
}
