// app/src/main/java/com/example/sonorid/di/NetworkModule.kt
package com.example.sonorid.di

import com.example.sonorid.BuildConfig
import com.example.sonorid.data.remote.FanartTvApi
import com.example.sonorid.data.remote.LrcLibApi
import com.example.sonorid.data.remote.MusicBrainzApi
import com.example.sonorid.data.remote.TheAudioDbApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LrcLibRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TheAudioDbRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MusicBrainzRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FanartTvApiKey

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Sonorid/1.0 (https://github.com/BryanRC99/Sonorid)")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()
    }

    /**
     * Cliente dedicado para MusicBrainz: mismo User-Agent identificable
     * (obligatorio, o bloquean el IP) + throttle de 1.1s entre requests.
     * ⚠️ Reemplaza "tu-email-o-repo" por tu email real o la URL del repo
     * antes de publicar — MusicBrainz puede bloquear User-Agents genéricos.
     */
    @Provides
    @Singleton
    @MusicBrainzRetrofit
    fun provideMusicBrainzOkHttpClient(): OkHttpClient {
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Sonorid/1.0 (contact: tu-email-o-repo)")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideLrcLibApi(json: Json, okHttpClient: OkHttpClient): LrcLibApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(LrcLibApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTheAudioDbApi(json: Json, okHttpClient: OkHttpClient): TheAudioDbApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://www.theaudiodb.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(TheAudioDbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMusicBrainzApi(
        json: Json,
        @MusicBrainzRetrofit okHttpClient: OkHttpClient
    ): MusicBrainzApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://musicbrainz.org/ws/2/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MusicBrainzApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFanartTvApi(json: Json, okHttpClient: OkHttpClient): FanartTvApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://webservice.fanart.tv/v3/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(FanartTvApi::class.java)
    }

    @Provides
    @FanartTvApiKey
    fun provideFanartTvApiKey(): String = BuildConfig.FANART_TV_API_KEY
}