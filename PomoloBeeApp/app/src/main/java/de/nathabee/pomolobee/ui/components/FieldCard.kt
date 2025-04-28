package de.nathabee.pomolobee.ui.component



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.ui.preview.FieldWithFruitsPreviewProvider
import de.nathabee.pomolobee.ui.component.ExpandableRowCardWithFruitEditor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun FieldCard(
    location: Location,
    fruits: List<Fruit>,
    onVisualize: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌳 Orchard: ${location.field.name} (${location.field.orientation})",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "📝 ${location.field.description.ifBlank { "No description" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onVisualize) {
                Text("🧭 Visualize Field")
            }

            Spacer(modifier = Modifier.height(16.dp))

            location.rows.forEach { row ->
                val fruit = fruits.find { it.fruitId == row.fruitId }
                if (fruit != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(1.dp)
                        //horizontalArrangement = Arrangement.spacedBy(8.dp) // Nice space between the two cards
                    ) {
                        ExpandableRowCardWithFruitEditor(
                            modifier = Modifier.padding(1.dp),
                            row = row,
                            fruit = fruit
                        )

                    }

                } else {
                    Text(
                        text = "❌ Fruit not found for row ${row.name}",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewFieldCard(
    @PreviewParameter(FieldWithFruitsPreviewProvider::class) data: Pair<Location, List<Fruit>>
) {
    FieldCard(
        location = data.first,
        fruits = data.second,
        onVisualize = {}
    )
}

