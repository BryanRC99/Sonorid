<h1 align="center">
  <img src="docs/images/Logo.png" width="100" alt="Sonorid logo"><br>
  🎵 Sonorid
</h1>

<p align="center">
  <b>A local music player for Android, with synced lyrics and its own visual identity.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Platform">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-8B5CF6?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin + Compose">
  <img src="https://img.shields.io/badge/Status-Work%20In%20Progress-orange?style=flat-square" alt="Status">
  <img src="https://img.shields.io/badge/License-TBD-lightgrey?style=flat-square" alt="License">
</p>

---

## 📌 About the Project

**Sonorid** is a local music player for Android, built with **Jetpack Compose**. It scans your library directly from the device (via `MediaStore`), organizes it into Songs, Albums, Artists, and Genres, and lets you build your own playlists and favorites, all stored locally.

Its flagship feature is **synced lyrics**: while a song plays, Sonorid queries **[LRCLIB](https://lrclib.net/)** for a synced (LRC) lyric, and if one exists, displays it line by line following the song's progress, with the ability to tap a line to seek to that point. If the song has no lyrics available, nothing extra is shown — the app keeps working normally.

No account or backend required: your music lives entirely on your phone, and all state (playlists, favorites, lyrics cache, artist info cache) is stored locally with Room.

> ⚠️ **Note:** Sonorid is under active development. The features described below are implemented and tested, but the UI and feature set are still evolving.

---

## ✨ Features

### Library & Playback
- Full library browser from `MediaStore` — Songs, Albums, Artists, and Genres, with built-in search
- Music folder selection: choose which folders on the device get indexed into your library
- Background playback with **Media3 (ExoPlayer + MediaSession)**: shuffle, three repeat modes, audio focus handling, and automatic pause when headphones disconnect
- Persistent mini-player across the whole app + full-screen expanded player
- Dominant color extraction from album art / artist photo (Palette) to theme each detail screen

### Synced Lyrics
- Synced (LRC) lyrics in the expanded player, sourced from **LRCLIB**
- Auto-scroll to the active line, with tap-to-seek on any line
- Local caching with Room: each song is only queried once (including remembering when a lyric *doesn't* exist, to avoid pointless retries)

### Artists
- Artist photo and genre sourced from **TheAudioDB**
- Local caching with Room, with the same "don't retry if already searched and not found" logic
- Auto-generated initials avatar when no photo is available

### Playlists & Favorites
- One-tap favorites, available from any song list
- Create, rename, and manage your own playlists
- Add or remove songs from a bottom sheet, with a 2x2 collage cover generated from each playlist's first songs

---

## 🖼️ Screenshots

<p align="center">
  <img src="docs/images/Menu.jpeg" width="260" alt="Home screen"><br>
  <i>Song library</i>
</p>

<p align="center">
  <img src="docs/images/Albumes.jpeg" width="260" alt="Albums">
  <img src="docs/images/AlbumDetalle.jpeg" width="260" alt="Album detail"><br>
  <i>Albums and album detail</i>
</p>

<p align="center">
  <img src="docs/images/Artistas.jpeg" width="260" alt="Artists">
  <img src="docs/images/ArtistaDetalle.jpeg" width="260" alt="Artist detail"><br>
  <i>Artists and artist detail</i>
</p>

<p align="center">
  <img src="docs/images/Listas.jpeg" width="260" alt="Playlists">
  <img src="docs/images/ListasDetalle.jpeg" width="260" alt="Playlist detail"><br>
  <i>Playlists and playlist detail</i>
</p>

<p align="center">
  <img src="docs/images/Favoritos.jpeg" width="260" alt="Favorites"><br>
  <i>Favorites</i>
</p>

<p align="center">
  <img src="docs/images/Reproductor.jpeg" width="260" alt="Expanded player">
  <img src="docs/images/LetrasSinc.jpeg" width="260" alt="Synced lyrics"><br>
  <i>Full-screen player and synced lyrics</i>
</p>

<p align="center">
  <img src="docs/images/Configuraciones.jpeg" width="260" alt="Settings"><br>
  <i>Settings / folder selection</i>
</p>

---

## 🛠️ Tech Stack

* **UI:** Kotlin + Jetpack Compose (Material 3), custom dark theme ("Sonorid": electric violet, teal, and coral accents), Navigation Compose
* **Architecture:** MVVM — one `ViewModel` + `StateFlow` per screen, dependency injection with **Hilt**
* **Playback:** `Media3` (ExoPlayer + MediaSession), driven by a single shared `MediaController`, with a foreground playback service
* **Local persistence:** `Room` (playlists, favorite songs, lyrics cache, artist info cache) + `DataStore Preferences` (selected folders)
* **Remote data:** `Retrofit` + `kotlinx.serialization` to consume the LRCLIB API (lyrics) and TheAudioDB API (artist info)
* **Images:** `Coil` for album art and artist photos, with `Palette` for dominant color extraction
* **Music source:** Android's `MediaStore` (no backend of its own — everything runs on-device)

---

## 🎤 Credits

- Synced lyrics provided by **[LRCLIB](https://lrclib.net/)**, a free and open lyrics database.
- Artist info and photos provided by **[TheAudioDB](https://www.theaudiodb.com/)**.

Thanks to both projects for making their APIs freely available.

---

## 🚀 Roadmap

- [x] Local library via MediaStore (songs, albums, artists, genres)
- [x] Music folder selection
- [x] Background playback with Media3 + notification
- [x] Synced lyrics via LRCLIB, with local caching
- [x] Artist info via TheAudioDB, with local caching
- [x] Favorites and playlist management
- [ ] Equalizer
- [ ] Android Auto support
- [ ] Home screen widget
- [ ] Playlist export / import
- [ ] Configurable light theme from Settings

---

## 🤝 Contributing

This is a personal project still under active development. If you have ideas, found a bug, or want to suggest an improvement, feel free to open an issue.
