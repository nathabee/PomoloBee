// OrchardViewModel.kt
package de.nathabee.pomolobee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.repository.OrchardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import de.nathabee.pomolobee.data.UserPreferences
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Fruit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrchardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //val prefs = UserPreferences(context)
        //return OrchardViewModel(prefs, context.getExternalFilesDir(null)?.absolutePath + "/PomoloBee") as T
        return OrchardViewModel() as T

    }
}

class OrchardViewModel : ViewModel() {


    private val _fruitCount = MutableStateFlow(0)
    val fruitCount: StateFlow<Int> = _fruitCount

    private val _fieldCount = MutableStateFlow(0)
    val fieldCount: StateFlow<Int> = _fieldCount

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    private val _fruits = MutableStateFlow<List<Fruit>>(emptyList())
    val fruits: StateFlow<List<Fruit>> = _fruits



    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    suspend fun loadConfigFromStorage(rootUri: Uri, context: Context): Boolean {
        val success = withContext(Dispatchers.IO) {
            OrchardRepository.loadAllConfigFromUri(context, rootUri)
        }

        if (success) {
            _fruitCount.value = OrchardCache.fruits.size
            _fieldCount.value = OrchardCache.locations.size
            _locations.value = OrchardCache.locations
            _fruits.value = OrchardCache.fruits
        }

        _syncStatus.value = if (success) "✅ Config loaded" else "❌ Failed to load"
        return success
    }



}

