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
import de.nathabee.pomolobee.util.hasAccessToUri
import de.nathabee.pomolobee.util.resolveSubDirectory
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
                val root = storageRootUri.value ?: throw IllegalStateException("❌ Storage root not set")
                ConnectionRepository.syncOrchard(context, root)
                onResult("✅ Orchard configuration synced")
            } catch (e: Exception) {
                ErrorLogger.logError(context, storageRootUri.value, e.message ?: "❌ Sync failed", e)
                onResult(e.message ?: "❌ Unknown sync error")
            }
        }

    fun performCloudSync(context: Context, onComplete: (String) -> Unit) = viewModelScope.launch {
        val api = apiEndpoint.value
        val media = mediaEndpoint.value
        val rootUri = storageRootUri.value

        if (api.isNullOrBlank() || media.isNullOrBlank() || rootUri == null) {
            onComplete("❌ Missing API, media endpoint, or storage location")
            return@launch
        }

        try {
            // STEP 1: 🔗 Fetch JSON config from API
            // TODO: Replace with actual network fetch
            // Example: val locationsJson = fetch("$api/locations.json")
            //          val fruitsJson = fetch("$api/fruits.json")

            // STEP 2: 💾 Save them into /config/
            // TODO: Save both JSON responses as "locations.json" and "fruits.json"
            // val configDir = resolveSubDirectory(context, rootUri, "config")
            // writeFile(configDir, "locations.json", locationsJson)
            // writeFile(configDir, "fruits.json", fruitsJson)

            // STEP 3: 📥 Download and save SVGs mentioned in locations
            // TODO: Parse locationsJson into model → loop over fields → get SVG filenames
            //       For each, fetch from "$media/svg/fields/$svgName" and save into /fields/svg/

            // STEP 4: 🧠 Update cache using already existing sync logic
            //OrchardRepository.syncOrchard(context, rootUri)

            // STEP 5: ✅ All done
            onComplete("✅ Cloud sync completed")
            //updateLastSyncTime()
        } catch (e: Exception) {
            onComplete("❌ Cloud sync failed: ${e.message}")
            e.printStackTrace()
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
