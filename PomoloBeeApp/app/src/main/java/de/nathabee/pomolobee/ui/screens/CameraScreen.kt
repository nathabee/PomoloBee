package de.nathabee.pomolobee.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory

import org.opencv.android.OpenCVLoader



@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val imageDirectory by viewModel.imageDirectory.collectAsState()
    val selectedFieldId by viewModel.selectedFieldId.collectAsState()
    val selectedRowId by viewModel.selectedRowId.collectAsState()

    val selectedLocation = OrchardCache.locations.find { it.field.fieldId == selectedFieldId }
    val selectedRow = selectedLocation?.rows?.find { it.rowId == selectedRowId }

    val locationStatus = if (selectedLocation != null && selectedRow != null)
        "✅ ${selectedLocation.field.name} / ${selectedRow.shortName}"
    else
        "❌ No Location Selected"



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
        Column {
            Row {
                Button(onClick = { /* TODO: Open camera */ }) {
                    Text("📸 Take Picture")
                }
                Button(onClick = { /* TODO: Open gallery */ }) {
                    Text("🖼️ Upload from Gallery")
                }
            }

            // TODO: If image selected, show preview here

            Button(onClick = { navController.navigate(Screen.Location.route) }) {
                Text("📍 Select Location")
            }

            Text("📌 Status: $locationStatus")

            Button(onClick = {
                // TODO: Save image + metadata locally
            }) {
                Text("💾 Save Image Locally")
            }

            Text("Storage Path: $imageDirectory")
        }

    }
}

/*


🛠️ Still Missing (To-Do List)
Feature	Task
📸 Camera capture	Hook up to real capture logic (CameraX or native CameraView)
🖼 Upload from gallery	Use ActivityResultLauncher<Intent> to pick an image
🖼 Preview selected image	Show selected image in UI before saving
💾 Save image locally	Write image to imageDirectory, along with field/row metadata
➡ Navigate to ProcessingScreen after save (optional)	You can add this logic later
💡 Suggested Enhancements (Future)
Add a viewModel.selectedImageUri: State<Uri?> or similar for image handling

Store ImageMetadata (fieldId + rowId) in a small database or file (if needed)

Add an optional snackbar/toast on successful image save



*/
