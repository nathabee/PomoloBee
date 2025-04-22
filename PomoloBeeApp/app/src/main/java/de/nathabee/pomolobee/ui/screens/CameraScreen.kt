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

import PomolobeeViewModels
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import de.nathabee.pomolobee.model.ImageListData
import de.nathabee.pomolobee.model.ImageListResponse
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.util.ErrorLogger
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import java.io.File
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.viewmodel.ImageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import java.util.Locale
import kotlinx.serialization.json.Json

// name of picture <FieldShortName>_<RowShortName>_<yyyyMMdd_HHmmss>.jpg

@Composable
fun CameraScreen(
    navController: NavController,
    sharedViewModels: PomolobeeViewModels
) {
    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val settingsViewModel = sharedViewModels.settings


    //val imageDirectory by settingsViewModel.imageDirectory.collectAsState()
    val selectedFieldId by settingsViewModel.selectedFieldId.collectAsState()
    val selectedRowId by settingsViewModel.selectedRowId.collectAsState()
    val locations by orchardViewModel.locations.collectAsState()

    val imageSourceUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoTempUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()

    val imagesDir = remember(storageRootUri) {
        StorageUtils.resolveSubDirectory(context, storageRootUri, "images")
   }

    //val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val latestJsonEntry = remember { mutableStateOf<ImageRecord?>(null) }
    val coroutineScope = rememberCoroutineScope()







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
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedRow by remember { mutableStateOf<Row?>(null) }

    LaunchedEffect(selectedFieldId, selectedRowId, locations) {
        selectedLocation = locations.find { it.field.fieldId == selectedFieldId }
        selectedRow = selectedLocation?.rows?.find { it.rowId == selectedRowId }
    }

    val locationStatus = selectedLocation?.let { loc ->
        selectedRow?.let { row ->
            "✅ ${loc.field.name} / ${row.shortName}"
        }
    } ?: "❌ No Location Selected"



    val svgReturnKey = "fromSvg_Location"
    val receiveHandle = navController.currentBackStackEntry?.savedStateHandle

    val cameraReturnKey = "fromLocation"
    val sendHandle = navController.previousBackStackEntry?.savedStateHandle

    fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())
    }

// Handle row coming from Location (forwarding data from SVG Map)
    LaunchedEffect(receiveHandle?.get<Int>("${svgReturnKey}_rowId")) {
        val rowId = receiveHandle?.get<Int>("${svgReturnKey}_rowId")
        val xy = receiveHandle?.get<String>("${svgReturnKey}_xy")

        if (rowId != null) {
            selectedLocation = locations.find { it.rows.any { it.rowId == rowId } }
            selectedRow = selectedLocation?.rows?.find { it.rowId == rowId }

            settingsViewModel.updateSelectedRow(rowId)
            selectedLocation?.field?.fieldId?.let {
                settingsViewModel.updateSelectedField(it)
            }

            // Optionally store XY
            if (xy != null) {
                imageViewModel.setPendingXYLocation(xy)
            }

            // Clean up
            receiveHandle.remove<Int>("${svgReturnKey}_rowId")
            receiveHandle.remove<String>("${svgReturnKey}_xy")
        }
    }

    LaunchedEffect(latestJsonEntry.value) {
        val entry = latestJsonEntry.value ?: return@LaunchedEffect

        val pendingPath = "image_data/pending_images.json"

        val existingJson = StorageUtils.readJsonFileFromStorage(context, storageRootUri!!, pendingPath)

        val existingEntries = if (!existingJson.isNullOrBlank()) {
            try {
                Json.decodeFromString<ImageListResponse>(existingJson).data.images
            } catch (e: Exception) {
                Log.e("CameraScreen", "❌ Failed to parse pending_images.json", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        val updatedEntries = existingEntries + entry

        val wrapped = ImageListResponse(
            status = "pending",
            data = ImageListData(
                total = updatedEntries.size,
                limit = 100,
                offset = 0,
                images = updatedEntries
            )
        )

        val newJson = Gson().toJson(wrapped)


        val saved = StorageUtils.saveTextFile(context, storageRootUri!!, pendingPath, newJson)
        if (!saved) {
            Log.e("CameraScreen", "❌ Failed to write pending_images.json")
        } else {
            Log.d("CameraScreen", "✅ pending_images.json updated")
        }

        imageViewModel.addPendingImage(entry)

        latestJsonEntry.value = null // Avoid duplicates
    }








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

        var captureDate by rememberSaveable { mutableStateOf(getTodayDateString()) }
        var userFruitPerPlant by rememberSaveable { mutableStateOf("") }

        OutlinedTextField(
            value = captureDate,
            onValueChange = { captureDate = it },
            label = { Text("📅 Capture Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = userFruitPerPlant,
            onValueChange = { userFruitPerPlant = it },
            label = { Text("🍎 Estimated Fruit per Plant") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { navController.navigate(Screen.Location.route) }) {
            Text("📍 Select Location")
        }

        Text("📌 Status: $locationStatus")

        /*
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

                    val imageFile = imagesDir!!.createFile("image/jpeg", fileName)

                    if (imageFile != null) {
                        resolver.openOutputStream(imageFile.uri)?.use { output ->
                            resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
                            Toast.makeText(context, "✅ Saved to /images/$fileName", Toast.LENGTH_SHORT).show()
                            Log.d("CameraScreen", "✅ Saved to: /images/$fileName")

                        }
                        val imageRecord = ImageRecord(
                            imageId = -1, // or null if you're using nullable Int? — make sure your class allows that!
                            rowId = selectedRow?.rowId ?: -1,
                            fieldId = selectedLocation?.field?.fieldId ?: -1,
                            xyLocation = imageViewModel.pendingXYLocation.value,
                            fruitType = selectedRow?.fruitType ?: "Unknown",
                            userFruitPlant = userFruitPerPlant.toIntOrNull() ?: 0,
                            uploadDate = "", // or null, depending on your model
                            date = captureDate,
                            imageUrl = "",
                            originalFilename = fileName,
                            processed = false,
                            processedAt = null,
                            status = "pending"
                        )

                        latestJsonEntry.value = imageRecord  // ✅ save for writing JSON file



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
        }*/

        Button(onClick = {
            val sourceUri = imageSourceUri.value
            if (imagesDir == null || sourceUri == null) {
                val msg = "❌ Missing image source or image directory"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                ErrorLogger.logError(context, storageRootUri, msg)
                return@Button
            }

            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
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
                            }

                            val imageRecord = ImageRecord(
                                imageId = -1,
                                rowId = selectedRow?.rowId ?: -1,
                                fieldId = selectedLocation?.field?.fieldId ?: -1,
                                xyLocation = imageViewModel.pendingXYLocation.value,
                                fruitType = selectedRow?.fruitType ?: "Unknown",
                                userFruitPlant = userFruitPerPlant.toIntOrNull() ?: 0,
                                uploadDate = "",
                                date = captureDate,
                                imageUrl = "",
                                originalFilename = fileName,
                                processed = false,
                                processedAt = null,
                                status = "pending"
                            )

                            latestJsonEntry.value = imageRecord

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "✅ Saved to /images/$fileName", Toast.LENGTH_SHORT).show()
                                Log.d("CameraScreen", "✅ Saved to: /images/$fileName")
                            }
                        } else {
                            throw Exception("Could not create image file")
                        }
                    } catch (e: Exception) {
                        val msg = "❌ Failed to process and save image"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        ErrorLogger.logError(context, storageRootUri, msg, e)
                    }
                }
            }
        })
        {
            Text("💾 Save Image Locally")
        }





        val folderName = imagesDir?.uri?.let { StorageUtils.getFriendlyFolderName(context, it) } ?: "❌ Not set"
        Text("📂 Storage Location: $folderName")

    }
}


