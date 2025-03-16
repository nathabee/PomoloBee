package de.nathabee.pomolobee.data


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object UserPreferences {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    suspend fun savePreference(context: Context, key: String, value: String) {
        context.dataStore.edit { settings -> settings[stringPreferencesKey(key)] = value }
    }

    fun getPreference(context: Context, key: String): Flow<String?> {
        return context.dataStore.data.map { it[stringPreferencesKey(key)] }
    }
}
