package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.nathabee.pomolobee.data.UserPreferences

@Composable
fun AboutScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    val apiVersion by prefs.getApiVersion().collectAsState(initial = "Not available")

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

