// app/src/main/java/com/example/sonorid/ui/genre/GenreDetailScreen.kt
package com.example.sonorid.ui.genre

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.common.LocalToastHost
import com.example.sonorid.ui.common.SelectionTopBar
import com.example.sonorid.ui.common.rememberSelectionState
import com.example.sonorid.ui.library.SongRow
import com.example.sonorid.ui.playlists.AddSongsToPlaylistSheet
import com.example.sonorid.ui.playlists.AddToPlaylistSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreDetailScreen(
    genre: String,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: GenreDetailViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }

    // Estado de selección múltiple
    val selection = rememberSelectionState<Long>()
    var showBulkPlaylistSheet by remember { mutableStateOf(false) }
    val showToast = LocalToastHost.current

    LaunchedEffect(genre) { viewModel.load(genre) }

    Scaffold(
        topBar = {
            if (selection.isActive) {
                SelectionTopBar(
                    selectedCount = selection.count,
                    totalCount = songs.size,
                    onClose = { selection.clear() },
                    onToggleSelectAll = { selection.toggleSelectAll(songs.map { it.id }) },
                    actions = {
                        IconButton(onClick = { showBulkPlaylistSheet = true }) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = "Agregar a lista")
                        }
                        IconButton(onClick = {
                            viewModel.addToFavorites(selection.selectedIds)
                            showToast("Agregadas a favoritos")
                            selection.clear()
                        }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Agregar a favoritos")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(genre) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (songs.isEmpty()) {
                Text("Sin canciones en este género", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        SongRow(
                            song = song,
                            isFavorite = song.id in favoriteIds,
                            isSelectionMode = selection.isActive,
                            isSelected = selection.isSelected(song.id),
                            onClick = {
                                if (selection.isActive) selection.toggle(song.id)
                                else onSongClick(songs, index)
                            },
                            onLongClick = { selection.toggle(song.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                            onAddToPlaylist = { sheetSongId = song.id }
                        )
                    }
                }
            }
        }
    }

    // Sheet individual
    sheetSongId?.let { songId ->
        AddToPlaylistSheet(songId = songId, onDismiss = { sheetSongId = null })
    }

    // Sheet masivo
    if (showBulkPlaylistSheet) {
        AddSongsToPlaylistSheet(
            songIds = selection.selectedIds.toList(),
            onDismiss = { showBulkPlaylistSheet = false },
            onDone = { message ->
                showToast(message)
                selection.clear()
            }
        )
    }
}