package de.nathabee.pomolobee.viewmodel

import PomolobeeViewModels
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.data.UserPreferences
import de.nathabee.pomolobee.repository.ConnectionRepository
import de.nathabee.pomolobee.sync.SyncManager
import de.nathabee.pomolobee.util.ErrorLogger
import de.nathabee.pomolobee.util.StorageUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val prefs = UserPreferences(context)
        return SettingsViewModel(prefs, context) as T
    }
}

enum class StartupStatus {
    MissingUri,
    InvalidUri,
    MissingConfig,
    InitConfig,
    Ready
}

class SettingsViewModel(
    private val prefs: UserPreferences,
    private val context: Context
) : ViewModel() {

    val storageRootUri: StateFlow<Uri?> = prefs.getRawStorageRoot()
        .map { it?.let { Uri.parse(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _startupStatus = MutableStateFlow(StartupStatus.MissingUri)
    val startupStatus: StateFlow<StartupStatus> = _startupStatus

    private val _initDone = MutableStateFlow(false)
    val initDone: StateFlow<Boolean> = _initDone

    fun updateStartupStatus(isCacheReady: Boolean) {
        val uri = storageRootUri.value
        _startupStatus.value = computeStatus(uri, isCacheReady)
    }

    private fun computeStatus(uri: Uri?, cacheIsReady: Boolean): StartupStatus {
        Log.d("SettingsViewModel", "🔍 Checking URI: $uri")

        if (_initDone.value) {
            if (uri == null || !StorageUtils.hasAccessToUri(context, uri)) {
                Log.w("SettingsViewModel", "⚠️ Storage was lost! Forcing re-init.")
                _initDone.value = false
                return StartupStatus.InvalidUri
            }
            return StartupStatus.Ready
        }

        if (uri == null) return StartupStatus.MissingUri
        if (!StorageUtils.hasAccessToUri(context, uri)) return StartupStatus.InvalidUri

        val docFile = DocumentFile.fromTreeUri(context, uri)
        val configDir = docFile?.findFile("config")
        val fruits = configDir?.findFile("fruits.json")
        val locations = configDir?.findFile("locations.json")

        return when {
            fruits == null || locations == null -> StartupStatus.MissingConfig
            !cacheIsReady -> StartupStatus.InitConfig
            else -> StartupStatus.Ready
        }
    }


    fun markInitDone() {
        _initDone.value = true
        _startupStatus.value = StartupStatus.Ready
    }


    // init is done by factorizing view in mainActivity, we set isDone to true here if uri acessible and cache not empty

    init {
        viewModelScope.launch {
            prefs.initializeDefaultsIfNeeded()

            val rawUri = prefs.getRawStorageRoot().firstOrNull()
            val uri = rawUri?.let { Uri.parse(it) }

            if (uri != null && StorageUtils.hasAccessToUri(context, uri)) {
                OrchardCache.setRootUri(uri)
                Log.d("SettingsViewModel", "✅ Preloaded valid URI into OrchardCache: $uri")
            } else {
                Log.w("SettingsViewModel", "❌ URI found in prefs but access denied: $uri")
            }

            val cacheReady = OrchardCache.locations.isNotEmpty()
            updateStartupStatus(cacheReady)

            // ✅ 👇 After updating status, decide if ready
            if (computeStatus(uri, cacheReady) == StartupStatus.Ready) {
                markInitDone()
                Log.d("SettingsViewModel", "✅ Storage and config valid — marking init done")
            } else {
                Log.d("SettingsViewModel", "ℹ️ Init not ready yet")
            }
        }
    }






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








    fun updateDebugMode(enabled: Boolean) = viewModelScope.launch {
        prefs.setDebugEnabled(enabled)
    }

    fun updateApiVersion(version: String) = viewModelScope.launch {
        prefs.setApiVersion(version)
    }

    fun updateLastSync(timestamp: Long) = viewModelScope.launch {
        prefs.updateLastSyncDate(timestamp)
    }

    /*
    fun updateSelectedField(fieldId: Int) = viewModelScope.launch {
        prefs.saveSelectedField(fieldId)
    }

    fun updateSelectedRow(rowId: Int) = viewModelScope.launch {
        prefs.saveSelectedRow(rowId)
    }

     */


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







}
