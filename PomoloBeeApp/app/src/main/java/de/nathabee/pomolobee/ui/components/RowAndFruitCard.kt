package de.nathabee.pomolobee.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.ui.preview.RowAndFruitPreviewProvider

// --- RowAndFruitCard ---

@Composable
fun RowAndFruitCard(
    row: Row,
    fruit: Fruit
) {
    Column {
        RowCard(
            row = row,
            onEditFruit = { /* no-op in preview */ }
        )
        Spacer(modifier = Modifier.height(16.dp))
        FruitCard(fruit = fruit)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRowAndFruitCard(
    @PreviewParameter(RowAndFruitPreviewProvider::class) data: Pair<Row, Fruit>
) {
    RowAndFruitCard(
        row = data.first,
        fruit = data.second
    )
}

// --- RowCardWithFruitEditor ---

@Composable
fun RowCardWithFruitEditor(
    row: Row,
    fruit: Fruit
) {
    val showEditor = remember { mutableStateOf(false) }

    Column {
        RowCard(
            row = row,
            onEditFruit = { showEditor.value = true }
        )

        if (showEditor.value) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            FruitCardWithClose(
                fruit = fruit,
                onClose = { showEditor.value = false }
            )
        }
    }
}

@Composable
fun ExpandableRowCardWithFruitEditor(
    modifier: Modifier = Modifier,
    row: de.nathabee.pomolobee.model.Row,
    fruit: de.nathabee.pomolobee.model.Fruit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier, // use the modifier passed
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(1.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌿 ${row.shortName}",
                    style = MaterialTheme.typography.labelLarge
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Close Row" else "Edit Row")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(1.dp))
                    RowCardWithFruitEditor(row = row,fruit = fruit)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRowCardWithFruitEditor(
    @PreviewParameter(RowAndFruitPreviewProvider::class) data: Pair<Row, Fruit>
) {
    RowCardWithFruitEditor(
        row = data.first,
        fruit = data.second
    )
}

// --- FruitCardWithClose ---

@Composable
fun FruitCardWithClose(
    fruit: Fruit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🍎 ${fruit.name}", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClose) {
                    Text("Close", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "🔖 Short Name: ${fruit.shortName}", style = MaterialTheme.typography.labelMedium)
            Text(text = "📅 Yield: ${fruit.yieldStartDate} ➔ ${fruit.yieldEndDate}", style = MaterialTheme.typography.bodySmall)
            Text(text = "⚖️ Avg Yield: ${fruit.yieldAvgKg} kg", style = MaterialTheme.typography.bodySmall)
            Text(text = "🍏 Avg Fruit: ${fruit.fruitAvgKg} kg", style = MaterialTheme.typography.bodySmall)
            Text(text = "📝 ${fruit.description}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
