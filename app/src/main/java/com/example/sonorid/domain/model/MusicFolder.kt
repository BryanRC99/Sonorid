package com.example.sonorid.domain.model

data class MusicFolder(
    val path: String,        // ej: "Music/" o "Download/Podcasts/"
    val displayName: String, // ej: "Music"
    val songCount: Int
)