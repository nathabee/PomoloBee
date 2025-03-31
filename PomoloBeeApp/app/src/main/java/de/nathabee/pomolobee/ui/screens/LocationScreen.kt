package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.data.UserPreferences
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.ui.components.ExposedDropdownMenuBoxWithLabel

@Composable
fun LocationScreen(navController: NavController) {
    val locations = OrchardCache.locations

    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedRow by remember { mutableStateOf<Row?>(null) }

    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val selectedFieldId by userPrefs.getSelectedFieldId().collectAsState(initial = null)

    val rows = selectedLocation?.rows ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Field Dropdown
        ExposedDropdownMenuBoxWithLabel(
            label = "🌱 Field",
            items = locations.map { it.field.name },
            selectedItem = selectedLocation?.field?.name,
            onItemSelected = { name ->
                selectedLocation = locations.find { it.field.name == name }
                selectedRow = null // reset row
            }
        )

        // Save preference outside UI
        LaunchedEffect(selectedLocation?.field?.fieldId) {
            selectedLocation?.field?.fieldId?.let {
                userPrefs.saveSelectedField(it)
            }
        }

        LaunchedEffect(selectedFieldId) {
            if (selectedFieldId != null && selectedLocation == null) {
                selectedLocation = locations.find { it.field.fieldId == selectedFieldId }
            }
        }


        // Row Dropdown
        if (selectedLocation != null) {
            ExposedDropdownMenuBoxWithLabel(
                label = "🌿 Row",
                items = rows.map { it.name },
                selectedItem = selectedRow?.name,
                onItemSelected = { rowName ->
                    selectedRow = rows.find { it.name == rowName }
                }
            )
        }

        if (selectedLocation == null) {
            Text("📍 Please select a field to begin.", style = MaterialTheme.typography.bodyMedium)
        }


        // Optional: Select from map
        if (selectedLocation != null) {
            Button(onClick = {
                navController.navigate(Screen.SvgMap.createRoute(selectedLocation!!.field.fieldId))
            }) {
                Text("🗺️ Select from Map")
            }
        }

        Text("📌 Status: ${
            if (selectedLocation != null && selectedRow != null)
                "✅ ${selectedLocation!!.field.name} / ${selectedRow!!.shortName}"
            else
                "❌ No location selected"
        }")

        // Confirm Button
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

