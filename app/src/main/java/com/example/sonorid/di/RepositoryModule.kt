package com.example.sonorid.di

import com.example.sonorid.data.repository.MusicRepositoryImpl
import com.example.sonorid.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMusicRepository(
        impl: MusicRepositoryImpl
    ): MusicRepository
}