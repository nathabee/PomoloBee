package de.nathabee.pomolobee.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import java.text.SimpleDateFormat
import java.util.Locale
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModelProvider


class CameraViewModelFactory( ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CameraViewModel( ) as T
    }
}

class CameraViewModel : ViewModel() {

    private val _tempImageRecord = MutableStateFlow(createEmptyImageRecord())
    val tempImageRecord: StateFlow<ImageRecord> = _tempImageRecord

    val selectedFieldId: Int?
        get() = _tempImageRecord.value.fieldId.takeIf { it != -1 }

    val selectedRowId: Int?
        get() = _tempImageRecord.value.rowId.takeIf { it != -1 }

    fun setTempImageRecord(record: ImageRecord) {
        _tempImageRecord.value = record
    }

    fun clearTempImageRecord() {
        _tempImageRecord.value = createEmptyImageRecord()
    }

    fun updateAfterPicture(uri: Uri, fieldShort: String, rowShort: String, xyLocation: String?) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val filename = "${fieldShort}_${rowShort}_$timestamp.jpg"

        _tempImageRecord.value = _tempImageRecord.value.copy(
            imageUrl = uri.toString(),
            originalFilename = filename,
            xyLocation = xyLocation,
            date = getTodayDateString(),
            status = "pending"
        )
    }

    fun updateSelectedFieldAndRow(fieldId: Int, rowId: Int?, fieldShort: String, rowShort: String, fruitType: String?) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val filename = "${fieldShort}_${rowShort}_$timestamp.jpg"

        _tempImageRecord.value = _tempImageRecord.value.copy(
            fieldId = fieldId,
            rowId = rowId ?: -1,
            originalFilename = filename,
            fruitType = fruitType ?: "Unknown"


        )
    }



    fun updatePendingXYLocation(xy: String) {
        _tempImageRecord.value = _tempImageRecord.value.copy(xyLocation = xy)
    }

    fun updateCaptureDate(newDate: String) {
        _tempImageRecord.value = _tempImageRecord.value.copy(date = newDate)
    }

    fun updateUserFruitPerPlant(newCount: Int) {
        _tempImageRecord.value = _tempImageRecord.value.copy(userFruitPlant = newCount)
    }

    private fun createEmptyImageRecord(): ImageRecord {
        return ImageRecord(
            imageId = null,
            fieldId = -1,        // force to select
            rowId = -1,          // force to select
            xyLocation = null,
            fruitType = "Unknown",    // Safe fallback
            userFruitPlant = null,
            uploadDate = "",
            date = getTodayDateString(),
            imageUrl = "",
            originalFilename = null,
            processed = false,
            processedAt = null,
            status = "unsaved"         // Safe initial status
        )
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())
    }

    fun canSaveRecord(): Boolean {
        val record = tempImageRecord.value
        return record.rowId != -1 && record.fieldId != -1 && record.date.isNotBlank() && !record.originalFilename.isNullOrBlank()
    }
}
