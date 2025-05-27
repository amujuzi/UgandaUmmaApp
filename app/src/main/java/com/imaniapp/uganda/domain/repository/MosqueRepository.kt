package com.imaniapp.uganda.domain.repository

import com.imaniapp.uganda.domain.model.Mosque
import com.imaniapp.uganda.domain.model.MosqueFilter
import com.imaniapp.uganda.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface MosqueRepository {
    
    /**
     * Get nearby mosques from multiple sources
     * @param location User's current location
     * @param radiusKm Search radius in kilometers
     * @param filter Optional filters for mosque features
     */
    suspend fun getNearbyMosques(
        location: Location,
        radiusKm: Double = 10.0,
        filter: MosqueFilter? = null
    ): Flow<Result<List<Mosque>>>
    
    /**
     * Get mosques from local database
     */
    suspend fun getLocalMosques(): Flow<List<Mosque>>
    
    /**
     * Get mosques with specific filters
     */
    suspend fun getMosquesWithFilters(filter: MosqueFilter): Flow<List<Mosque>>
    
    /**
     * Get mosque details by ID
     */
    suspend fun getMosqueById(id: String): Mosque?
    
    /**
     * Search mosques by name or location
     */
    suspend fun searchMosques(
        query: String,
        location: Location? = null
    ): Flow<Result<List<Mosque>>>
    
    /**
     * Add a new mosque (user contribution)
     */
    suspend fun addMosque(mosque: Mosque): Result<Unit>
    
    /**
     * Update mosque information
     */
    suspend fun updateMosque(mosque: Mosque): Result<Unit>
    
    /**
     * Report incorrect mosque information
     */
    suspend fun reportMosque(
        mosqueId: String,
        issue: String,
        userEmail: String? = null
    ): Result<Unit>
    
    /**
     * Cache mosques locally
     */
    suspend fun cacheMosques(mosques: List<Mosque>)
    
    /**
     * Get cached mosques for offline use
     */
    suspend fun getCachedMosques(
        location: Location,
        radiusKm: Double
    ): Flow<List<Mosque>>
    
    /**
     * Clear old cached data
     */
    suspend fun clearOldCache()
    
    /**
     * Get mosque statistics for a location
     */
    suspend fun getMosqueStats(location: Location): MosqueStats
}

data class MosqueStats(
    val totalMosques: Int,
    val mosquesWithJummah: Int,
    val mosquesWithWomenFacilities: Int,
    val mosquesWithWudu: Int,
    val averageDistance: Double
) 