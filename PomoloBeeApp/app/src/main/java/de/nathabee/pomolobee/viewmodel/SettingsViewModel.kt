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
import de.nathabee.pomolobee.util.ErrorLogger
import de.nathabee.pomolobee.util.StorageUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.edit
import de.nathabee.pomolobee.repository.OrchardRepository


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
        return rawPath?.let { Uri.parse(it) }?.takeIf { StorageUtils.hasAccessToUri(context, it) }
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


    fun performConnectionTest(context: Context, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val api = apiEndpoint.value ?: return@launch
        val media = mediaEndpoint.value ?: return@launch

        try {
            val result = ConnectionRepository.testConnection(api, media) { version ->
                updateApiVersion(version)
            }
            onResult(result)
        } catch (e: Exception) {
            ErrorLogger.logError(context, storageRootUri.value, e.message ?: "❌ Unknown connection error", e)
            onResult(false)
        }
    }



    fun performLocalSync(context: Context, onResult: (String) -> Unit) = viewModelScope.launch {
        try {
            val rootUri = storageRootUri.value ?: throw IllegalStateException("❌ Storage root not set")
            val success = OrchardRepository.loadAllConfigFromUri(context, rootUri)
            if (!success) {
                onResult("❌ Failed to load configuration after sync")
                return@launch
            }
            updateLastSync(System.currentTimeMillis())

            onResult("✅ Orchard configuration synced")
        } catch (e: Exception) {
            ErrorLogger.logError(context, storageRootUri.value, e.message ?: "❌ Sync failed", e)
            onResult(e.message ?: "❌ Unknown sync error")
        }
    }


    fun performCloudSync(context: Context, onComplete: (String) -> Unit) = viewModelScope.launch {
        val apiUrl = apiEndpoint.value
        val mediaUrl = mediaEndpoint.value
        val rootUri = storageRootUri.value

        if (apiUrl.isNullOrBlank() || mediaUrl.isNullOrBlank() || rootUri == null) {
            onComplete("❌ Missing API, media endpoint, or storage location")
            return@launch
        }

        try {
            // ✅ Run the connection test first
            val connected = ConnectionRepository.testConnection(apiUrl, mediaUrl) { version ->
                updateApiVersion(version)
            }

            if (!connected) {
                onComplete("❌ Connection test failed — cloud sync aborted")
                return@launch
            }

            // ✅ Proceed with cloud sync
            val result = ConnectionRepository.performCloudSync(context, rootUri, apiUrl, mediaUrl)

            updateLastSync(System.currentTimeMillis())
            onComplete(result)

        } catch (e: Exception) {
            onComplete("❌ Cloud sync failed: ${e.message}")
            ErrorLogger.logError(context, rootUri, "Cloud sync error", e)
        }
    }







    private val _recomposeTrigger = mutableStateOf(0)
    val recomposeTrigger: State<Int> get() = _recomposeTrigger

    fun invalidate() {
        _recomposeTrigger.value++
    }

    fun safeLaunchInViewModel(context: Context, message: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                ErrorLogger.logError(context, storageRootUri.value, message, e)
            }
        }
    }

    init {
        viewModelScope.launch {
            prefs.initializeDefaultsIfNeeded()
        }
    }




}
