package com.imaniapp.uganda.data.local.dao

import androidx.room.*
import com.imaniapp.uganda.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    
    @Query("SELECT * FROM reading_progress ORDER BY updatedAt DESC")
    fun getAllReadingProgress(): Flow<List<ReadingProgressEntity>>
    
    @Query("SELECT * FROM reading_progress WHERE surahNumber = :surahNumber")
    suspend fun getReadingProgress(surahNumber: Int): ReadingProgressEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ReadingProgressEntity)
    
    @Query("UPDATE reading_progress SET lastAyahRead = :lastAyahRead, updatedAt = :updatedAt WHERE surahNumber = :surahNumber")
    suspend fun updateProgress(surahNumber: Int, lastAyahRead: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteProgress(progress: ReadingProgressEntity)
    
    @Query("DELETE FROM reading_progress WHERE surahNumber = :surahNumber")
    suspend fun deleteProgressBySurah(surahNumber: Int)
    
    @Query("DELETE FROM reading_progress")
    suspend fun deleteAllProgress()
} 