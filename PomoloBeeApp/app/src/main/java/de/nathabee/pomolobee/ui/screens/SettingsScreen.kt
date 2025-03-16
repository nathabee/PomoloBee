package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.nathabee.pomolobee.data.UserPreferences // ✅ Import UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedApple by remember { mutableStateOf("") } // ✅ Import mutableStateOf

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select Apple Type:")
        TextField(value = selectedApple, onValueChange = { selectedApple = it })

        Button(onClick = {
            scope.launch { UserPreferences.savePreference(context, "apple_type", selectedApple) }
        }) {
            Text("Save Preference")
        }
    }
}
