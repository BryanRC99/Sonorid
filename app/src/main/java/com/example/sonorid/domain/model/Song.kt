package com.example.sonorid.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,       // en milisegundos
    val uri: android.net.Uri,
    val albumArtUri: android.net.Uri,
    val trackNumber: Int
)