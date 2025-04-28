package de.nathabee.pomolobee.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.ui.preview.RowPreviewProvider


@Composable
fun RowCard(
    row: Row,
    onEditFruit: (Row) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🌿 ${row.name}", style = MaterialTheme.typography.titleSmall)
            Text(text = "🔖 Short Name: ${row.shortName}", style = MaterialTheme.typography.labelSmall)
            Text(text = "🍏 Fruit Type: ${row.fruitType}", style = MaterialTheme.typography.bodySmall)
            Text(text = "🌱 Nb Plants: ${row.nbPlant}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onEditFruit(row) }) {
                    Text("Edit Fruit", color = Color.Blue)
                }
            }
        }
    }
}


@Composable
fun ExpandableRowCard(
    modifier: Modifier = Modifier,
    row: de.nathabee.pomolobee.model.Row
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier, // use the modifier passed
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    RowCard(row = row, onEditFruit = {})
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRowCard(
    @PreviewParameter(RowPreviewProvider::class) row: Row
) {
    RowCard(
        row = row,
        onEditFruit = {}
    )
}
