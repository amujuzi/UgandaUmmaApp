package com.imaniapp.uganda.domain.repository

import com.imaniapp.uganda.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DuaRepository {
    
    /**
     * Get all du'as from local database
     */
    suspend fun getAllDuas(): Flow<List<Dua>>
    
    /**
     * Get du'as by category
     */
    suspend fun getDuasByCategory(category: DuaCategory): Flow<List<Dua>>
    
    /**
     * Get favorite du'as
     */
    suspend fun getFavoriteDuas(): Flow<List<Dua>>
    
    /**
     * Get AI-generated du'as
     */
    suspend fun getAiGeneratedDuas(): Flow<List<Dua>>
    
    /**
     * Get Hisnul Muslim du'as (predefined)
     */
    suspend fun getHisnulMuslimDuas(): Flow<List<Dua>>
    
    /**
     * Search du'as by text
     */
    suspend fun searchDuas(query: String): Flow<List<Dua>>
    
    /**
     * Get specific du'a by ID
     */
    suspend fun getDuaById(id: Long): Dua?
    
    /**
     * Add du'a to favorites
     */
    suspend fun addToFavorites(duaId: Long)
    
    /**
     * Remove du'a from favorites
     */
    suspend fun removeFromFavorites(duaId: Long)
    
    /**
     * Save du'a to local database
     */
    suspend fun saveDua(dua: Dua): Long
    
    /**
     * Delete du'a from local database
     */
    suspend fun deleteDua(duaId: Long)
    
    /**
     * Generate AI du'a based on request
     */
    suspend fun generateAiDua(request: DuaRequest): Result<AiDuaResponse>
    
    /**
     * Save AI-generated du'a to database
     */
    suspend fun saveAiDua(aiResponse: AiDuaResponse, request: DuaRequest): Long
    
    /**
     * Initialize Hisnul Muslim du'as in database
     */
    suspend fun initializeHisnulMuslimDuas()
    
    /**
     * Check if Hisnul Muslim du'as are already initialized
     */
    suspend fun isHisnulMuslimInitialized(): Boolean
} 