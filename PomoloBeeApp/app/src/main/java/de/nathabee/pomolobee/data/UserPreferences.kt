package de.nathabee.pomolobee.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Global property delegate for accessing the DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val LAST_SYNC_DATE_KEY = longPreferencesKey("last_sync_date")
        private val SELECTED_FIELD_ID_KEY = intPreferencesKey("selected_field")

        val SYNC_MODE_KEY = stringPreferencesKey("sync_mode")
        val IMAGE_PATH_KEY = stringPreferencesKey("image_path")
        val CONFIG_PATH_KEY = stringPreferencesKey("config_path")
        val API_ENDPOINT_KEY = stringPreferencesKey("api_endpoint")
        val MEDIA_ENDPOINT_KEY = stringPreferencesKey("media_endpoint")
        val DEBUG_MODE_KEY = booleanPreferencesKey("debug_enabled")
        val API_VERSION_KEY = stringPreferencesKey("api_version")


    }


    // 🔁 Generic String Prefs
    fun getPreference(key: String): Flow<String?> =
        context.dataStore.data.map { it[stringPreferencesKey(key)] }

    suspend fun savePreference(key: String, value: String) {
        context.dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    // 🕒 Last Sync
    val lastSyncDate: Flow<Long?> = context.dataStore.data
        .map { it[LAST_SYNC_DATE_KEY] }

    suspend fun updateLastSyncDate(timestamp: Long) {
        context.dataStore.edit { it[LAST_SYNC_DATE_KEY] = timestamp }
    }

    // 🌱 Selected Field
    suspend fun saveSelectedField(fieldId: Int) {
        context.dataStore.edit { it[SELECTED_FIELD_ID_KEY] = fieldId }
    }

    fun getSelectedFieldId(): Flow<Int?> =
        context.dataStore.data.map { it[SELECTED_FIELD_ID_KEY] }

    // 🌐 Sync Mode
    fun getSyncMode(): Flow<String?> =
        context.dataStore.data.map { it[SYNC_MODE_KEY] }

    suspend fun setSyncMode(value: String) {
        context.dataStore.edit { it[SYNC_MODE_KEY] = value }
    }

    // 📂 Image Path
    fun getImagePath(): Flow<String?> =
        context.dataStore.data.map { it[IMAGE_PATH_KEY] }

    suspend fun setImagePath(value: String) {
        context.dataStore.edit { it[IMAGE_PATH_KEY] = value }
    }

    // ⚙ Config Path
    fun getConfigPath(): Flow<String?> =
        context.dataStore.data.map { it[CONFIG_PATH_KEY] }

    suspend fun setConfigPath(value: String) {
        context.dataStore.edit { it[CONFIG_PATH_KEY] = value }
    }

    // 🌍 API Endpoint
    fun getApiEndpoint(): Flow<String?> =
        context.dataStore.data.map { it[API_ENDPOINT_KEY] }

    suspend fun setApiEndpoint(value: String) {
        context.dataStore.edit { it[API_ENDPOINT_KEY] = value }
    }

    // 🖼 Media Endpoint
    fun getMediaEndpoint(): Flow<String?> =
        context.dataStore.data.map { it[MEDIA_ENDPOINT_KEY] }

    suspend fun setMediaEndpoint(value: String) {
        context.dataStore.edit { it[MEDIA_ENDPOINT_KEY] = value }
    }

    // 🐞 Debug Mode
    fun isDebugEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[DEBUG_MODE_KEY] ?: false }

    suspend fun setDebugEnabled(value: Boolean) {
        context.dataStore.edit { it[DEBUG_MODE_KEY] = value }
    }

    fun getApiVersion(): Flow<String?> =
        context.dataStore.data.map { it[API_VERSION_KEY] }

    suspend fun setApiVersion(version: String) {
        context.dataStore.edit { it[API_VERSION_KEY] = version }

    }
}