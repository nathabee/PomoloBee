package de.nathabee.pomolobee.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import de.nathabee.pomolobee.util.StorageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
 import androidx.lifecycle.ViewModelProvider

class InitViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InitViewModel() as T // context is here if you later need it
    }
}

enum class StartupStatus {
    MissingUri,
    InvalidUri,
    MissingConfig,
    InitConfig,
    Ready
}

class InitViewModel : ViewModel() {

    private val _initDone = MutableStateFlow(false)
    val initDone: StateFlow<Boolean> = _initDone

    private val _startupStatus = MutableStateFlow(StartupStatus.MissingUri)
    val startupStatus: StateFlow<StartupStatus> = _startupStatus

    fun markInitDone() {
        Log.d("InitViewModel", "✅ markInitDone() called — setting Ready")
        _initDone.value = true
        _startupStatus.value = StartupStatus.Ready
    }


    fun refreshStatus(context: Context, uri: Uri?, cacheReady: Boolean) {

        _startupStatus.value = computeStatus(context, uri, cacheReady)
    }

    private fun computeStatus(
        context: Context,
        uri: Uri?,
        cacheIsReady: Boolean
    ): StartupStatus {
        Log.d("InitViewModel", "🔍 Checking URI: $uri")

        if (_initDone.value) {
            Log.d("InitViewModel", "✅ Init already done — forcing status = Ready")
            return StartupStatus.Ready
        }

        if (uri == null) {
            Log.w("InitViewModel", "⚠️ URI is null")
            return StartupStatus.MissingUri
        }

        if (!StorageUtils.hasAccessToUri(context, uri)) {
            Log.e("InitViewModel", "❌ No access to URI: $uri")
            return StartupStatus.InvalidUri
        }

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
}
