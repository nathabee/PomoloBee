// SyncManager.kt
package de.nathabee.pomolobee.sync

import PomolobeeViewModels
import android.content.Context
import android.net.Uri
import de.nathabee.pomolobee.repository.ConnectionRepository
import de.nathabee.pomolobee.util.ErrorLogger

object SyncManager {


    suspend fun performLocalSync(
        context: Context,
        rootUri: Uri,
        sharedViewModels: PomolobeeViewModels
    ): Boolean {
        val orchardSuccess = sharedViewModels.orchard.loadConfigFromStorage(rootUri, context)
        if (!orchardSuccess) {
            ErrorLogger.logError(context, rootUri, "❌ Failed to load orchard config")
            return false
        }

        val imageSuccess = sharedViewModels.image.loadImageCacheFromStorage(rootUri)
        if (!imageSuccess) {
            ErrorLogger.logError(
                context,
                rootUri,
                "⚠️ Orchard config loaded, but image load failed"
            )
            return false
        }

        sharedViewModels.settings.updateLastSync(System.currentTimeMillis())
        return true
    }

    suspend fun performCloudSync(
        context: Context,
        rootUri: Uri,
        apiUrl: String,
        mediaUrl: String,
        sharedViewModels: PomolobeeViewModels
    ): Boolean {
        val cloudResult = ConnectionRepository.performCloudSync(context, rootUri, apiUrl, mediaUrl)

        if (!cloudResult) {
            ErrorLogger.logError(context, rootUri, "❌ Failed during cloud data fetch or save")
            return false
        }

        val orchardSuccess = sharedViewModels.orchard.loadConfigFromStorage(rootUri, context)
        if (!orchardSuccess) {
            ErrorLogger.logError(context, rootUri, "⚠️ Cloud sync: Failed to reload orchard config")
            return false
        }

        val imageSuccess = sharedViewModels.image.loadImageCacheFromStorage(rootUri)
        if (!imageSuccess) {
            ErrorLogger.logError(context, rootUri, "⚠️ Cloud sync: Config loaded, but image data reload failed")
            return false
        }

        sharedViewModels.settings.updateLastSync(System.currentTimeMillis())
        return true
    }



}