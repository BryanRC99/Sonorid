// app/src/main/java/com/example/sonorid/data/remote/TheAudioDbApi.kt
package com.example.sonorid.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class TheAudioDbArtistResponse(
    val artists: List<TheAudioDbArtist>? = null
)

@Serializable
data class TheAudioDbArtist(
    @SerialName("strArtist") val name: String? = null,
    @SerialName("strArtistThumb") val thumbUrl: String? = null,
    @SerialName("strArtistFanart") val fanartUrl: String? = null,
    @SerialName("strGenre") val genre: String? = null,
    @SerialName("strBiographyEN") val biography: String? = null
)

interface TheAudioDbApi {
    @GET("api/v1/json/123/search.php")
    suspend fun searchArtist(@Query("s") name: String): TheAudioDbArtistResponse
}