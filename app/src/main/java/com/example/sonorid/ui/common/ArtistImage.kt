// app/src/main/java/com/example/sonorid/ui/common/ArtistImage.kt
package com.example.sonorid.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun ArtistImage(
    artistName: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    if (imageUrl.isNullOrBlank()) {
        InitialsAvatar(name = artistName, modifier = modifier, size = size)
    } else {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = artistName,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape),
            loading = { InitialsAvatar(name = artistName, size = size) },
            error = { InitialsAvatar(name = artistName, size = size) }
        )
    }
}