package de.nathabee.pomolobee.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import de.nathabee.pomolobee.cache.OrchardCache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

// Global property delegate for accessing the DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val LAST_SYNC_DATE_KEY = longPreferencesKey("last_sync_date")
        private val SELECTED_FIELD_ID_KEY = intPreferencesKey("selected_field")
        private val SELECTED_ROW_ID_KEY = intPreferencesKey("selected_row")
        private val SYNC_MODE_KEY = stringPreferencesKey("sync_mode")
        private val STORAGE_ROOT_PATH_KEY = stringPreferencesKey("storage_root_path")
        private val API_ENDPOINT_KEY = stringPreferencesKey("api_endpoint")
        private val MEDIA_ENDPOINT_KEY = stringPreferencesKey("media_endpoint")
        private val DEBUG_MODE_KEY = booleanPreferencesKey("debug_enabled")
        private val API_VERSION_KEY = stringPreferencesKey("api_version")
       // private val INIT_DONE_KEY = booleanPreferencesKey("init_done")
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

    // 🌱 Selected Field
    suspend fun saveSelectedRow(rowId: Int) {
        context.dataStore.edit { it[SELECTED_ROW_ID_KEY] = rowId }
    }
    fun getSelectedRowId(): Flow<Int?> =
        context.dataStore.data.map { it[SELECTED_ROW_ID_KEY] }

    // 🌐 Sync Mode
    fun getSyncMode(): Flow<String?> =
        context.dataStore.data.map { it[SYNC_MODE_KEY] }

    suspend fun setSyncMode(value: String) {
        context.dataStore.edit { it[SYNC_MODE_KEY] = value }
    }

    // storage, nullable if not set
    fun getRawStorageRoot(): Flow<String?> =
        context.dataStore.data.map { it[STORAGE_ROOT_PATH_KEY] }

    suspend fun setStorageRoot(path: String) {
        context.dataStore.edit { it[STORAGE_ROOT_PATH_KEY] = path }

        // 🌱 Immediately update in-memory cache
        OrchardCache.currentRootUri = Uri.parse(path)
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

    // do not persist initDone : each Activity will be created with initDone =False
    // and it will go in intScreen for uri test,change if necessary and copy asset if necessary and init cache if necessary
    //fun isInitDone(): Flow<Boolean> =
   //     context.dataStore.data.map { it[INIT_DONE_KEY] ?: false }

    //suspend fun setInitDone(value: Boolean) {
    //    context.dataStore.edit { it[INIT_DONE_KEY] = value }
    //}

    suspend fun initializeDefaultsIfNeeded() {
        val current = context.dataStore.data.first()

        context.dataStore.edit { prefs ->
            if (current[SYNC_MODE_KEY] == null) prefs[SYNC_MODE_KEY] = "local"
            if (current[API_ENDPOINT_KEY] == null) prefs[API_ENDPOINT_KEY] = "http://192.168.178.71:8000/api"
            if (current[MEDIA_ENDPOINT_KEY] == null) prefs[MEDIA_ENDPOINT_KEY] = "http://192.168.178.71:8000/media"
            if (current[DEBUG_MODE_KEY] == null) prefs[DEBUG_MODE_KEY] = false
        }
    }

}