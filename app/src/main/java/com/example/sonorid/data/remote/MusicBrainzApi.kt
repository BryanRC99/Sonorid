// app/src/main/java/com/example/sonorid/data/remote/MusicBrainzApi.kt
package com.example.sonorid.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class MusicBrainzSearchResponse(
    val artists: List<MusicBrainzArtist> = emptyList()
)

@Serializable
data class MusicBrainzArtist(
    val id: String, // MBID: lo que necesitamos para consultar Fanart.tv
    val name: String? = null,
    val score: Int? = null, // 0-100, qué tan bien coincide con la búsqueda
    @SerialName("sort-name") val sortName: String? = null,
    val disambiguation: String? = null
)

interface MusicBrainzApi {
    @GET("artist/")
    suspend fun searchArtist(
        @Query("query") query: String,
        @Query("fmt") format: String = "json"
    ): MusicBrainzSearchResponse
}