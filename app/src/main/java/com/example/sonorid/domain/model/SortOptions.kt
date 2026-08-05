package com.example.sonorid.domain.model

enum class SongSortOption(val label: String) {
    TITLE_ASC("Título (A-Z)"),
    TITLE_DESC("Título (Z-A)"),
    ARTIST("Artista"),
    ALBUM("Álbum"),
    DURATION("Duración: más larga primero"),
    RECENTLY_ADDED("Agregadas recientemente")
}

enum class AlbumSortOption(val label: String) {
    TITLE_ASC("Título (A-Z)"),
    TITLE_DESC("Título (Z-A)"),
    ARTIST("Artista (A-Z)"),
    SONG_COUNT("Cantidad de canciones")
}

enum class ArtistSortOption(val label: String) {
    NAME_ASC("Nombre (A-Z)"),
    NAME_DESC("Nombre (Z-A)"),
    SONG_COUNT("Cantidad de canciones")
}

/** CUSTOM = orden real (posición en la playlist, u orden de carga en Favoritos). */
enum class PlaylistSongSortOption(val label: String) {
    CUSTOM("Orden personalizado"),
    TITLE_ASC("Título (A-Z)"),
    TITLE_DESC("Título (Z-A)"),
    ARTIST("Artista"),
    ALBUM("Álbum"),
    DURATION("Duración: más larga primero"),
    RECENTLY_ADDED("Agregadas recientemente")
}

fun List<Song>.sortedByOption(option: SongSortOption): List<Song> = when (option) {
    SongSortOption.TITLE_ASC -> sortedBy { it.title.lowercase() }
    SongSortOption.TITLE_DESC -> sortedByDescending { it.title.lowercase() }
    SongSortOption.ARTIST -> sortedBy { it.artist.lowercase() }
    SongSortOption.ALBUM -> sortedBy { it.album.lowercase() }
    SongSortOption.DURATION -> sortedByDescending { it.duration }
    SongSortOption.RECENTLY_ADDED -> sortedByDescending { it.dateAdded }
}

fun List<Song>.sortedByOption(option: PlaylistSongSortOption): List<Song> = when (option) {
    PlaylistSongSortOption.CUSTOM -> this
    PlaylistSongSortOption.TITLE_ASC -> sortedBy { it.title.lowercase() }
    PlaylistSongSortOption.TITLE_DESC -> sortedByDescending { it.title.lowercase() }
    PlaylistSongSortOption.ARTIST -> sortedBy { it.artist.lowercase() }
    PlaylistSongSortOption.ALBUM -> sortedBy { it.album.lowercase() }
    PlaylistSongSortOption.DURATION -> sortedByDescending { it.duration }
    PlaylistSongSortOption.RECENTLY_ADDED -> sortedByDescending { it.dateAdded }
}