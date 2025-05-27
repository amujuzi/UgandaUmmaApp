package com.imaniapp.uganda.di

import com.imaniapp.uganda.data.repository.MosqueRepositoryImpl
import com.imaniapp.uganda.data.repository.PrayerTimeRepositoryImpl
import com.imaniapp.uganda.data.repository.QuranRepositoryImpl
import com.imaniapp.uganda.data.repository.DuaRepositoryImpl
import com.imaniapp.uganda.domain.repository.MosqueRepository
import com.imaniapp.uganda.domain.repository.PrayerTimeRepository
import com.imaniapp.uganda.domain.repository.QuranRepository
import com.imaniapp.uganda.domain.repository.DuaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPrayerTimeRepository(
        prayerTimeRepositoryImpl: PrayerTimeRepositoryImpl
    ): PrayerTimeRepository
    
    @Binds
    @Singleton
    abstract fun bindQuranRepository(
        quranRepositoryImpl: QuranRepositoryImpl
    ): QuranRepository
    
    @Binds
    @Singleton
    abstract fun bindDuaRepository(
        duaRepositoryImpl: DuaRepositoryImpl
    ): DuaRepository
    
    @Binds
    @Singleton
    abstract fun bindMosqueRepository(
        mosqueRepositoryImpl: MosqueRepositoryImpl
    ): MosqueRepository
} 