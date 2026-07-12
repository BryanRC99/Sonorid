// app/src/main/java/com/example/sonorid/data/local/UserPreferencesRepository.kt
package com.example.sonorid.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_FOLDERS = stringSetPreferencesKey("selected_folders")
    }

    /** Vacío = "todas las carpetas" */
    val selectedFolders: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[Keys.SELECTED_FOLDERS] ?: emptySet() }

    suspend fun setSelectedFolders(folders: Set<String>) {
        context.dataStore.edit { it[Keys.SELECTED_FOLDERS] = folders }
    }
}