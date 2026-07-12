// app/src/main/java/com/example/sonorid/ui/player/LyricsScreen.kt
package com.example.sonorid.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    song: Song,
    currentPositionMs: Long,
    onBack: () -> Unit,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val lyrics by viewModel.lyrics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(song.id) { viewModel.loadIfNeeded(song) }

    val listState = rememberLazyListState()
    val activeIndex = lyrics?.synced?.indexOfLast { it.timeMs <= currentPositionMs } ?: -1

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(song.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                lyrics == null -> Text(
                    "No se encontraron letras para esta canción",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                lyrics!!.synced.isNotEmpty() -> SyncedLyricsList(lyrics!!.synced.map { it.text }, activeIndex, listState)
                lyrics!!.plainText != null -> Text(
                    lyrics!!.plainText!!,
                    modifier = Modifier.padding(24.dp)
                )
                else -> Text(
                    "Letras no disponibles",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun SyncedLyricsList(lines: List<String>, activeIndex: Int, listState: LazyListState) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        items(lines.size) { index ->
            Text(
                text = lines[index],
                style = if (index == activeIndex) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.titleMedium,
                color = if (index == activeIndex) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}