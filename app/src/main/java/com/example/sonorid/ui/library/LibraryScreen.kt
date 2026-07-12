package com.example.sonorid.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSongs()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (songs.isEmpty()) {
            Text(
                text = "No se encontraron canciones",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(songs) { song ->
                    ListItem(
                        headlineContent = { Text(song.title) },
                        supportingContent = { Text("${song.artist} • ${song.album}") },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}