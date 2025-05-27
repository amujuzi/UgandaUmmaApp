package com.imaniapp.uganda.di

import android.content.Context
import androidx.room.Room
import com.imaniapp.uganda.data.local.database.ImaniDatabase
import com.imaniapp.uganda.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideImaniDatabase(@ApplicationContext context: Context): ImaniDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ImaniDatabase::class.java,
            "imani_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun providePrayerTimeDao(database: ImaniDatabase): PrayerTimeDao {
        return database.prayerTimeDao()
    }
    
    @Provides
    fun provideQuranBookmarkDao(database: ImaniDatabase): QuranBookmarkDao {
        return database.quranBookmarkDao()
    }
    
    @Provides
    fun provideDuaDao(database: ImaniDatabase): DuaDao {
        return database.duaDao()
    }
    
    @Provides
    fun provideMosqueDao(database: ImaniDatabase): MosqueDao {
        return database.mosqueDao()
    }
    
    @Provides
    fun provideReadingProgressDao(database: ImaniDatabase): ReadingProgressDao {
        return database.readingProgressDao()
    }
} 