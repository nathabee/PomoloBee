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
import de.nathabee.pomolobee.model.ImageListData
import de.nathabee.pomolobee.model.ImageListResponse
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.ui.component.ImageCard
import de.nathabee.pomolobee.util.ErrorLogger
import java.io.File
import de.nathabee.pomolobee.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

// name of picture <FieldShortName>_<RowShortName>_<yyyyMMdd_HHmmss>.jpg


@Composable
fun CameraScreen(
    navController: NavController,
    sharedViewModels: PomolobeeViewModels
) {


    /*************************************************************************/
    /* variable */
    /*************************************************************************/

    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val settingsViewModel = sharedViewModels.settings
    val cameraViewModel = sharedViewModels.camera




    val imageSourceUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoTempUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()

    val imagesDir = remember(storageRootUri) {
        StorageUtils.resolveSubDirectory(context, storageRootUri, "images")
   }

    //val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val coroutineScope = rememberCoroutineScope()






    // Build location string
    /*
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedRow by remember { mutableStateOf<Row?>(null) }

    val selectedFieldId by cameraViewModel.selectedFieldId.collectAsState()
    val selectedRowId by cameraViewModel.selectedRowId.collectAsState()

     */
    val tempImageRecord by cameraViewModel.tempImageRecord.collectAsState()
    val locations by orchardViewModel.locations.collectAsState()

    val selectedLocation = remember(tempImageRecord, locations) {
        locations.find { it.field.fieldId == tempImageRecord.fieldId }
    }
    val selectedRow = remember(tempImageRecord, selectedLocation) {
        selectedLocation?.rows?.find { it.rowId == tempImageRecord.rowId }
    }

    val locationStatus = selectedLocation?.let { loc ->
        selectedRow?.let { row ->
            "✅ ${loc.field.name} / ${row.shortName}"
        }
    } ?: "❌ No Location Selected"



   // val svgReturnKey = "fromSvg_Location"
   // val receiveHandle = navController.currentBackStackEntry?.savedStateHandle



    /*************************************************************************/
    /* FUNCTION*/
    /*************************************************************************/
    /*
    fun generateFilename(selectedLocation: Location?, selectedRow: Row?): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val fieldShort = selectedLocation?.field?.shortName ?: "UnknownField"
        val rowShort = selectedRow?.shortName ?: "UnknownRow"
        return "${fieldShort}_${rowShort}_$timestamp.jpg"
    }*/



    fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())
    }

    fun createEmptyImageRecord(): ImageRecord {
        return ImageRecord(
            imageId = -1,
            rowId = -1,
            fieldId = -1,
            xyLocation = null,
            fruitType = "No Fruit",
            userFruitPlant = 0,
            uploadDate = "",
            date = getTodayDateString(),
            imageUrl = "", // No image
            originalFilename = "empty_placeholder.jpg",
            processed = false,
            processedAt = null,
            status = "No Status"
        )
    }



    suspend fun savePendingImage(context: Context, rootUri: Uri, newImage: ImageRecord): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val pendingPath = "image_data/pending_images.json"
                val existingJson = StorageUtils.readJsonFileFromStorage(context, rootUri, pendingPath)
                val existingEntries = if (!existingJson.isNullOrBlank()) {
                    Gson().fromJson(existingJson, ImageListResponse::class.java).data.images
                } else {
                    emptyList()
                }

                val updatedEntries = existingEntries + newImage
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
                StorageUtils.saveTextFile(context, rootUri, pendingPath, newJson)
            } catch (e: Exception) {
                Log.e("SavePendingImage", "❌ Failed to save pending image", e)
                false
            }
        }
    }


    //var captureDate by rememberSaveable { mutableStateOf(getTodayDateString()) }
    var userFruitPerPlant by rememberSaveable { mutableStateOf("") }


    val isSaveEnabled by remember(tempImageRecord) {
        derivedStateOf { cameraViewModel.canSaveRecord() }
    }


    /*************************************************************************/
    /* LaunchedEffect*/
    /*************************************************************************/

    // Gallery picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageSourceUri.value = it
            cameraViewModel.updateAfterPicture(
                it,
                fieldShort = selectedLocation?.field?.shortName ?: "UnknownField",
                rowShort = selectedRow?.shortName ?: "UnknownRow",
                xyLocation = imageViewModel.pendingXYLocation.value
            )



        }
    }



    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoTempUri.value?.let { uri ->
                imageSourceUri.value = uri
                cameraViewModel.updateAfterPicture(
                    uri,
                    fieldShort = selectedLocation?.field?.shortName ?: "UnknownField",
                    rowShort = selectedRow?.shortName ?: "UnknownRow",
                    xyLocation = imageViewModel.pendingXYLocation.value
                )

            }
        }
    }



    /*************************************************************************/
    /* UI */
    /*************************************************************************/




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


        ImageCard(
            image = tempImageRecord  ,
            estimation = null,
            rootUri = storageRootUri,
            imagesDir = imagesDir,
            mediaUrl = "",
            isCloudMode = false,
            onPreview = {
                navController.navigate(
                    Screen.SvgMap.withArgs(
                        "fieldId" to cameraViewModel.tempImageRecord.value.fieldId.toString(),
                        "returnKey" to "readonly_preview",
                        "xyMarker" to (cameraViewModel.tempImageRecord.value.xyLocation ?: ""),
                        "readOnly" to "true"
                    )
                )},
            onAnalyze = {},
            onDelete = { cameraViewModel.clearTempImageRecord() }
        )



        // OutlinedTextField that opens DatePickerDialog

        val calendar = remember { Calendar.getInstance() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val dateParts = tempImageRecord.date.split("-")
                    val year = dateParts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                    val month = dateParts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
                    val day = dateParts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                            val newDate = String.format(
                                Locale.getDefault(),
                                "%04d-%02d-%02d",
                                selectedYear,
                                selectedMonth + 1,
                                selectedDayOfMonth
                            )
                            cameraViewModel.updateCaptureDate(newDate)
                        },
                        year,
                        month,
                        day
                    ).show()
                },
            contentAlignment = Alignment.CenterStart // So text is nicely aligned
        ) {
            OutlinedTextField(
                value = tempImageRecord.date,
                onValueChange = {}, // No manual editing
                label = { Text("📅 Capture Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true, // Important
                enabled = false // Disable typing, only show picker on click
            )
        }


        OutlinedTextField(
            value = userFruitPerPlant,
            onValueChange = { input ->
                // Filter out non-digit characters if needed (optional extra safety)
                val filtered = input.filter { it.isDigit() }
                userFruitPerPlant = filtered
                cameraViewModel.updateUserFruitPerPlant(filtered.toIntOrNull() ?: 0)
            },
            label = { Text("🍎 Estimated Fruit per Plant") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )


        Button(onClick = { navController.navigate(Screen.Location.route) }) {
            Text("📍 Select Location")
        }

        Text("📌 Status: $locationStatus")



        var isSaving by remember { mutableStateOf(false) }

        Button(
            onClick = {
                val sourceUri = imageSourceUri.value
                if (imagesDir == null || sourceUri == null) {
                    val msg = "❌ Missing image source, storage directory, or temp record"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    ErrorLogger.logError(context, storageRootUri, msg)
                    return@Button
                }

                coroutineScope.launch {
                    isSaving = true
                    try {
                        val resolver = context.contentResolver
                        val inputStream = resolver.openInputStream(sourceUri)
                        val originalBitmap = BitmapFactory.decodeStream(inputStream)
                        val resized = Bitmap.createScaledBitmap(originalBitmap, 800, 600, true)

                        val filename = cameraViewModel.tempImageRecord.value.originalFilename ?: "default_image.jpg"
                        val imageFile = imagesDir.createFile("image/jpeg", filename)

                        if (imageFile != null) {
                            resolver.openOutputStream(imageFile.uri)?.use { output ->
                                resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
                            }

                            val newPendingImage = cameraViewModel.tempImageRecord.value.copy(
                                imageUrl = "" // Now local
                            )

                            val saved = savePendingImage(context, storageRootUri!!, newPendingImage)

                            withContext(Dispatchers.Main) {
                                if (saved) {
                                    sharedViewModels.image.addPendingImage(newPendingImage)
                                    Toast.makeText(context, "✅ Image saved and pending_images updated", Toast.LENGTH_SHORT).show()
                                    cameraViewModel.clearTempImageRecord()
                                    imageSourceUri.value = null
                                } else {
                                    Toast.makeText(context, "❌ Failed to update pending_images.json", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            throw Exception("❌ Could not create image file")
                        }
                    } catch (e: Exception) {
                        val msg = "❌ Failed to process and save image"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        ErrorLogger.logError(context, storageRootUri, msg, e)
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = isSaveEnabled && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("💾 Save Image Locally")
            }
        }





        val folderName = imagesDir?.uri?.let { StorageUtils.getFriendlyFolderName(context, it) } ?: "❌ Not set"
        Text("📂 Storage Location: $folderName")

    }
}


