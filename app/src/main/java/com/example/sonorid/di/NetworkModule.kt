// app/src/main/java/com/example/sonorid/di/NetworkModule.kt
package com.example.sonorid.di

import com.example.sonorid.data.remote.LrcLibApi
import com.example.sonorid.data.remote.TheAudioDbApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LrcLibRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TheAudioDbRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideLrcLibApi(json: Json): LrcLibApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(LrcLibApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTheAudioDbApi(json: Json): TheAudioDbApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://www.theaudiodb.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(TheAudioDbApi::class.java)
    }
}