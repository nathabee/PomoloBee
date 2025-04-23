package de.nathabee.pomolobee.viewmodel

import PomolobeeViewModels
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
import de.nathabee.pomolobee.sync.SyncManager


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
        val api = apiEndpoint.value
        val media = mediaEndpoint.value
        val rootUri = storageRootUri.value

        if (api.isNullOrBlank() || media.isNullOrBlank()) {
            onResult(false)
            return@launch
        }

        val result = ConnectionRepository.testConnection(context, rootUri, api, media) {
            updateApiVersion(it)
        }

        onResult(result)
    }



    fun performLocalSync(
        context: Context,
        sharedViewModels: PomolobeeViewModels,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val rootUri = storageRootUri.value ?: run {
            onResult(false)
            return@launch
        }

        val success = SyncManager.performLocalSync(context, rootUri, sharedViewModels)
        onResult(success)
    }



    fun performCloudSync(
        context: Context,
        sharedViewModels: PomolobeeViewModels,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val apiUrl = apiEndpoint.value
        val mediaUrl = mediaEndpoint.value
        val rootUri = storageRootUri.value

        if (apiUrl.isNullOrBlank() || mediaUrl.isNullOrBlank() || rootUri == null) {
            ErrorLogger.logError(
                context,
                rootUri,
                "❌ Missing configuration: apiUrl=$apiUrl, mediaUrl=$mediaUrl, rootUri=$rootUri"
            )
            onResult(false)
            return@launch
        }


        val connected = ConnectionRepository.testConnection(context, rootUri, apiUrl, mediaUrl) {
            updateApiVersion(it)
        }

        if (!connected) {
            onResult(false)
            return@launch
        }

        val result = SyncManager.performCloudSync(
            context = context,
            rootUri = rootUri,
            apiUrl = apiUrl,
            mediaUrl = mediaUrl,
            sharedViewModels = sharedViewModels
        )

        onResult(result)
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
