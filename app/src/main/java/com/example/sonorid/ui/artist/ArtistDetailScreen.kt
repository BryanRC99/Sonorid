// app/src/main/java/com/example/sonorid/ui/artist/ArtistDetailScreen.kt
package com.example.sonorid.ui.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonorid.domain.model.Song
import com.example.sonorid.ui.common.AlbumArt
import com.example.sonorid.ui.common.ArtistImage
import com.example.sonorid.ui.common.LocalToastHost
import com.example.sonorid.ui.common.SelectionTopBar
import com.example.sonorid.ui.common.SongOverflowMenu
import com.example.sonorid.ui.common.colorForName
import com.example.sonorid.ui.common.rememberDominantColor
import com.example.sonorid.ui.common.rememberSelectionState
import com.example.sonorid.ui.playlists.AddSongsToPlaylistSheet
import com.example.sonorid.ui.playlists.AddToPlaylistSheet
import com.example.sonorid.ui.theme.SonoridSizes
import com.example.sonorid.ui.theme.SonoridSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    infoViewModel: ArtistInfoViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val infoMap by infoViewModel.infoMap.collectAsState()
    var sheetSongId by remember { mutableStateOf<Long?>(null) }

    // Estado de selección múltiple
    val selection = rememberSelectionState<Long>()
    var showBulkPlaylistSheet by remember { mutableStateOf(false) }
    val showToast = LocalToastHost.current

    LaunchedEffect(artistName) {
        viewModel.load(artistName)
        infoViewModel.request(artistName)
    }

    val artistInfo = infoMap[artistName]

    val listState = rememberLazyListState()
    val showSolidTopBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item {
                    ArtistHeader(
                        artistName = artistName,
                        imageUrl = artistInfo?.imageUrl,
                        bannerUrl = artistInfo?.bannerUrl,
                        genre = artistInfo?.genre,
                        style = artistInfo?.style,
                        country = artistInfo?.country,
                        formedYear = artistInfo?.formedYear,
                        songCount = songs.size,
                        onPlay = { if (songs.isNotEmpty()) onSongClick(songs, 0) },
                        onShuffle = { if (songs.isNotEmpty()) onSongClick(songs.shuffled(), 0) }
                    )
                }
                item {
                    val biography = artistInfo?.biography
                    if (!biography.isNullOrBlank()) {
                        ArtistBiographySection(biography = biography)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = SonoridSpacing.Lg)
                        )
                        Spacer(Modifier.height(SonoridSpacing.Sm))
                    }
                }
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    ArtistSongRow(
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
                item { Spacer(Modifier.height(SonoridSpacing.Xxl)) }
            }
        }

        // TopBar condicional para selección múltiple
        // TopBar condicional para selección múltiple
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
                title = {
                    AnimatedVisibility(visible = showSolidTopBar, enter = fadeIn(), exit = fadeOut()) {
                        Text(artistName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (showSolidTopBar) Color.Transparent
                                    else MaterialTheme.colorScheme.background.copy(alpha = 0.55f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showSolidTopBar) MaterialTheme.colorScheme.background else Color.Transparent
                )
            )
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

@Composable
private fun ArtistHeader(
    artistName: String,
    imageUrl: String?,
    bannerUrl: String?,
    genre: String?,
    style: String?,
    country: String?,
    formedYear: String?,
    songCount: Int,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val nameBasedColor = remember(artistName) { colorForName(artistName) }
    val dominantColor = rememberDominantColor(model = imageUrl, fallback = nameBasedColor)
    val animatedScrim by androidx.compose.animation.animateColorAsState(
        targetValue = dominantColor,
        label = "artistScrimColor"
    )

    var isValidBannerRatio by remember(bannerUrl) { mutableStateOf<Boolean?>(null) }
    val useBanner = !bannerUrl.isNullOrBlank() && isValidBannerRatio != false

    Box(modifier = Modifier.fillMaxWidth()) {
        if (!bannerUrl.isNullOrBlank()) {
            coil.compose.SubcomposeAsyncImage(
                model = bannerUrl,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .then(if (isValidBannerRatio == false) Modifier.size(0.dp) else Modifier),
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    val ratio = if (size.height > 0) size.width / size.height else 0f
                    isValidBannerRatio = ratio >= 3f
                },
                onError = { isValidBannerRatio = false },
                loading = {}
            )
        }

        if (useBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f),
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.15f),
                                backgroundColor
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(animatedScrim.copy(alpha = 0.55f), backgroundColor),
                            startY = 0f,
                            endY = Offset.Infinite.y
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 56.dp, start = SonoridSpacing.Lg, end = SonoridSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArtistImage(
                artistName = artistName,
                imageUrl = imageUrl,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f)
                    .shadow(elevation = 20.dp, shape = CircleShape)
            )

            Spacer(Modifier.height(SonoridSpacing.Lg))

            Text(
                text = artistName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildString {
                    append("$songCount canciones")
                    if (!genre.isNullOrBlank()) append(" · $genre")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            val extraInfo = listOfNotNull(
                country,
                formedYear?.let { "Desde $it" },
                style?.takeIf { it != genre }
            ).joinToString(" · ")
            if (extraInfo.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = extraInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(SonoridSpacing.Md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                FilledIconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(SonoridSpacing.Sm))
        }
    }
}

@Composable
private fun ArtistBiographySection(biography: String) {
    var expanded by remember(biography) { mutableStateOf(false) }
    var isOverflowing by remember(biography) { mutableStateOf(false) }
    val collapsedMaxLines = 5

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm)
    ) {
        Text(
            text = "Biografía",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(SonoridSpacing.Xs))
        Text(
            text = biography,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) isOverflowing = result.hasVisualOverflow
            }
        )
        if (isOverflowing || expanded) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (expanded) "Ver menos" else "Ver más",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArtistSongRow(
    song: Song,
    isFavorite: Boolean,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = SonoridSpacing.Lg, vertical = SonoridSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seleccionado",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.width(SonoridSpacing.Sm))
        }

        AlbumArt(
            artUri = song.albumArtUri,
            modifier = Modifier.size(SonoridSizes.SongRowArt)
        )
        Spacer(Modifier.width(SonoridSpacing.Sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isFavorite) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                song.album,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isSelectionMode) {
            SongOverflowMenu(
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onAddToPlaylist = onAddToPlaylist
            )
        }
    }
}