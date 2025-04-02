// OrchardViewModel.kt
package de.nathabee.pomolobee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.repository.OrchardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrchardViewModel : ViewModel() {


    private val _fruitCount = MutableStateFlow(0)
    val fruitCount: StateFlow<Int> = _fruitCount

    private val _fieldCount = MutableStateFlow(0)
    val fieldCount: StateFlow<Int> = _fieldCount

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    fun loadLocalConfig(configPath: String) {
        viewModelScope.launch {
            val success = OrchardRepository.loadAllConfigFromPath(configPath)
            if (success) {
                _fruitCount.value = OrchardCache.fruits.size
                _fieldCount.value = OrchardCache.locations.size
            }
            _syncStatus.value = if (success) "✅ Config loaded" else "❌ Failed to load"
        }
    }

}

