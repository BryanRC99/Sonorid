// app/src/main/java/com/example/sonorid/ui/library/SongsTabScreen.kt
package com.example.sonorid.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.domain.model.SongSortOption
import com.example.sonorid.domain.model.sortedByOption
import com.example.sonorid.ui.common.LocalToastHost
import com.example.sonorid.ui.common.SelectionTopBar
import com.example.sonorid.ui.common.SortBottomSheet
import com.example.sonorid.ui.common.SortIconButton
import com.example.sonorid.ui.common.rememberSelectionState
import com.example.sonorid.ui.playlists.AddSongsToPlaylistSheet
import com.example.sonorid.ui.theme.SonoridSpacing

@Composable
fun SongsTabScreen(
    onOpenSettings: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val rawSongs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortOption by viewModel.songsSortOption.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }

    val songs = remember(rawSongs, sortOption) { rawSongs.sortedByOption(sortOption) }

    var sheetSongId by remember { mutableStateOf<Long?>(null) }
    var showBulkPlaylistSheet by remember { mutableStateOf(false) }
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val selection = rememberSelectionState<Long>()
    val showToast = LocalToastHost.current

    LaunchedEffect(Unit) { viewModel.loadSongs() }

    sheetSongId?.let { songId ->
        com.example.sonorid.ui.playlists.AddToPlaylistSheet(
            songId = songId,
            onDismiss = { sheetSongId = null }
        )
    }

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

    if (showSortSheet) {
        SortBottomSheet(
            options = SongSortOption.entries,
            selected = sortOption,
            labelFor = { it.label },
            onSelect = { viewModel.setSongsSortOption(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (selection.isActive) {
            SelectionTopBar(
                selectedCount = selection.count,
                totalCount = songs.size,
                onClose = { selection.clear() },
                onToggleSelectAll = { selection.toggleSelectAll(songs.map { it.id }) },
                actions = {
                    IconButton(onClick = { showBulkPlaylistSheet = true }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = "Agregar a lista de reproducción")
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
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (songs.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No se encontraron canciones")
                    TextButton(onClick = onOpenSettings) { Text("Elegir carpetas") }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { GreetingHeader() }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Canciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            SortIconButton(onClick = { showSortSheet = true })
                        }
                    }
                    itemsIndexed(items = songs, key = { _, song -> song.id }) { index, song ->
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
                    item { Spacer(Modifier.height(SonoridSpacing.Xxxl)) }
                }
            }

            if (songs.isNotEmpty() && !selection.isActive) {
                ExtendedFloatingActionButton(
                    onClick = { onSongClick(songs.shuffled(), 0) },
                    icon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                    text = { Text("Aleatorio") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(SonoridSpacing.Md)
                        .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
                )
            }
        }
    }
}