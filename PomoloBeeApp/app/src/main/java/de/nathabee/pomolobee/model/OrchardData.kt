package de.nathabee.pomolobee.model

import com.google.gson.annotations.SerializedName

//*****************************************************************************
// FIELD (inside FieldWithRows now)
//*****************************************************************************

data class Field(
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("short_name") val shortName: String,
    val name: String,
    val description: String,
    val orientation: String,
    @SerializedName("svg_map_url") val svgMapUrl: String?,
    @SerializedName("background_image_url") val backgroundImageUrl: String?
)


//*****************************************************************************
// LOCATION
//*****************************************************************************

data class Location(
    val field: Field,
    val rows: List<Row>
)

data class Row(
    @SerializedName("row_id") val rowId: Int,
    @SerializedName("short_name") val shortName: String,
    val name: String,
    @SerializedName("nb_plant") val nbPlant: Int,
    @SerializedName("fruit_id") val fruitId: Int,
    @SerializedName("fruit_type") val fruitType: String
)

// Wrapper for API response
data class LocationResponse(
    val status: String,
    val data: LocationData
)

data class LocationData(
    val locations: List<Location>
)


//*****************************************************************************
// FRUIT
//*****************************************************************************

data class FruitResponse(
    val status: String,
    val data: FruitData
)

data class FruitData(
    val fruits: List<FruitType>
)

data class FruitType(
    @SerializedName("fruit_id") val fruitId: Int,
    @SerializedName("short_name") val shortName: String,
    val name: String,
    val description: String,
    @SerializedName("yield_start_date") val yieldStartDate: String,
    @SerializedName("yield_end_date") val yieldEndDate: String,
    @SerializedName("yield_avg_kg") val yieldAvgKg: Float,
    @SerializedName("fruit_avg_kg") val fruitAvgKg: Float
)
