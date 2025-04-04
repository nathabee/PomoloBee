package de.nathabee.pomolobee.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.nathabee.pomolobee.data.UserPreferences
import de.nathabee.pomolobee.repository.ConnectionRepository

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import de.nathabee.pomolobee.util.hasAccessToUri
import kotlinx.coroutines.runBlocking


class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val prefs = UserPreferences(context)
        return SettingsViewModel(prefs, context.getExternalFilesDir(null)?.absolutePath + "/PomoloBee") as T

    }
}
class SettingsViewModel(
    private val prefs: UserPreferences,
    private val defaultStorageRoot: String
) : ViewModel() {

    // Add a new StateFlow for the URI
    val storageRootUri: StateFlow<Uri?> = prefs.getRawStorageRoot()
        .map { it?.let { Uri.parse(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    val lastSyncDate: StateFlow<Long?> = prefs.lastSyncDate.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val apiEndpoint: StateFlow<String?> = prefs.getApiEndpoint().stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val syncMode: StateFlow<String?> = prefs.getSyncMode().stateIn(
        viewModelScope, SharingStarted.Eagerly, "local"
    )


    val configDirectory: StateFlow<Uri?> = storageRootUri.map { root ->
        root?.let { Uri.withAppendedPath(it, "config") }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


    val imageDirectory: StateFlow<Uri?> = storageRootUri.map { root ->
        root?.let { Uri.withAppendedPath(it, "images") }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)





    val mediaEndpoint = prefs.getMediaEndpoint().stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val isDebug = prefs.isDebugEnabled().stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val apiVersion = prefs.getApiVersion().stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )


    val selectedFieldId: StateFlow<Int?> = prefs.getSelectedFieldId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val selectedRowId: StateFlow<Int?> = prefs.getSelectedRowId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)




    fun setStorageRoot(uri: Uri) = viewModelScope.launch {
        prefs.setStorageRoot(uri.toString())
    }


    fun updateMediaEndpoint(value: String) = viewModelScope.launch {
        prefs.setMediaEndpoint(value)
    }

    fun updateApiEndpoint(value: String) = viewModelScope.launch {
        prefs.setApiEndpoint(value)
    }

    fun updateSyncMode(value: String) = viewModelScope.launch {
        prefs.setSyncMode(value)
    }

    fun clearStorageRoot() = viewModelScope.launch {
        prefs.setStorageRoot("") // or null, depending on your implementation
    }

    // check URI in blocking mode from preference and check for access : at start to see if initialisation must be done
    fun getStartupStorageUri(context: Context): Uri? {
        val rawPath = runBlocking {
            prefs.getRawStorageRoot().first() // <== direct read of latest value
        }
        return rawPath?.let { Uri.parse(it) }?.takeIf { hasAccessToUri(context, it) }
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

    fun updateSelectedField(fieldId: Int) = viewModelScope.launch {
        prefs.saveSelectedField(fieldId)
    }

    fun updateSelectedRow(rowId: Int) = viewModelScope.launch {
        prefs.saveSelectedRow(rowId)
    }


    fun performConnectionTest(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val api = apiEndpoint.value ?: return@launch
        val media = mediaEndpoint.value ?: return@launch

        val result = ConnectionRepository.testConnection(api, media) { version ->
            updateApiVersion(version)
        }
        onResult(result)
    }

    fun performLocalSync(context: Context, onMessage: (String) -> Unit) = viewModelScope.launch {
        val configUri = configDirectory.value
        if (configUri != null) {
            val message = ConnectionRepository.syncOrchard(context, configUri)
            updateLastSync(System.currentTimeMillis())
            onMessage(message)
        } else {
            onMessage("❌ Config path missing")
        }
    }


    private val _recomposeTrigger = mutableStateOf(0)
    val recomposeTrigger: State<Int> get() = _recomposeTrigger

    fun invalidate() {
        _recomposeTrigger.value++
    }


}
