package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.ui.component.ImageCard
import de.nathabee.pomolobee.ui.components.ExposedDropdownMenuBoxWithLabel
import de.nathabee.pomolobee.viewmodel.ImageViewModel
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel


@Composable
fun ImageHistoryScreen(
    imageViewModel: ImageViewModel,
    orchardViewModel: OrchardViewModel,
    settingsViewModel: SettingsViewModel
) {
    val locations by orchardViewModel.locations.collectAsState()
    val images by imageViewModel.filteredImages.collectAsState()

    val selectedFieldId by imageViewModel.selectedFieldId.collectAsState()
    val selectedRowId by imageViewModel.selectedRowId.collectAsState()

    val selectedLocation = remember(selectedFieldId, locations) {
        locations.find { it.field.fieldId == selectedFieldId }
    }

    val rows = selectedLocation?.rows ?: emptyList()
    val selectedRow = rows.find { it.rowId == selectedRowId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🌱 Field Dropdown
        ExposedDropdownMenuBoxWithLabel(
            label = "🌱 Field",
            items = locations.map { it.field.name },
            selectedItem = selectedLocation?.field?.name,
            onItemSelected = { name ->
                val selected = locations.find { it.field.name == name }
                imageViewModel.selectField(selected?.field?.fieldId)
                settingsViewModel.updateSelectedField(selected?.field?.fieldId ?: -1)
            }
        )

        // 🌿 Row Dropdown
        if (selectedLocation != null) {
            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBoxWithLabel(
                label = "🌿 Row",
                items = rows.map { it.name },
                selectedItem = selectedRow?.name,
                onItemSelected = { name ->
                    val selected = rows.find { it.name == name }
                    imageViewModel.selectRow(selected?.rowId)
                    settingsViewModel.updateSelectedRow(selected?.rowId ?: -1)
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        if (images.isEmpty()) {
            Text("❌ No images found for this selection.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images.size) { idx ->
                    ImageCard(
                        image = images[idx],
                        onPreview = { /* TODO: handle preview */ },
                        onAnalyze = { /* TODO: handle analysis */ },
                        onDelete = { /* TODO: handle delete */ }
                    )
                }
            }
        }
    }
}
