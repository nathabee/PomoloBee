package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.ui.component.ImageCard
import de.nathabee.pomolobee.ui.component.EstimationCard
import de.nathabee.pomolobee.ui.components.ExposedDropdownMenuBoxWithLabel
import de.nathabee.pomolobee.util.StorageUtils
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.model.Estimation
import de.nathabee.pomolobee.util.findEstimationForImage


@Composable
fun ImageHistoryScreen(
    sharedViewModels: PomolobeeViewModels,
    navController: NavController
) {
    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val settingsViewModel = sharedViewModels.settings

    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    val mediaUrl by settingsViewModel.mediaEndpoint.collectAsState()

    val imagesDir = remember(storageRootUri) {
        StorageUtils.resolveSubDirectory(context, storageRootUri, "images")
    }

    val locations by orchardViewModel.locations.collectAsState()
    val processedImages by imageViewModel.filteredImages.collectAsState()
    val pendingImages by imageViewModel.filteredPendingImages.collectAsState()

    val selectedFieldId by imageViewModel.selectedFieldId.collectAsState()
    val selectedRowId by imageViewModel.selectedRowId.collectAsState()

    val selectedLocation = remember(selectedFieldId, locations) {
        locations.find { it.field.fieldId == selectedFieldId }
    }

    val rows = selectedLocation?.rows.orEmpty()
    val selectedRow = rows.find { it.rowId == selectedRowId }

    val syncMode by settingsViewModel.syncMode.collectAsState()
    val isCloudMode = syncMode == "cloud"

    // ✅ FIX: selectedEstimation must be OUTSIDE of the LazyColumn
    var selectedEstimation by remember { mutableStateOf<Estimation?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🌱 Field selector
        item {
            val fieldNames = listOf("All Fields") + locations.map { it.field.name }
            ExposedDropdownMenuBoxWithLabel(
                label = "🌱 Field",
                items = fieldNames,
                selectedItem = selectedLocation?.field?.name ?: "All Fields",
                onItemSelected = { name ->
                    if (name == "All Fields") {
                        imageViewModel.selectField(null)
                    } else {
                        val selected = locations.find { it.field.name == name }
                        imageViewModel.selectField(selected?.field?.fieldId)
                    }
                }
            )
        }

        // 🌿 Row selector
        if (selectedLocation != null) {
            item {
                val rowNames = listOf("All Rows") + rows.map { it.name }
                ExposedDropdownMenuBoxWithLabel(
                    label = "🌿 Row",
                    items = rowNames,
                    selectedItem = selectedRow?.name ?: "All Rows",
                    onItemSelected = { name ->
                        if (name == "All Rows") {
                            imageViewModel.selectRow(null)
                        } else {
                            val selected = rows.find { it.name == name }
                            imageViewModel.selectRow(selected?.rowId)
                        }
                    }
                )
            }
        }

        // 🕒 Pending images
        if (pendingImages.isNotEmpty()) {
            item {
                Text("🕒 Pending Images", style = MaterialTheme.typography.titleMedium)
            }

            items(
                pendingImages,
                key = { it.imageId?.toString() ?: it.originalFilename ?: "unknown-${it.hashCode()}" }
            ) { image ->
                ImageCard(
                    image = image,
                    estimation = null, // 🚫 pending images have no estimation
                    rootUri = storageRootUri,
                    imagesDir = imagesDir,
                    mediaUrl = mediaUrl ?: "",
                    isCloudMode = isCloudMode,
                    onPreview = { /* TODO */ },
                    onAnalyze = {},
                    onDelete = {}
                )
            }
        }

        // ✅ Processed images
        item {
            Text("✅ Processed Images", style = MaterialTheme.typography.titleMedium)
        }

        if (processedImages.isEmpty()) {
            item {
                Text("❌ No processed images found for this selection.")
            }
        } else {
            items(
                processedImages,
                key = { it.imageId?.toString() ?: it.originalFilename ?: "unknown-${it.hashCode()}" }
            ) { image ->
                val estimation = remember(image.imageId) {
                    findEstimationForImage(image.imageId)
                }

                ImageCard(
                    image = image,
                    estimation = estimation,
                    imagesDir = imagesDir,
                    rootUri = storageRootUri,
                    mediaUrl = mediaUrl ?: "",
                    isCloudMode = isCloudMode,
                    onPreview = {
                        navController.navigate(
                            Screen.SvgMap.withArgs(
                                "fieldId" to it.fieldId.toString(),
                                "returnKey" to "readonly_preview",
                                "xyMarker" to (it.xyLocation ?: ""),
                                "readOnly" to "true"
                            )
                        )
                    },
                    onAnalyze = { selectedEstimation = estimation },
                    onDelete = {}
                )
            }
        }
    }

    // ✅ Show the EstimationCard in a dialog if selected
    selectedEstimation?.let { estimation ->
        AlertDialog(
            onDismissRequest = { selectedEstimation = null },
            title = { Text("Estimation Detail") },
            text = {
                EstimationCard(
                    estimation = estimation,
                    onPreview = {},
                    onDelete = { selectedEstimation = null }
                )
            },
            confirmButton = {
                Button(onClick = { selectedEstimation = null }) {
                    Text("Close")
                }
            }
        )
    }
}
