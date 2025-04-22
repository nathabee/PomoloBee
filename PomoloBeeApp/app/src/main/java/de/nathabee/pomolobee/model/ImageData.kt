package de.nathabee.pomolobee.model


import com.google.gson.annotations.SerializedName

data class ImageRecord(
    @SerializedName("image_id") val imageId: Int? = null,
    @SerializedName("row_id") val rowId: Int,
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("xy_location") val xyLocation: String? = null,
    @SerializedName("fruit_type") val fruitType: String,
    @SerializedName("user_fruit_plant") val userFruitPlant: Int? = null,
    @SerializedName("upload_date") val uploadDate: String,
    val date: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("original_filename") val originalFilename: String? = null,
    val processed: Boolean,
    @SerializedName("processed_at") val processedAt: String? = null,
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



