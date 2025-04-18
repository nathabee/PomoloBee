
// ImageViewModel.kt	Used in Result/Preview screen — holds current image + metadata (e.g. analysis result, row info).
package de.nathabee.pomolobee.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.Estimation
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.repository.ImageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ImageViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImageViewModel(context) as T
    }
}

class ImageViewModel(private val context: Context) : ViewModel() {

    private val _images = MutableStateFlow<List<ImageRecord>>(emptyList())
    val images: StateFlow<List<ImageRecord>> = _images

    private val _estimations = MutableStateFlow<List<Estimation>>(emptyList())
    val estimations: StateFlow<List<Estimation>> = _estimations

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    fun loadLocalImageData(rootUri: Uri) {
        viewModelScope.launch {
            val success = ImageRepository.loadAllImageDataFromUri(context, rootUri)
            if (success) {
                _images.value = OrchardCache.images
                _estimations.value = OrchardCache.estimations
                _syncStatus.value = "✅ Image data loaded"
            } else {
                _syncStatus.value = "❌ Failed to load image data"
            }
        }
    }

    // ImageViewModel.kt (Add these inside your ViewModel class)

    private val _selectedFieldId = MutableStateFlow<Int?>(null)
    val selectedFieldId: StateFlow<Int?> = _selectedFieldId

    private val _selectedRowId = MutableStateFlow<Int?>(null)
    val selectedRowId: StateFlow<Int?> = _selectedRowId

    fun selectField(fieldId: Int?) {
        _selectedFieldId.value = fieldId
        _selectedRowId.value = null // reset row when field changes
    }

    fun selectRow(rowId: Int?) {
        _selectedRowId.value = rowId
    }

    val filteredImages: StateFlow<List<ImageRecord>> = combine(images, selectedFieldId, selectedRowId) { images, fieldId, rowId ->
        images.filter { image ->
            (fieldId == null || image.fieldId == fieldId) &&
                    (rowId == null || image.rowId == rowId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingXYLocation = MutableStateFlow<String?>(null)
    val pendingXYLocation: StateFlow<String?> = _pendingXYLocation

    fun setPendingXYLocation(location: String?) {
        _pendingXYLocation.value = location
    }


}
