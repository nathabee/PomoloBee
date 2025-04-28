package de.nathabee.pomolobee.ui.preview


import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.model.Estimation
import de.nathabee.pomolobee.model.Field
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.ui.component.RowCardWithFruitEditor



class FruitPreviewProvider : PreviewParameterProvider<Fruit> {
    override val values = sequenceOf(
        Fruit(
            fruitId = 1,
            shortName = "Swing_CG1",
            name = "Cultivar Swing on CG1",
            description = "Late harvest, sweet, crisp texture, medium storage (3-4 months), aromatic",
            yieldStartDate = "2025-09-15",
            yieldEndDate = "2025-10-05",
            yieldAvgKg = 40f,
            fruitAvgKg = 0.2f
        )
    )
}

class ImageRecordPreviewProvider : PreviewParameterProvider<ImageRecord> {
    override val values = sequenceOf(
        ImageRecord(
            imageId = 123,
            fieldId = 1,
            rowId = 11,
            xyLocation = "{\"x\":0.5,\"y\":0.5}",
            fruitType = "Cultivar Gallwa",
            userFruitPlant = 20,
            uploadDate = "2025-04-20",
            date = "2025-04-18",
            imageUrl = "",
            originalFilename = "sample.jpg",
            processed = false,
            processedAt = null,
            status = "pending"
        )
    )
}


class ImageEstimationProvider : PreviewParameterProvider<Pair<ImageRecord, Estimation>> {
    override val values = sequenceOf(
        Pair(
            ImageRecord(
                imageId = 123,
                fieldId = 1,
                rowId = 11,
                xyLocation = "{\"x\":0.5,\"y\":0.5}",
                fruitType = "Cultivar Gallwa",
                userFruitPlant = 20,
                uploadDate = "2025-04-20",
                date = "2025-04-18",
                imageUrl = "",
                originalFilename = "sample.jpg",
                processed = true,
                processedAt = "2025-04-20T12:00:00",
                status = "done"
            ),
            Estimation(
                estimationId = 1,
                imageId = 123,
                date = "2025-04-18",
                timestamp = "2025-04-18T12:00:00",
                rowId = 11,
                rowName = "Rang 11",
                fieldId = 1,
                fieldName = "ChampMaison",
                fruitType = "Cultivar Gallwa",
                plantKg = 18f,
                rowKg = 702f,
                maturationGrade = 2,
                confidenceScore = 0.85f,
                source = "Machine Learning (Image)",
                fruitPlant = 100,
                status = "done"
            )
        )
    )
}

class EstimationPreviewProvider : PreviewParameterProvider<Estimation> {
    override val values = sequenceOf(
        Estimation(
            estimationId = 1,
            imageId = 1,
            date = "2025-04-18",
            timestamp = "2025-04-18T12:00:00",
            rowId = 15,
            rowName = "Rang 15",
            fieldId = 1,
            fieldName = "ChampMaison",
            fruitType = "Cultivar Pitch on M9",
            plantKg = 18f,
            rowKg = 702f,
            maturationGrade = 2,
            confidenceScore = 0.85f,
            source = "Machine Learning (Image)",
            fruitPlant = 100,
            status = "done"
        )
    )
}



// --- Row Preview Provider ---
class RowPreviewProvider : PreviewParameterProvider<Row> {
    override val values = sequenceOf(
        Row(
            rowId = 1,
            shortName = "R1",
            name = "Rang 1 maison",
            nbPlant = 38,
            fruitId = 1,
            fruitType = "Cultivar Swing on CG1"
        )
    )
}




class FieldWithFruitsPreviewProvider : PreviewParameterProvider<Pair<Location, List<Fruit>>> {
    override val values = sequenceOf(
        Pair(
            Location(
                field = Field(
                    fieldId = 1,
                    shortName = "C1",
                    name = "ChampMaison",
                    description = "Champ situé sur la parcelle de la maison",
                    orientation = "NW",
                    svgMapUrl = null,
                    backgroundImageUrl = null
                ),
                rows = listOf(
                    Row(
                        rowId = 1,
                        shortName = "R1",
                        name = "Rang 1 maison",
                        nbPlant = 38,
                        fruitId = 1,
                        fruitType = "Cultivar Swing on CG1"
                    ),
                    Row(
                        rowId = 2,
                        shortName = "R2",
                        name = "Rang 2 maison",
                        nbPlant = 40,
                        fruitId = 2,
                        fruitType = "Cultivar Swing on CG1"
                    )
                )
            ),
            listOf(
                    Fruit(
                        fruitId = 1,
                        shortName = "Swing_CG1",
                        name = "Cultivar Swing on CG1",
                        description = "Late harvest, sweet, crisp texture, medium storage (3-4 months), aromatic",
                        yieldStartDate = "2025-09-15",
                        yieldEndDate = "2025-10-05",
                        yieldAvgKg = 40f,
                        fruitAvgKg = 0.2f
                    ),
                    Fruit(
                        fruitId = 2,
                        shortName = "Ladina_CG1",
                        name = "Cultivar Ladina on CG1",
                        description = "Mid-late harvest, balanced sweetness and acidity, long storage (5-6 months), juicy",
                        yieldStartDate = "2025-09-25",
                        yieldEndDate = "2025-10-15",
                        yieldAvgKg = 35f,
                        fruitAvgKg = 0.22f
                    ),
                    Fruit(
                        fruitId = 3,
                        shortName = "Gallwa_CG1",
                        name = "Cultivar Gallwa on CG1",
                        description = "Mid-season, high sweetness, slightly tangy, firm flesh, long storage (6+ months)",
                        yieldStartDate = "2025-09-15",
                        yieldEndDate = "2025-10-05",
                        yieldAvgKg = 45f,
                        fruitAvgKg = 0.24f
                    ),
                    Fruit(
                        fruitId = 4,
                        shortName = "Pitch_M9",
                        name = "Cultivar Pitch on M9",
                        description = "Early harvest, very sweet, soft flesh, short storage (2-3 months), suitable for fresh eating",
                        yieldStartDate = "2025-09-01",
                        yieldEndDate = "2025-09-25",
                        yieldAvgKg = 55f,
                        fruitAvgKg = 0.18f
                    ),
                    Fruit(
                        fruitId = 5,
                        shortName = "Pixie_CG1",
                        name = "Cultivar Pixie on CG1",
                        description = "Late-season, aromatic, slight bitterness, good for cider, long storage (6+ months)",
                        yieldStartDate = "2025-09-25",
                        yieldEndDate = "2025-10-15",
                        yieldAvgKg = 30f,
                        fruitAvgKg = 0.16f
                    ),
                    Fruit(
                        fruitId = 6,
                        shortName = "EarlyCrunch_M9_Nakab",
                        name = "Cultivar Early Crunch on M9 Nakab",
                        description = "Very early harvest, crunchy, high acidity, short storage (1-2 months), refreshing taste",
                        yieldStartDate = "2025-08-05",
                        yieldEndDate = "2025-08-25",
                        yieldAvgKg = 50f,
                        fruitAvgKg = 0.17f
                    )
                )

            )
        )

}


class FruitListPreviewProvider : PreviewParameterProvider<List<Fruit>> {
    override val values = sequenceOf(
        listOf(
            Fruit(
                fruitId = 1,
                shortName = "Swing_CG1",
                name = "Cultivar Swing on CG1",
                description = "Late harvest, sweet, crisp texture, medium storage (3-4 months), aromatic",
                yieldStartDate = "2025-09-15",
                yieldEndDate = "2025-10-05",
                yieldAvgKg = 40f,
                fruitAvgKg = 0.2f
            ),
            Fruit(
                fruitId = 2,
                shortName = "Ladina_CG1",
                name = "Cultivar Ladina on CG1",
                description = "Sweet and aromatic cultivar, medium storage life.",
                yieldStartDate = "2025-09-10",
                yieldEndDate = "2025-09-30",
                yieldAvgKg = 35f,
                fruitAvgKg = 0.18f
            )
        )
    )
}



class RowAndFruitPreviewProvider : PreviewParameterProvider<Pair<Row, Fruit>> {
    override val values = sequenceOf(
        Pair(
            Row(
                rowId = 1,
                shortName = "R1",
                name = "Rang 1",
                nbPlant = 40,
                fruitId = 1,
                fruitType = "Cultivar Swing on CG1"
            ),
            Fruit(
                fruitId = 1,
                shortName = "Swing_CG1",
                name = "Cultivar Swing on CG1",
                description = "Late harvest, sweet, crisp texture, medium storage (3-4 months), aromatic",
                yieldStartDate = "2025-09-15",
                yieldEndDate = "2025-10-05",
                yieldAvgKg = 40f,
                fruitAvgKg = 0.2f
            )
        )
    )
}