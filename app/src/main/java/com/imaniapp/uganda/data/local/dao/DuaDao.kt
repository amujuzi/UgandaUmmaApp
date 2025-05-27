package com.imaniapp.uganda.data.local.dao

import androidx.room.*
import com.imaniapp.uganda.data.local.entity.DuaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDao {
    
    @Query("SELECT * FROM duas ORDER BY createdAt DESC")
    fun getAllDuas(): Flow<List<DuaEntity>>
    
    @Query("SELECT * FROM duas WHERE category = :category ORDER BY createdAt DESC")
    fun getDuasByCategory(category: String): Flow<List<DuaEntity>>
    
    @Query("SELECT * FROM duas WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteDuas(): Flow<List<DuaEntity>>
    
    @Query("SELECT * FROM duas WHERE isAiGenerated = 1 ORDER BY createdAt DESC")
    fun getAiGeneratedDuas(): Flow<List<DuaEntity>>
    
    @Query("SELECT * FROM duas WHERE id = :id")
    suspend fun getDuaById(id: Long): DuaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDua(dua: DuaEntity): Long
    
    @Update
    suspend fun updateDua(dua: DuaEntity)
    
    @Query("UPDATE duas SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    @Delete
    suspend fun deleteDua(dua: DuaEntity)
    
    @Query("DELETE FROM duas WHERE id = :id")
    suspend fun deleteDuaById(id: Long)
    
    @Query("DELETE FROM duas")
    suspend fun deleteAllDuas()
    
    @Query("SELECT * FROM duas WHERE source = :source ORDER BY createdAt DESC")
    fun getDuasBySource(source: String): Flow<List<DuaEntity>>
    
    @Query("SELECT * FROM duas WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchDuas(query: String): Flow<List<DuaEntity>>
    
    @Query("SELECT COUNT(*) FROM duas WHERE source = 'HISNUL_MUSLIM'")
    suspend fun getHisnulMuslimCount(): Int
} 