package de.nathabee.pomolobee.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.nathabee.pomolobee.util.readErrors
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import java.io.File

@Composable
fun ErrorLogScreen(navController: NavController? = null,
                   settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val logs = readErrors(context, settingsViewModel.storageRootUri.value)

        errorMessages.clear()
        errorMessages.addAll(logs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("📜 Error Log", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessages.isEmpty()) {
            Text("✅ No errors logged.")
        } else {
            LazyColumn {
                items(errorMessages) { line ->
                    Text("• $line", modifier = Modifier.padding(vertical = 4.dp))
                    Divider()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        navController?.let {
            Button(onClick = { it.popBackStack() }) {
                Text("⬅ Back")
            }
        }
    }
}
