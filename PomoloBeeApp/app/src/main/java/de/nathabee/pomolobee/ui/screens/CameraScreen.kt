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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.compose.AsyncImage
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.util.getFriendlyFolderName
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import java.io.File
import java.io.InputStream

@Composable
fun CameraScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    orchardViewModel: OrchardViewModel
) {
    val context = LocalContext.current

    val imageDirectory by settingsViewModel.imageDirectory.collectAsState()
    val selectedFieldId by settingsViewModel.selectedFieldId.collectAsState()
    val selectedRowId by settingsViewModel.selectedRowId.collectAsState()
    val locations by orchardViewModel.locations.collectAsState()

    val galleryImageUri = remember { mutableStateOf<Uri?>(null) }
    val takenPhotoUri = remember { mutableStateOf<Uri?>(null) }
    val imageToSaveUri = remember { mutableStateOf<Uri?>(null) }
    val photoTempUri = remember { mutableStateOf<Uri?>(null) }

    // Gallery picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        galleryImageUri.value = uri
        imageToSaveUri.value = uri
    }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            takenPhotoUri.value = photoTempUri.value
            imageToSaveUri.value = photoTempUri.value
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

        imageToSaveUri.value?.let { uri ->
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
            val sourceUri = imageToSaveUri.value
            if (sourceUri != null && imageDirectory != null) {
                val resolver = context.contentResolver
                val docDir = DocumentFile.fromTreeUri(context, imageDirectory!!)
                val inputStream: InputStream? = resolver.openInputStream(sourceUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                val resized = Bitmap.createScaledBitmap(originalBitmap, 800, 600, true)

                val imageFile = docDir?.createFile("image/jpeg", "IMG_${System.currentTimeMillis()}.jpg")
                if (imageFile == null) {
                    Toast.makeText(context, "❌ Failed to create file", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                imageFile.uri.let { targetUri ->
                    resolver.openOutputStream(targetUri)?.use { output ->
                        resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
                        val folderName = getFriendlyFolderName(context, imageDirectory!!)
                        Toast.makeText(context, "✅ Image saved to $folderName", Toast.LENGTH_SHORT).show()
                        Log.d("CameraScreen", "✅ Image saved to:  $folderName")
                    }
                }
            } else {
                Toast.makeText(context, "❌ No image or storage path", Toast.LENGTH_SHORT).show()
                Log.e("CameraScreen", "No source image or storage URI set")
            }
        }) {
            Text("💾 Save Image Locally")
        }

        Text("📂 Storage Location: ${imageDirectory?.let { getFriendlyFolderName(context, it) } ?: "❌ Not set"}")

        // Debug info
        imageToSaveUri.value?.let { Text("🔍 imageToSaveUri: $it") }
    }
}
