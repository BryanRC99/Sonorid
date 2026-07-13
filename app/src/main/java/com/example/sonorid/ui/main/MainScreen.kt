// app/src/main/java/com/example/sonorid/ui/main/MainScreen.kt
package com.example.sonorid.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // 👈 Asegura el correcto funcionamiento de 'by'
import androidx.compose.runtime.setValue // 👈 Asegura el correcto funcionamiento de 'by'
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.sonorid.playback.PlaybackMetaState // 👈 IMPORTADO: El estado de metadatos real de tu MusicController
import com.example.sonorid.ui.folders.FolderSelectionScreen
import com.example.sonorid.ui.library.*
import com.example.sonorid.ui.player.*
import com.example.sonorid.ui.search.SearchScreen

private sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Songs : Tab("songs", "Canciones", Icons.Default.MusicNote)
    object Albums : Tab("albums", "Álbumes", Icons.Default.Album)
    object Artists : Tab("artists", "Artistas", Icons.Default.Person)
    object Genres : Tab("genres", "Géneros", Icons.Default.Category)
    object Playlists : Tab("playlists", "Listas", Icons.Default.QueueMusic)
}

private val tabs = listOf(Tab.Songs, Tab.Albums, Tab.Artists, Tab.Genres, Tab.Playlists)

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val rootNav = rememberNavController()

    // 1. Recolectamos el estado de los metadatos (Canción, si está reproduciendo, etc.)
    val playbackState by playerViewModel.metaState.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = rootNav, startDestination = "main") {
            composable("main") {
                val tabNav = rememberNavController()

                Scaffold(
                    bottomBar = {
                        Column {
                            AnimatedVisibility(visible = playbackState.currentSong != null && !isExpanded) {
                                // MiniPlayer usará el playbackState para metadatos (Título, covers, isPlaying)
                                MiniPlayerWithProgress(
                                    state = playbackState,
                                    onExpand = { isExpanded = true },
                                    onTogglePlayPause = { playerViewModel.togglePlayPause() },
                                    onSkipNext = { playerViewModel.skipNext() },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    playerViewModel = playerViewModel
                                )
                            }
                            NavigationBar {
                                val backStack by tabNav.currentBackStackEntryAsState()
                                val currentRoute = backStack?.destination?.route
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentRoute == tab.route,
                                        onClick = {
                                            tabNav.navigate(tab.route) {
                                                popUpTo(Tab.Songs.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = tabNav,
                        startDestination = Tab.Songs.route,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Tab.Songs.route) {
                            SongsTabScreen(
                                onOpenFolders = { rootNav.navigate("folders") },
                                onOpenSearch = { rootNav.navigate("search") },
                                onSongClick = { songs, index -> playerViewModel.play(songs, index) }
                            )
                        }
                        composable(Tab.Albums.route) {
                            AlbumsScreen(onAlbumClick = { albumId -> rootNav.navigate("album/$albumId") })
                        }
                        composable(Tab.Artists.route) {
                            ArtistsScreen(onArtistClick = { artist ->
                                rootNav.navigate("artist/${java.net.URLEncoder.encode(artist, "UTF-8")}")
                            })
                        }
                        composable(Tab.Genres.route) {
                            GenresScreen(onGenreClick = { genre ->
                                rootNav.navigate("genre/${java.net.URLEncoder.encode(genre, "UTF-8")}")
                            })
                        }
                        composable(Tab.Playlists.route) {
                            com.example.sonorid.ui.playlists.PlaylistsScreen(
                                onOpenFavorites = { rootNav.navigate("favorites") },
                                onOpenPlaylist = { id -> rootNav.navigate("playlist/$id") }
                            )
                        }
                    }
                }
            }

            composable("folders") {
                FolderSelectionScreen(onBack = { rootNav.popBackStack() })
            }

            composable("search") {
                SearchScreen(
                    onBack = { rootNav.popBackStack() },
                    onSongClick = { songs, index ->
                        playerViewModel.play(songs, index)
                        rootNav.popBackStack()
                    }
                )
            }

            composable("favorites") {
                com.example.sonorid.ui.playlists.PlaylistDetailScreen(
                    playlistId = null,
                    title = "Favoritos",
                    onBack = { rootNav.popBackStack() },
                    onSongClick = { songs, index ->
                        playerViewModel.play(songs, index)
                        rootNav.popBackStack()
                    }
                )
            }

            composable(
                route = "playlist/{playlistId}",
                arguments = listOf(androidx.navigation.navArgument("playlistId") { type = androidx.navigation.NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
                com.example.sonorid.ui.playlists.PlaylistDetailScreen(
                    playlistId = id,
                    title = "Lista",
                    onBack = { rootNav.popBackStack() },
                    onSongClick = { songs, index ->
                        playerViewModel.play(songs, index)
                        rootNav.popBackStack()
                    }
                )
            }

            // 🚀 Limpieza: Se quitó la segunda declaración duplicada que tenías de esta ruta
            composable(
                route = "album/{albumId}",
                arguments = listOf(androidx.navigation.navArgument("albumId") { type = androidx.navigation.NavType.LongType })
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
                com.example.sonorid.ui.album.AlbumDetailScreen(
                    albumId = albumId,
                    onBack = { rootNav.popBackStack() },
                    onSongClick = { songs, index ->
                        playerViewModel.play(songs, index)
                        rootNav.popBackStack()
                    }
                )
            }

            composable(
                route = "genre/{genreName}",
                arguments = listOf(androidx.navigation.navArgument("genreName") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("genreName") ?: return@composable
                val genre = java.net.URLDecoder.decode(encoded, "UTF-8")
                com.example.sonorid.ui.genre.GenreDetailScreen(
                    genre = genre,
                    onBack = { rootNav.popBackStack() },
                    onSongClick = { songs, index ->
                        playerViewModel.play(songs, index)
                        rootNav.popBackStack()
                    }
                )
            }

            composable(
                route = "artist/{artistName}",
                arguments = listOf(androidx.navigation.navArgument("artistName") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("artistName") ?: return@composable
                val artistName = java.net.URLDecoder.decode(encoded, "UTF-8")
                com.example.sonorid.ui.artist.ArtistDetailScreen(
                    artistName = artistName,
                    onBack = { rootNav.popBackStack() },
                    onSongClick = { songs, index ->
                        playerViewModel.play(songs, index)
                        rootNav.popBackStack()
                    }
                )
            }
        } // Cierre del NavHost general (rootNav)

        // El reproductor expandido pertenece a la UI global flotando sobre la navegación actual

        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            // Nota: En tu ExpandedPlayerScreen pasa 'state = playbackState'. Si necesitas dibujar la barra de progreso
            // de la canción, consume directamente 'playerViewModel.progress' en ese componente para no causar retrasos aquí.
            ExpandedPlayerWithProgress(
                state = playbackState,
                onCollapse = { isExpanded = false },
                onTogglePlayPause = { playerViewModel.togglePlayPause() },
                onSkipNext = { playerViewModel.skipNext() },
                onSkipPrevious = { playerViewModel.skipPrevious() },
                onSeek = { playerViewModel.seekTo(it) },
                onToggleShuffle = { playerViewModel.toggleShuffle() },
                onCycleRepeat = { playerViewModel.cycleRepeat() },
                playerViewModel = playerViewModel
            )
        }
    }
}

/** Mantiene las actualizaciones frecuentes del progreso fuera de la navegación y las listas. */
@Composable
private fun MiniPlayerWithProgress(
    state: PlaybackMetaState,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel
) {
    val progress by playerViewModel.progress.collectAsState()
    MiniPlayer(state, progress, onExpand, onTogglePlayPause, onSkipNext, modifier)
}

@Composable
private fun ExpandedPlayerWithProgress(
    state: PlaybackMetaState,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    val progress by playerViewModel.progress.collectAsState()
    ExpandedPlayerScreen(
        state = state,
        progress = progress,
        onCollapse = onCollapse,
        onTogglePlayPause = onTogglePlayPause,
        onSkipNext = onSkipNext,
        onSkipPrevious = onSkipPrevious,
        onSeek = onSeek,
        onToggleShuffle = onToggleShuffle,
        onCycleRepeat = onCycleRepeat
    )
}
