package de.nathabee.pomolobee.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.model.Estimation
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.ui.preview.EstimationPreviewProvider
import de.nathabee.pomolobee.ui.preview.FruitPreviewProvider

@Composable
fun EstimationCard(
    estimation: Estimation,
    onPreview: (Estimation) -> Unit = {},
    onDelete: (Estimation) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text("📅 ${estimation.date}", style = MaterialTheme.typography.labelMedium)
            Text("🧪 Status: ${estimation.status}", style = MaterialTheme.typography.labelSmall)
            Text("🌿 Fruit Type: ${estimation.fruitType}", style = MaterialTheme.typography.bodyMedium)
            Text("🏡 Field: ${estimation.fieldName}", style = MaterialTheme.typography.labelSmall)
            Text("🌱 Row: ${estimation.rowName}", style = MaterialTheme.typography.labelSmall)

            Text("📦 Row KG: ${estimation.rowKg}", style = MaterialTheme.typography.labelSmall)
            Text("🍏 Plant KG: ${estimation.plantKg}", style = MaterialTheme.typography.labelSmall)
            Text("📈 Maturation: ${estimation.maturationGrade}", style = MaterialTheme.typography.labelSmall)
            Text("🎯 Confidence: ${(estimation.confidenceScore * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onPreview(estimation) }) { Text("Preview") }
                TextButton(onClick = { onDelete(estimation) }) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewEstimationCard(
    @PreviewParameter(EstimationPreviewProvider::class) estimation: Estimation
) {
    EstimationCard(
        estimation = estimation,
        onPreview = {},
        onDelete = {}
    )
}