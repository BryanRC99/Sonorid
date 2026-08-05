// ui/main/MainScreen.kt
package com.example.sonorid.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.sonorid.ui.settings.AboutScreen
import com.example.sonorid.ui.settings.BulkLyricsScreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.navigation.compose.*
import com.example.sonorid.playback.PlaybackMetaState
import com.example.sonorid.ui.common.LocalToastHost
import com.example.sonorid.ui.common.SonoridToast
import com.example.sonorid.ui.folders.FolderSelectionScreen
import com.example.sonorid.ui.library.*
import com.example.sonorid.ui.player.*
import com.example.sonorid.ui.search.SearchScreen
import com.example.sonorid.ui.settings.SettingsScreen
import com.example.sonorid.ui.settings.BackupScreen
import com.example.sonorid.ui.theme.SonoridSpacing
import kotlinx.coroutines.launch

private sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    object Songs : Tab("songs", "Canciones", Icons.Default.MusicNote)
    object Albums : Tab("albums", "Álbumes", Icons.Default.Album)
    object Artists : Tab("artists", "Artistas", Icons.Default.Person)
    object Playlists : Tab("playlists", "Listas", Icons.Default.QueueMusic)
}

private val tabs = listOf(Tab.Songs, Tab.Albums, Tab.Artists, Tab.Playlists)

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val rootNav = rememberNavController()
    val tabNav = rememberNavController()

    val playbackState by playerViewModel.metaState.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        playerViewModel.expandRequests.collect { isExpanded = true }
    }

    val rootBackStack by rootNav.currentBackStackEntryAsState()
    val isOnMainTabs = rootBackStack?.destination?.route == "main"

    val tabBackStack by tabNav.currentBackStackEntryAsState()
    val currentTabRoute = tabBackStack?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showToast: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    CompositionLocalProvider(LocalToastHost provides showToast) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data -> SonoridToast(data) }
                },
                bottomBar = {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        AnimatedVisibility(visible = playbackState.currentSong != null && !isExpanded) {
                            MiniPlayerWithProgress(
                                state = playbackState,
                                onExpand = { isExpanded = true },
                                onTogglePlayPause = { playerViewModel.togglePlayPause() },
                                onSkipNext = { playerViewModel.skipNext() },
                                modifier = Modifier,
                                playerViewModel = playerViewModel
                            )
                        }
                        if (isOnMainTabs) {
                            SonoridBottomBar(
                                currentRoute = currentTabRoute,
                                onTabSelected = { tab ->
                                    tabNav.navigate(tab.route) {
                                        popUpTo(Tab.Songs.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { padding ->
                NavHost(
                    navController = rootNav,
                    startDestination = "main",
                    modifier = Modifier.padding(padding)
                ) {
                    composable("main") {
                        Scaffold(
                            topBar = {
                                SonoridAppTopBar(
                                    onOpenSearch = { rootNav.navigate("search") },
                                    onOpenSettings = { rootNav.navigate("settings") }
                                )
                            }
                        ) { tabPadding ->
                            NavHost(
                                navController = tabNav,
                                startDestination = Tab.Songs.route,
                                modifier = Modifier.padding(tabPadding)
                            ) {
                                composable(Tab.Songs.route) {
                                    SongsTabScreen(
                                        onOpenSettings = { rootNav.navigate("settings") },
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
                                composable(Tab.Playlists.route) {
                                    com.example.sonorid.ui.playlists.PlaylistsScreen(
                                        onOpenFavorites = { rootNav.navigate("favorites") },
                                        onOpenPlaylist = { id -> rootNav.navigate("playlist/$id") }
                                    )
                                }
                            }
                        }
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBack = { rootNav.popBackStack() },
                            onOpenFolders = { rootNav.navigate("folders") },
                            onOpenBulkLyrics = { rootNav.navigate("bulk_lyrics") },
                            onOpenBulkMetadata = { rootNav.navigate("bulk_metadata") },
                            onOpenBackup = { rootNav.navigate("backup") },
                            onOpenAbout = { rootNav.navigate("about") },
                            onOpenAudioEffects = { rootNav.navigate("audio_effects") }
                        )
                    }

                    composable("folders") {
                        FolderSelectionScreen(onBack = { rootNav.popBackStack() })
                    }
                    composable("about") {
                        AboutScreen(onBack = { rootNav.popBackStack() })
                    }

                    composable("bulk_lyrics") {
                        BulkLyricsScreen(onBack = { rootNav.popBackStack() })
                    }

                    composable("bulk_metadata") {
                        com.example.sonorid.ui.settings.BulkMetadataScreen(onBack = { rootNav.popBackStack() })
                    }

                    composable("backup") {
                        BackupScreen(onBack = { rootNav.popBackStack() })
                    }

                    composable("audio_effects") {
                        com.example.sonorid.ui.settings.AudioEffectsScreen(onBack = { rootNav.popBackStack() })
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
                            onSongClick = { songs, index -> playerViewModel.play(songs, index) }
                        )
                    }

                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(androidx.navigation.navArgument("playlistId") { type = androidx.navigation.NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
                        com.example.sonorid.ui.playlists.PlaylistDetailScreen(
                            playlistId = id,
                            title = "Lista de reproducción",
                            onBack = { rootNav.popBackStack() },
                            onSongClick = { songs, index -> playerViewModel.play(songs, index) }
                        )
                    }

                    composable(
                        route = "album/{albumId}",
                        arguments = listOf(androidx.navigation.navArgument("albumId") { type = androidx.navigation.NavType.LongType })
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
                        com.example.sonorid.ui.album.AlbumDetailScreen(
                            albumId = albumId,
                            onBack = { rootNav.popBackStack() },
                            onSongClick = { songs, index -> playerViewModel.play(songs, index) }
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
                            onSongClick = { songs, index -> playerViewModel.play(songs, index) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ExpandedPlayerScreen(
                    playerViewModel = playerViewModel,
                    onCollapse = { isExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun SonoridAppTopBar(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = SonoridSpacing.Sm, vertical = SonoridSpacing.Xs)
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarIconButton(icon = Icons.Default.Search, contentDescription = "Buscar", onClick = onOpenSearch)
            TopBarIconButton(icon = Icons.Default.Settings, contentDescription = "Ajustes", onClick = onOpenSettings)
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SonoridBottomBar(
    currentRoute: String?,
    onTabSelected: (Tab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(2.dp))
                    Text(text = tab.label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1)
                }
            }
        }
    }
}

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
    MiniPlayer(
        state = state,
        progress = progress,
        onExpand = onExpand,
        onTogglePlayPause = onTogglePlayPause,
        onSkipPrevious = { playerViewModel.skipPrevious() },
        onSkipNext = onSkipNext,
        modifier = modifier
    )
}