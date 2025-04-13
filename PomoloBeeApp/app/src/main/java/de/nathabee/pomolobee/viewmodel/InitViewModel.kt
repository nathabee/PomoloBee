package de.nathabee.pomolobee.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import de.nathabee.pomolobee.util.hasAccessToUri

enum class StartupStatus {
    MissingUri,
    InvalidUri,
    MissingConfig,
    Ready
}

class InitViewModel : ViewModel() {

    /**
     * Evaluates startup status based on a given storage Uri.
     */
    fun getStartupStatusFromUri(context: Context, uri: Uri?): StartupStatus {
        if (uri == null) {
            Log.w("StartupCheck", "📂 No URI provided")
            return StartupStatus.MissingUri
        }

        if (!hasAccessToUri(context, uri)) {
            Log.e("StartupCheck", "❌ No access to URI: $uri")
            return StartupStatus.InvalidUri
        }

        val docFile = DocumentFile.fromTreeUri(context, uri)
        val configDir = docFile?.findFile("config")
        val fruits = configDir?.findFile("fruits.json")
        val locations = configDir?.findFile("locations.json")

        return if (fruits != null && locations != null) {
            Log.i("StartupCheck", "✅ Config ready at $uri")
            StartupStatus.Ready
        } else {
            Log.w("StartupCheck", "⚠️ Config incomplete at $uri")
            StartupStatus.MissingConfig
        }
    }
}
