package com.imaniapp.uganda.data.local.dao

import androidx.room.*
import com.imaniapp.uganda.data.local.entity.MosqueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueDao {
    
    @Query("SELECT * FROM mosques ORDER BY name")
    fun getAllMosques(): Flow<List<MosqueEntity>>
    
    @Query("SELECT * FROM mosques WHERE hasJummah = 1 ORDER BY name")
    fun getMosquesWithJummah(): Flow<List<MosqueEntity>>
    
    @Query("SELECT * FROM mosques WHERE hasWomenPrayer = 1 ORDER BY name")
    fun getMosquesWithWomenPrayer(): Flow<List<MosqueEntity>>
    
    @Query("SELECT * FROM mosques WHERE hasWudu = 1 ORDER BY name")
    fun getMosquesWithWudu(): Flow<List<MosqueEntity>>
    
    @Query("SELECT * FROM mosques WHERE id = :id")
    suspend fun getMosqueById(id: String): MosqueEntity?
    
    @Query("""
        SELECT * FROM mosques 
        WHERE (:hasJummah = 0 OR hasJummah = :hasJummah)
        AND (:hasWomenPrayer = 0 OR hasWomenPrayer = :hasWomenPrayer)
        AND (:hasWudu = 0 OR hasWudu = :hasWudu)
        ORDER BY name
    """)
    fun getMosquesWithFilters(
        hasJummah: Boolean = false,
        hasWomenPrayer: Boolean = false,
        hasWudu: Boolean = false
    ): Flow<List<MosqueEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMosque(mosque: MosqueEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMosques(mosques: List<MosqueEntity>)
    
    @Update
    suspend fun updateMosque(mosque: MosqueEntity)
    
    @Delete
    suspend fun deleteMosque(mosque: MosqueEntity)
    
    @Query("DELETE FROM mosques WHERE id = :id")
    suspend fun deleteMosqueById(id: String)
    
    @Query("DELETE FROM mosques")
    suspend fun deleteAllMosques()
} 