// app/src/main/java/com/example/sonorid/ui/search/SearchScreen.kt
package com.example.sonorid.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // 👈 Cambiado a itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.library.LibraryViewModel
import com.example.sonorid.ui.library.SongRow // 👈 Importamos SongRow para usar el mismo diseño y lógica de favoritos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    modifier: Modifier = Modifier, // 👈 Agregado por buenas prácticas
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val allSongs by viewModel.songs.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState() // 👈 Recolectamos el estado de favoritos

    var sheetSongId by remember { mutableStateOf<Long?>(null) } // 👈 Estado para manejar el BottomSheet si se añade a una playlist

    LaunchedEffect(Unit) {
        viewModel.loadSongs()
    }

    // Desplegar el BottomSheet si se selecciona una canción para agregar a una playlist
    sheetSongId?.let { songId ->
        com.example.sonorid.ui.playlists.AddToPlaylistSheet(
            songId = songId,
            onDismiss = { sheetSongId = null }
        )
    }

    var query by remember { mutableStateOf("") }
    val results = remember(query, allSongs) {
        if (query.isBlank()) emptyList()
        else allSongs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar canciones, artistas...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 🚀 Patrón optimizado con itemsIndexed
            itemsIndexed(
                items = results,
                key = { _, song -> song.id }
            ) { index, song ->
                // Usamos SongRow para mantener la misma UI e incluir el botón de favoritos y menú
                SongRow(
                    song = song,
                    isFavorite = song.id in favoriteIds, // 👈 Pasamos el estado real de favoritos
                    onClick = { onSongClick(results, index) },
                    onToggleFavorite = { viewModel.toggleFavorite(song.id) }, // 👈 Permite dar favorito desde la búsqueda
                    onAddToPlaylist = { sheetSongId = song.id }
                )
            }
        }
    }
}