package de.nathabee.pomolobee.model

import com.google.gson.annotations.SerializedName

data class Estimation(
    @SerializedName("estimation_id") val estimationId: Int,
    @SerializedName("image_id") val imageId: Int,
    val date: String,
    val timestamp: String,
    @SerializedName("row_id") val rowId: Int,
    @SerializedName("row_name") val rowName: String,
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("field_name") val fieldName: String,
    @SerializedName("fruit_type") val fruitType: String,
    @SerializedName("plant_kg") val plantKg: Float,
    @SerializedName("row_kg") val rowKg: Float,
    @SerializedName("maturation_grade") val maturationGrade: Int,
    @SerializedName("confidence_score") val confidenceScore: Float,
    val source: String,
    @SerializedName("fruit_plant") val fruitPlant: Int,
    val status: String
)

data class EstimationResponse(
    val status: String,
    val data: EstimationData
)

data class EstimationData(
    val estimations: List<Estimation>
)
