package de.nathabee.pomolobee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nathabee.pomolobee.data.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: UserPreferences) : ViewModel() {

    val lastSyncDate: StateFlow<Long?> = prefs.lastSyncDate.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val apiEndpoint: StateFlow<String?> = prefs.getApiEndpoint().stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val syncMode: StateFlow<String?> = prefs.getSyncMode().stateIn(
        viewModelScope, SharingStarted.Eagerly, "local"
    )

    val configPath = prefs.getConfigPath().stateIn(
        viewModelScope, SharingStarted.Eagerly, "/sdcard/PomoloBee/config"
    )

    val mediaEndpoint = prefs.getMediaEndpoint().stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val isDebug = prefs.isDebugEnabled().stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val apiVersion = prefs.getApiVersion().stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    fun updateApiEndpoint(value: String) = viewModelScope.launch {
        prefs.setApiEndpoint(value)
    }

    fun updateSyncMode(value: String) = viewModelScope.launch {
        prefs.setSyncMode(value)
    }

    fun updateConfigPath(value: String) = viewModelScope.launch {
        prefs.setConfigPath(value)
    }

    fun updateMediaEndpoint(value: String) = viewModelScope.launch {
        prefs.setMediaEndpoint(value)
    }

    fun updateDebugMode(enabled: Boolean) = viewModelScope.launch {
        prefs.setDebugEnabled(enabled)
    }

    fun updateApiVersion(version: String) = viewModelScope.launch {
        prefs.setApiVersion(version)
    }

    fun updateLastSync(timestamp: Long) = viewModelScope.launch {
        prefs.updateLastSyncDate(timestamp)
    }
}
