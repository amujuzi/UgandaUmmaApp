package com.imaniapp.uganda.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.imaniapp.uganda.data.local.dao.*
import com.imaniapp.uganda.data.local.entity.*

@Database(
    entities = [
        PrayerTimeEntity::class,
        QuranBookmarkEntity::class,
        DuaEntity::class,
        MosqueEntity::class,
        ReadingProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ImaniDatabase : RoomDatabase() {
    
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun quranBookmarkDao(): QuranBookmarkDao
    abstract fun duaDao(): DuaDao
    abstract fun mosqueDao(): MosqueDao
    abstract fun readingProgressDao(): ReadingProgressDao
    
    companion object {
        @Volatile
        private var INSTANCE: ImaniDatabase? = null
        
        fun getDatabase(context: Context): ImaniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImaniDatabase::class.java,
                    "imani_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 