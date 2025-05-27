package com.imaniapp.uganda.domain.repository

import com.imaniapp.uganda.domain.model.Surah
import com.imaniapp.uganda.domain.model.Ayah
import com.imaniapp.uganda.domain.model.QuranBookmark
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    
    /**
     * Get list of all Surahs
     */
    suspend fun getSurahs(): Flow<Result<List<Surah>>>
    
    /**
     * Get specific Surah with Arabic text and translation
     * @param surahNumber Surah number (1-114)
     * @param includeTranslation Whether to include English translation
     */
    suspend fun getSurah(
        surahNumber: Int,
        includeTranslation: Boolean = true
    ): Flow<Result<Surah>>
    
    /**
     * Get specific Ayah with translation
     * @param surahNumber Surah number
     * @param ayahNumber Ayah number within the surah
     */
    suspend fun getAyah(
        surahNumber: Int,
        ayahNumber: Int
    ): Flow<Result<Ayah>>
    
    /**
     * Search in Quran
     * @param query Search query
     * @param surahNumber Optional: specific surah to search in
     */
    suspend fun searchQuran(
        query: String,
        surahNumber: Int? = null
    ): Flow<Result<List<Ayah>>>
    
    /**
     * Get user bookmarks
     */
    suspend fun getBookmarks(): Flow<List<QuranBookmark>>
    
    /**
     * Add bookmark
     */
    suspend fun addBookmark(bookmark: QuranBookmark)
    
    /**
     * Remove bookmark
     */
    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int)
    
    /**
     * Check if ayah is bookmarked
     */
    suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean
    
    /**
     * Get reading progress for a surah
     */
    suspend fun getReadingProgress(surahNumber: Int): Int
    
    /**
     * Update reading progress
     */
    suspend fun updateReadingProgress(surahNumber: Int, lastAyahRead: Int)
} 