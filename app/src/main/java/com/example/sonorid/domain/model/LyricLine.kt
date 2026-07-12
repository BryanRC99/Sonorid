// app/src/main/java/com/example/sonorid/domain/model/LyricLine.kt
package com.example.sonorid.domain.model

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class Lyrics(
    val synced: List<LyricLine>,
    val plainText: String?
)