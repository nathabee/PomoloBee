package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.ui.components.ExposedDropdownMenuBoxWithLabel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory

@Composable
fun LocationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))

    val locations = OrchardCache.locations

    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedRow by remember { mutableStateOf<Row?>(null) }

    val selectedRowId by viewModel.selectedRowId.collectAsState()
    val selectedFieldId by viewModel.selectedFieldId.collectAsState()

    val rows = selectedLocation?.rows ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🌱 Field Dropdown
        ExposedDropdownMenuBoxWithLabel(
            label = "🌱 Field",
            items = locations.map { it.field.name },
            selectedItem = selectedLocation?.field?.name,
            onItemSelected = { name ->
                selectedLocation = locations.find { it.field.name == name }
                selectedRow = null // reset row when field changes
            }
        )

        // 🔄 Sync field to preferences
        LaunchedEffect(selectedLocation?.field?.fieldId) {
            selectedLocation?.field?.fieldId?.let { viewModel.updateSelectedField(it) }
        }

        // 🔁 Restore location from stored fieldId
        LaunchedEffect(selectedFieldId) {
            if (selectedFieldId != null && selectedLocation == null) {
                selectedLocation = locations.find { it.field.fieldId == selectedFieldId }
            }
        }

        // 🌿 Row Dropdown (only after field selected)
        if (selectedLocation != null) {
            ExposedDropdownMenuBoxWithLabel(
                label = "🌿 Row",
                items = rows.map { it.name },
                selectedItem = selectedRow?.name,
                onItemSelected = { rowName ->
                    selectedRow = rows.find { it.name == rowName }
                    selectedRow?.rowId?.let { viewModel.updateSelectedRow(it) }
                }
            )
        }

        // 🧠 Restore selected row after SVG map or reload
        LaunchedEffect(selectedRowId, selectedLocation) {
            if (selectedRowId != null && selectedLocation != null) {
                selectedRow = selectedLocation!!.rows.find { it.rowId == selectedRowId }
            }
        }

        // 📝 Status
        Text("📌 Status: ${
            if (selectedLocation != null && selectedRow != null)
                "✅ ${selectedLocation!!.field.name} / ${selectedRow!!.shortName}"
            else
                "❌ No location selected"
        }")

        // 🗺️ SVG Map Button
        if (selectedLocation != null) {
            Button(onClick = {
                navController.navigate(Screen.SvgMap.createRoute(selectedLocation!!.field.fieldId))
            }) {
                Text("🗺️ Select from Map")
            }
        }

        // ✅ Confirm Button
        Button(
            onClick = {
                println("✅ Field: ${selectedLocation?.field?.name}, Row: ${selectedRow?.name}")
                navController.popBackStack()
            },
            enabled = selectedLocation != null && selectedRow != null
        ) {
            Text("✅ Confirm & Continue")
        }
    }
}
