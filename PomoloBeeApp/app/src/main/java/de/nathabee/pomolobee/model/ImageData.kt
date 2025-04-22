package de.nathabee.pomolobee.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ImageRecord(
    @SerialName("image_id") val imageId: Int? = null,
    @SerialName("row_id") val rowId: Int,
    @SerialName("field_id") val fieldId: Int,
    @SerialName("xy_location") val xyLocation: String? = null,
    @SerialName("fruit_type") val fruitType: String,
    @SerialName("user_fruit_plant") val userFruitPlant: Int? = null,
    @SerialName("upload_date") val uploadDate: String? = null,
    @SerialName("date") val date: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("original_filename") val originalFilename: String? = null,
    val processed: Boolean = false,
    @SerialName("processed_at") val processedAt: String? = null,
    val status: String = "pending"
)

@Serializable
data class ImageListResponse(
    val status: String,
    val data: ImageListData
)

@Serializable
data class ImageListData(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val images: List<ImageRecord>
)



