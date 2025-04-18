package de.nathabee.pomolobee.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.util.parseXYLocation

@Composable
fun ImageCard(
    image: ImageRecord,
    onPreview: (ImageRecord) -> Unit,
    onAnalyze: (ImageRecord) -> Unit,
    onDelete: (ImageRecord) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Image thumbnail
            Image(
                painter = rememberAsyncImagePainter(image.imageUrl),
                contentDescription = "Image thumbnail",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("📅 ${image.date}", style = MaterialTheme.typography.labelMedium)
                Text("🧪 Status: ${image.status}", style = MaterialTheme.typography.labelSmall)
                Text("🌿 ${image.fruitType}", style = MaterialTheme.typography.bodyMedium)
                if (!image.xyLocation.isNullOrEmpty()) {
                    val xyCoords = parseXYLocation(image.xyLocation)
                    if (xyCoords != null) {
                        Text("📍 Location: (${(xyCoords.x * 100).toInt()}%, ${(xyCoords.y * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }




                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onPreview(image) }) {
                        Text("Preview")
                    }
                    TextButton(onClick = { onAnalyze(image) }) {
                        Text("Analyze")
                    }
                    TextButton(onClick = { onDelete(image) }) {
                        Text("Delete", color = Color.Red)
                    }
                }
            }
        }
    }
}
