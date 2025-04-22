package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.ui.platform.LocalContext
import de.nathabee.pomolobee.navigation.Screen

import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.ui.components.FolderPicker
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.util.TimeUtils
import de.nathabee.pomolobee.util.safeLaunch
import de.nathabee.pomolobee.viewmodel.ImageViewModel


@Composable
fun SettingsScreen(
    navController: NavController? = null,
    sharedViewModels: PomolobeeViewModels
) {
    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val settingsViewModel = sharedViewModels.settings

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
                safeLaunch(context, settingsViewModel.storageRootUri.value) {
                    settingsViewModel.setStorageRoot(selectedUri)
                    StorageUtils.copyAssetsIfNotExists(context, selectedUri)
                    orchardViewModel.loadConfigFromStorage(selectedUri, context)
                    imageViewModel.loadImageCacheFromStorage(selectedUri)
                    settingsViewModel.invalidate()
                }
            }
        })

    }


    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {


    // Last sync display
        Text("🕒 Last config sync: ${TimeUtils.formatTimestamp(lastSyncDate)}")

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
                        scope.launch {
                            safeLaunch(context, settingsViewModel.storageRootUri.value) {
                                settingsViewModel.updateSyncMode(mode)
                            }

                        }
                    },
                    label = { Text(mode.uppercase()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cloud-specific settings
        if (selectedSyncMode == "cloud") {
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
                        safeLaunch(context, settingsViewModel.storageRootUri.value) {
                            settingsViewModel.updateApiEndpoint(apiInput)
                            settingsViewModel.updateMediaEndpoint(mediaInput)
                        }
                    }
                }
            ) {
                Text("💾 Save Settings")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test connection button
            Button(onClick = {
                settingsViewModel.performConnectionTest(context) { success ->
                    connectionStatus = if (success) "✅ Connection OK" else "❌ Connection failed"
                }
            }) {
                Text("🔌 Test Connection")
            }
            connectionStatus?.let { Text(it) }

            Spacer(modifier = Modifier.height(16.dp))
        }


        // Sync now button
        Button(onClick = {
            safeLaunch(context, settingsViewModel.storageRootUri.value) {
                if (selectedSyncMode == "local") {
                    syncMessage = "⏳ Local config sync..."
                    settingsViewModel.performLocalSync(context, sharedViewModels) { success ->
                        syncMessage = if (success) "✅ Local sync complete"
                        else "❌ Sync failed. See error log for details"
                    }

                } else {
                    // 🔧 Placeholder: implement cloud sync (download + save config + SVGs)
                    syncMessage = "⏳ Cloud config sync..."
                    settingsViewModel.performCloudSync(context, sharedViewModels) { success ->
                        syncMessage = if (success) "✅ Local sync complete"
                        else "❌ Sync failed. See error log for details"
                    }

                }
            }
        }) {
            Text("🔄 Sync Now")
        }
        syncMessage?.let { Text(it) }


        Spacer(modifier = Modifier.height(16.dp))
        Text("🧪 API Version: ${apiVersion ?: "Unknown"}")
        Text("🐞 Debug Mode: ${if (isDebug) "ENABLED" else "DISABLED"}")



        // ✅ Change storage folder button
        Button(
            onClick = { showFolderPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📂 Change Storage Folder")
        }


        Text("📂 Storage Location: ${storageRootUri?.let { StorageUtils.getFriendlyFolderName(context, it) } ?: "Not set"}")


        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Text("🛠 Developer Options")

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🐞 Enable Debug Mode")
            Switch(
                checked = isDebug,
                onCheckedChange = { newValue ->
                    scope.launch {

                        safeLaunch(context, settingsViewModel.storageRootUri.value) {
                            settingsViewModel.updateDebugMode(newValue)
                        }
                    }
                }
            )
        }
        if (isDebug) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {navController?.navigate(Screen.ErrorLog.route)
            }) {
                Text("📜 View Error Log")
            }
        }




        Spacer(modifier = Modifier.height(16.dp))


    }
}
