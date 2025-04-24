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
import de.nathabee.pomolobee.ui.components.ExposedDropdownMenuBoxWithLabel
import de.nathabee.pomolobee.util.StorageUtils
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import de.nathabee.pomolobee.navigation.Screen


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
                        settingsViewModel.updateSelectedField(-1)
                    } else {
                        val selected = locations.find { it.field.name == name }
                        imageViewModel.selectField(selected?.field?.fieldId)
                        settingsViewModel.updateSelectedField(selected?.field?.fieldId ?: -1)
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
                            settingsViewModel.updateSelectedRow(-1)
                        } else {
                            val selected = rows.find { it.name == name }
                            imageViewModel.selectRow(selected?.rowId)
                            settingsViewModel.updateSelectedRow(selected?.rowId ?: -1)
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

            items(  pendingImages,
                    key = { it.imageId?.toString() ?: it.originalFilename ?: "unknown-${it.hashCode()}" }
                    ) { image ->
                ImageCard(
                    image = image,
                    rootUri = storageRootUri,
                    imagesDir = imagesDir,
                    mediaUrl = "",
                    isCloudMode = isCloudMode,
                    onPreview = {
                        navController?.navigate(
                            Screen.SvgMap.withArgs(
                                "fieldId" to it.fieldId.toString(),
                                "returnKey" to "readonly_preview",
                                "xyMarker" to (it.xyLocation ?: ""),
                                "readOnly" to "true"
                            )
                        )
                    },
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
            items(processedImages,
                key = { it.imageId?.toString() ?: it.originalFilename ?: "unknown-${it.hashCode()}" }
            ) { image ->
                ImageCard(
                    image = image,
                    imagesDir = imagesDir,
                    rootUri = storageRootUri,
                    mediaUrl = mediaUrl ?: "",
                    isCloudMode = isCloudMode,
                    onPreview = {
                        navController?.navigate(
                            Screen.SvgMap.withArgs(
                                "fieldId" to it.fieldId.toString(),
                                "returnKey" to "readonly_preview",
                                "xyMarker" to (it.xyLocation ?: ""),
                                "readOnly" to "true"
                            )
                        )
                    },
                    onAnalyze = {},
                    onDelete = {}
                )
            }
        }
    }
}