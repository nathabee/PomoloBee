package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.viewmodel.SettingsViewModel

@Composable
fun AboutScreen(settingsViewModel: SettingsViewModel) {


    val apiVersion by settingsViewModel.apiVersion.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🍯 PomoloBee", style = MaterialTheme.typography.headlineMedium)
        Text("📦 Version: $apiVersion")
        Text("🧑‍💻 Developed with ❤️ by Nathalie")
        Text("🔧 Powered by OpenCV + Jetpack Compose")
    }
}

