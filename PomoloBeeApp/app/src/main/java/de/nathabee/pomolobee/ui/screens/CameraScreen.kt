package de.nathabee.pomolobee.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.data.UserPreferences
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.ui.components.CameraView

import org.opencv.android.OpenCVLoader

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first


@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val userPrefs = UserPreferences(context)

    val configDir = runBlocking {
        userPrefs.getConfigPath().first()
    }
    val selectedFieldId by userPrefs.getSelectedFieldId().collectAsState(initial = null)


    val openCvLoaded = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        OpenCVLoader.initDebug().also {
            openCvLoaded.value = it
            Log.d("CameraScreen", "OpenCV Loaded: $it")
        }
        onDispose { }
    }

    val selectedFieldName = OrchardCache.locations
        .find { it.field.fieldId == selectedFieldId }
        ?.field?.name ?: "❌ No Field Selected"

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (openCvLoaded.value) {
                // Camera preview
                CameraView(context = context, modifier = Modifier.fillMaxWidth().weight(1f))
            } else {
                Text("Loading OpenCV...", fontSize = 20.sp)
            }

            // 📍 Select Location Button
            Button(onClick = {
                navController.navigate(Screen.Location.route)
            }) {
                Text("📍 Select Location")
            }

            // 📌 Selected Field & Row Label
            Text("Selected Field: $selectedFieldName")

            // 💾 Save Button (just placeholder)
            Button(onClick = {
                // Save action here
            }) {
                Text("💾 Save Image Locally")
            }
            Text("Storage Path: $configDir")

        }
    }
}
