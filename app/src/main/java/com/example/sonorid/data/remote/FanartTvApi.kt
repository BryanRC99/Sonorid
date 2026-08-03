// app/src/main/java/com/example/sonorid/data/remote/FanartTvApi.kt
package com.example.sonorid.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class FanartTvArtistResponse(
    val name: String? = null,
    val artistthumb: List<FanartTvImage>? = null,
    val artistbackground: List<FanartTvImage>? = null,
    val hdmusiclogo: List<FanartTvImage>? = null,
    val musiclogo: List<FanartTvImage>? = null,
    val musicbanner: List<FanartTvImage>? = null
)

@Serializable
data class FanartTvImage(
    val id: String? = null,
    val url: String? = null,
    val lang: String? = null,
    val likes: String? = null
)

interface FanartTvApi {
    @GET("music/{mbid}")
    suspend fun getArtistImages(
        @Path("mbid") mbid: String,
        @Query("api_key") apiKey: String
    ): FanartTvArtistResponse
}