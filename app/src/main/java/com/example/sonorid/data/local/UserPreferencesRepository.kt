// app/src/main/java/com/example/sonorid/data/local/UserPreferencesRepository.kt
package com.example.sonorid.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.sonorid.domain.model.AlbumSortOption
import com.example.sonorid.domain.model.ArtistSortOption
import com.example.sonorid.domain.model.AudioEffectsPreferences
import com.example.sonorid.domain.model.PlaylistSongSortOption
import com.example.sonorid.domain.model.SongSortOption
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
        val SONGS_SORT = stringPreferencesKey("songs_sort")
        val ALBUMS_SORT = stringPreferencesKey("albums_sort")
        val ARTISTS_SORT = stringPreferencesKey("artists_sort")
        val PLAYLIST_SORT = stringPreferencesKey("playlist_sort")

        val AUDIO_FX_ENABLED = booleanPreferencesKey("audiofx_enabled")
        val AUDIO_FX_PRESET = intPreferencesKey("audiofx_preset")
        val AUDIO_FX_BANDS = stringPreferencesKey("audiofx_bands") // niveles separados por coma
        val AUDIO_FX_BASS = intPreferencesKey("audiofx_bass")
        val AUDIO_FX_VIRTUALIZER = intPreferencesKey("audiofx_virtualizer")
    }

    val selectedFolders: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[Keys.SELECTED_FOLDERS] ?: emptySet() }

    suspend fun setSelectedFolders(folders: Set<String>) {
        context.dataStore.edit { it[Keys.SELECTED_FOLDERS] = folders }
    }

    // Preferencias de efectos de audio: se guardan como un solo bloque
    // porque siempre se leen/escriben juntas (evita múltiples llamadas
    // secuenciales a DataStore cada vez que el usuario mueve un slider).
    val audioEffectsPreferences: Flow<AudioEffectsPreferences> = context.dataStore.data.map { prefs ->
        AudioEffectsPreferences(
            enabled = prefs[Keys.AUDIO_FX_ENABLED] ?: false,
            presetIndex = prefs[Keys.AUDIO_FX_PRESET] ?: -1,
            bandLevels = prefs[Keys.AUDIO_FX_BANDS]
                ?.takeIf { it.isNotBlank() }
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?: emptyList(),
            bassBoost = prefs[Keys.AUDIO_FX_BASS] ?: 0,
            virtualizer = prefs[Keys.AUDIO_FX_VIRTUALIZER] ?: 0
        )
    }

    suspend fun setAudioEffectsPreferences(
        enabled: Boolean,
        presetIndex: Int,
        bandLevels: List<Int>,
        bassBoost: Int,
        virtualizer: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUDIO_FX_ENABLED] = enabled
            prefs[Keys.AUDIO_FX_PRESET] = presetIndex
            prefs[Keys.AUDIO_FX_BANDS] = bandLevels.joinToString(",")
            prefs[Keys.AUDIO_FX_BASS] = bassBoost
            prefs[Keys.AUDIO_FX_VIRTUALIZER] = virtualizer
        }
    }

    // Una preferencia de orden por sección, guardada como el nombre del
    // enum. runCatching cubre el caso de un valor guardado que ya no exista
    // (ej. si algún día renombras una opción).
    val songsSortOption: Flow<SongSortOption> = context.dataStore.data.map { prefs ->
        prefs[Keys.SONGS_SORT]?.let { raw -> runCatching { SongSortOption.valueOf(raw) }.getOrNull() }
            ?: SongSortOption.TITLE_ASC
    }
    suspend fun setSongsSortOption(option: SongSortOption) {
        context.dataStore.edit { it[Keys.SONGS_SORT] = option.name }
    }

    val albumsSortOption: Flow<AlbumSortOption> = context.dataStore.data.map { prefs ->
        prefs[Keys.ALBUMS_SORT]?.let { raw -> runCatching { AlbumSortOption.valueOf(raw) }.getOrNull() }
            ?: AlbumSortOption.TITLE_ASC
    }
    suspend fun setAlbumsSortOption(option: AlbumSortOption) {
        context.dataStore.edit { it[Keys.ALBUMS_SORT] = option.name }
    }

    val artistsSortOption: Flow<ArtistSortOption> = context.dataStore.data.map { prefs ->
        prefs[Keys.ARTISTS_SORT]?.let { raw -> runCatching { ArtistSortOption.valueOf(raw) }.getOrNull() }
            ?: ArtistSortOption.NAME_ASC
    }
    suspend fun setArtistsSortOption(option: ArtistSortOption) {
        context.dataStore.edit { it[Keys.ARTISTS_SORT] = option.name }
    }

    val playlistSortOption: Flow<PlaylistSongSortOption> = context.dataStore.data.map { prefs ->
        prefs[Keys.PLAYLIST_SORT]?.let { raw -> runCatching { PlaylistSongSortOption.valueOf(raw) }.getOrNull() }
            ?: PlaylistSongSortOption.CUSTOM
    }
    suspend fun setPlaylistSortOption(option: PlaylistSongSortOption) {
        context.dataStore.edit { it[Keys.PLAYLIST_SORT] = option.name }
    }
}