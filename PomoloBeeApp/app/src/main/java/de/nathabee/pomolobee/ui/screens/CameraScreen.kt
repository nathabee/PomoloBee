/* // will be used in future not now
val openCvLoaded = remember { mutableStateOf(false) }
DisposableEffect(Unit) {
    OpenCVLoader.initDebug().also {
        openCvLoaded.value = it
        Log.d("CameraScreen", "OpenCV Loaded: $it")
    }
    onDispose {}
}
 */
package de.nathabee.pomolobee.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.compose.AsyncImage
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.util.ErrorLogger
import de.nathabee.pomolobee.util.getFriendlyFolderName
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import java.io.File
import java.io.InputStream
import de.nathabee.pomolobee.util.resolveDocumentDir
import de.nathabee.pomolobee.util.resolveSubDirectory
import java.util.Locale

// name of picture <FieldShortName>_<RowShortName>_<yyyyMMdd_HHmmss>.jpg

@Composable
fun CameraScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    orchardViewModel: OrchardViewModel
) {
    val context = LocalContext.current

    //val imageDirectory by settingsViewModel.imageDirectory.collectAsState()
    val selectedFieldId by settingsViewModel.selectedFieldId.collectAsState()
    val selectedRowId by settingsViewModel.selectedRowId.collectAsState()
    val locations by orchardViewModel.locations.collectAsState()

    val imageSourceUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoTempUri = rememberSaveable { mutableStateOf<Uri?>(null) }

    val storageRootUri = settingsViewModel.storageRootUri.collectAsState().value
    val imagesDir = remember(storageRootUri) {
        resolveSubDirectory(context, storageRootUri, "images")
    }

    // Gallery picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageSourceUri.value = uri
    }


    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageSourceUri.value = photoTempUri.value

        }
    }

    // Build location string
    val selectedLocation = locations.find { it.field.fieldId == selectedFieldId }
    val selectedRow = selectedLocation?.rows?.find { it.rowId == selectedRowId }
    val locationStatus = if (selectedLocation != null && selectedRow != null)
        "✅ ${selectedLocation.field.name} / ${selectedRow.shortName}"
    else "❌ No Location Selected"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                // 1. Create a temp file before launching camera
                val file = File.createTempFile("photo_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                photoTempUri.value = uri
                cameraLauncher.launch(uri)
            }) {
                Text("📸 Take Picture")
            }

            Button(onClick = {
                pickImageLauncher.launch("image/*")
            }) {
                Text("🖼️ Upload from Gallery")
            }
        }

        imageSourceUri.value?.let { uri ->
            Text("🖼️ Selected: ${uri.lastPathSegment}")
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Button(onClick = { navController.navigate(Screen.Location.route) }) {
            Text("📍 Select Location")
        }

        Text("📌 Status: $locationStatus")

        Button(onClick = {
            val sourceUri = imageSourceUri.value
            //val imagesDir = resolveSubDirectory(context, settingsViewModel.storageRootUri.value, "images")



            if (imagesDir == null) {
                val msg = "❌ Image directory is not available"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                ErrorLogger.logError(context, storageRootUri, msg)
                return@Button
            }

            try {
                if (sourceUri != null) {
                    val resolver = context.contentResolver

                    val inputStream = resolver.openInputStream(sourceUri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    val resized = Bitmap.createScaledBitmap(originalBitmap, 800, 600, true)

                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(System.currentTimeMillis())

                    val fieldShort = selectedLocation?.field?.shortName ?: "UnknownField"
                    val rowShort = selectedRow?.shortName ?: "UnknownRow"
                    val fileName = "${fieldShort}_${rowShort}_$timestamp.jpg"

                    val imageFile = imagesDir.createFile("image/jpeg", fileName)

                    if (imageFile != null) {
                        resolver.openOutputStream(imageFile.uri)?.use { output ->
                            resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
                            Toast.makeText(context, "✅ Saved to /images/$fileName", Toast.LENGTH_SHORT).show()
                            Log.d("CameraScreen", "✅ Saved to: /images/$fileName")

                        }
                    } else {
                        val msg = "❌ Could not create image file"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        ErrorLogger.logError(context, settingsViewModel.storageRootUri.value, msg)
                    }
                } else {
                    val msg = "❌ No image or storage path"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    ErrorLogger.logError(context, settingsViewModel.storageRootUri.value, msg)
                }
            } catch (e: Exception) {
                val msg = "❌ Failed to process and save image"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                ErrorLogger.logError(context, settingsViewModel.storageRootUri.value, msg, e)
            }
        }) {
            Text("💾 Save Image Locally")
        }



        val folderName = imagesDir?.uri?.let { getFriendlyFolderName(context, it) } ?: "❌ Not set"
        Text("📂 Storage Location: $folderName")

    }
}


