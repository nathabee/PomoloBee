package de.nathabee.pomolobee.model

import com.google.gson.annotations.SerializedName

data class ImageRecord(
    @SerializedName("image_id") val imageId: Int,
    @SerializedName("row_id") val rowId: Int,
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("xy_location") val xyLocation: String?,  //value is a JSON string like {"x":0.42,"y":0.75},
    @SerializedName("fruit_type") val fruitType: String,
    @SerializedName("user_fruit_plant") val userFruitPlant: Int,
    @SerializedName("upload_date") val uploadDate: String,
    @SerializedName("date") val date: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("original_filename") val originalFilename: String?,
    val processed: Boolean,
    @SerializedName("processed_at") val processedAt: String?,
    val status: String
)

data class ImageListResponse(
    val status: String,
    val data: ImageListData
)

data class ImageListData(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val images: List<ImageRecord>
)




