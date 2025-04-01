package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.repository.ConnectionRepository.syncOrchard
import de.nathabee.pomolobee.repository.ConnectionRepository.testConnection
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    navController: NavController? = null
) {
    // Get context
    val context = LocalContext.current

    // Get SettingsViewModel using factory
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context) // Pass context to factory
    )

    val scope = rememberCoroutineScope()

    // Collecting state values
    val lastSyncDate by viewModel.lastSyncDate.collectAsState()
    val apiEndpoint by viewModel.apiEndpoint.collectAsState()
    val syncMode by viewModel.syncMode.collectAsState()

    val configPath by viewModel.configPath.collectAsState()
    val mediaEndpoint by viewModel.mediaEndpoint.collectAsState()
    val isDebug by viewModel.isDebug.collectAsState()
    val apiVersion by viewModel.apiVersion.collectAsState()

    // Mutable states for inputs
    var apiInput by remember { mutableStateOf(apiEndpoint ?: "") }
    var mediaInput by remember { mutableStateOf(mediaEndpoint ?: "") }
    var selectedSyncMode by remember { mutableStateOf(syncMode ?: "local") }
    var connectionStatus by remember { mutableStateOf<String?>(null) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Last sync display
        Text("🕒 Last sync: ${lastSyncDate?.let { Date(it).toLocaleString() } ?: "Never"}")
        Spacer(modifier = Modifier.height(8.dp))

        // Cached fields and fruits
        Text("🧭 Fields Cached: ${OrchardCache.locations.size}")
        Text("🍏 Fruits Cached: ${OrchardCache.fruits.size}")
        Spacer(modifier = Modifier.height(16.dp))

        // Sync mode selection
        Text("🌐 Sync Mode:")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("cloud", "local").forEach { mode ->
                FilterChip(
                    selected = selectedSyncMode == mode,
                    onClick = {
                        selectedSyncMode = mode
                        scope.launch { viewModel.updateSyncMode(mode) }
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
                    viewModel.updateApiEndpoint(apiInput)
                    viewModel.updateMediaEndpoint(mediaInput)
                }
            }
        ) {
            Text("💾 Save Settings")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Config path display
        Text("📂 Image Path: ${configPath?.replace("/config", "/images")}")
        Text("📂 Config Path: $configPath")

        Spacer(modifier = Modifier.height(16.dp))

        // Test connection button
        Button(onClick = {
            scope.launch {
                connectionStatus = if (testConnection(context)) "✅ Connection OK" else "❌ Connection failed"
            }
        }) {
            Text("🔌 Test Connection")
        }
        connectionStatus?.let { Text(it) }

        Spacer(modifier = Modifier.height(16.dp))

        // Sync now button
        Button(onClick = {
            scope.launch {
                syncMessage = syncOrchard(context)
            }
        }) {
            Text("🔄 Sync Now")
        }
        syncMessage?.let { Text(it) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("🧪 API Version: ${apiVersion ?: "Unknown"}")
        Text("🐞 Debug: ${if (isDebug) "ON" else "OFF"}")
    }
}
