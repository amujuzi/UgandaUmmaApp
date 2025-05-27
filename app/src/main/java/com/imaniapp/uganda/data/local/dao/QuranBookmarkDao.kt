package com.imaniapp.uganda.data.local.dao

import androidx.room.*
import com.imaniapp.uganda.data.local.entity.QuranBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranBookmarkDao {
    
    @Query("SELECT * FROM quran_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>
    
    @Query("SELECT * FROM quran_bookmarks WHERE surahNumber = :surahNumber ORDER BY ayahNumber")
    fun getBookmarksBySurah(surahNumber: Int): Flow<List<QuranBookmarkEntity>>
    
    @Query("SELECT * FROM quran_bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: Long): QuranBookmarkEntity?
    
    @Query("SELECT * FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun getBookmarkByAyah(surahNumber: Int, ayahNumber: Int): QuranBookmarkEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmarkEntity): Long
    
    @Update
    suspend fun updateBookmark(bookmark: QuranBookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: QuranBookmarkEntity)
    
    @Query("DELETE FROM quran_bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)
    
    @Query("DELETE FROM quran_bookmarks")
    suspend fun deleteAllBookmarks()
    
    @Query("SELECT COUNT(*) > 0 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean
} 