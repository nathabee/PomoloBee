package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.navigation.Screen

@Composable
fun OrchardScreen(
    navController: NavController,
    orchardViewModel: OrchardViewModel
) {
    val locations by orchardViewModel.locations.collectAsState()

    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(locations) { location ->
            FieldCard(location = location, onVisualize = {
                // navController.navigate(Screen.SvgMap.createRoute(location.field.fieldId))
                navController.navigate(
                    Screen.SvgMap.withArgs(
                        "fieldId" to location.field.fieldId.toString(),
                        "returnKey" to "unused"
                    )
                )


            })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FieldCard(location: Location, onVisualize: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌳 Orchard: ${location.field.name} (${location.field.orientation})",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "📝 ${location.field.description ?: "No description"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onVisualize) {
                Text("📝 Visualize")
            }

            Spacer(modifier = Modifier.height(8.dp))
            location.rows.forEach { row ->
                Text(
                    text = "🌿 ${row.shortName}  • ${row.nbPlant} trees • 🍏 ${row.fruitType}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
