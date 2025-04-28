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
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.model.Fruit
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.nathabee.pomolobee.ui.preview.FruitPreviewProvider

@Composable
fun FruitCard(
    fruit: Fruit
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
            Text(text = "🍎 ${fruit.name}", style = MaterialTheme.typography.titleMedium)
            Text(text = "🔖 Short Name: ${fruit.shortName}", style = MaterialTheme.typography.labelMedium)
            Text(text = "📅 Yield: ${fruit.yieldStartDate} ➔ ${fruit.yieldEndDate}", style = MaterialTheme.typography.bodySmall)
            Text(text = "⚖️ Avg Yield: ${fruit.yieldAvgKg} kg", style = MaterialTheme.typography.bodySmall)
            Text(text = "🍏 Avg Fruit: ${fruit.fruitAvgKg} kg", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "📝 ${fruit.description}", style = MaterialTheme.typography.bodySmall)

        }
    }
}


@Composable
fun ExpandableFruitCard(
    modifier: Modifier = Modifier,
    fruit: Fruit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🍏 ${fruit.shortName}",
                    style = MaterialTheme.typography.labelLarge
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Close Fruit" else "View Fruit")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    FruitCard(fruit = fruit)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFruitCard(
    @PreviewParameter(FruitPreviewProvider::class) fruit: Fruit
) {
    FruitCard(
        fruit = fruit
    )
}
