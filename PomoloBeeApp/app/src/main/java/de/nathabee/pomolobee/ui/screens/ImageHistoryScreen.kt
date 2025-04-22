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


@Composable
fun ImageHistoryScreen(
    sharedViewModels: PomolobeeViewModels
) {
    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val settingsViewModel = sharedViewModels.settings


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🌱 Field selector
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


        // 🌿 Row selector
        if (selectedLocation != null) {
            Spacer(Modifier.height(12.dp))

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


        Spacer(Modifier.height(16.dp))

        // 🕒 Pending images
        if (pendingImages.isNotEmpty()) {
            Text("🕒 Pending Images", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pendingImages.size) { idx ->
                    ImageCard(
                        image = pendingImages[idx],
                        onPreview = { /* TODO: Preview image */ },
                        onAnalyze = { /* TODO: Trigger analysis */ },
                        onDelete = { /* TODO: Delete pending */ }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ✅ Processed images
        Text("✅ Processed Images", style = MaterialTheme.typography.titleMedium)
        if (processedImages.isEmpty()) {
            Text("❌ No processed images found for this selection.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(processedImages.size) { idx ->
                    ImageCard(
                        image = processedImages[idx],
                        onPreview = { /* TODO: Preview image */ },
                        onAnalyze = { /* TODO: Re-analyze */ },
                        onDelete = { /* TODO: Delete processed */ }
                    )
                }
            }
        }
    }
}
