package com.imaniapp.uganda.data.repository

import android.content.Context
import com.imaniapp.uganda.BuildConfig
import com.imaniapp.uganda.data.local.dao.MosqueDao
import com.imaniapp.uganda.data.local.entity.MosqueEntity
import com.imaniapp.uganda.data.remote.api.GooglePlacesApi
import com.imaniapp.uganda.data.remote.dto.PlaceResult
import com.imaniapp.uganda.data.remote.supabase.SupabaseClient
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.MosqueRepository
import com.imaniapp.uganda.domain.repository.MosqueStats
import com.imaniapp.uganda.presentation.utils.LocationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class MosqueRepositoryImpl @Inject constructor(
    private val googlePlacesApi: GooglePlacesApi,
    private val mosqueDao: MosqueDao,
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : MosqueRepository {
    
    private val googleApiKey = getGoogleApiKey()
    
    override suspend fun getNearbyMosques(
        location: Location,
        radiusKm: Double,
        filter: MosqueFilter?
    ): Flow<Result<List<Mosque>>> = flow {
        try {
            val mosques = mutableListOf<Mosque>()
            
            // 1. Try Google Places API first (most comprehensive)
            try {
                val googleMosques = fetchFromGooglePlaces(location, radiusKm)
                mosques.addAll(googleMosques)
            } catch (e: Exception) {
                // Log error but continue with other sources
            }
            
            // 2. Get from Supabase (community data)
            try {
                val supabaseMosques = fetchFromSupabase(location, radiusKm)
                mosques.addAll(supabaseMosques)
            } catch (e: Exception) {
                // Log error but continue
            }
            
            // 3. Get from local database (cached + user added)
            val localMosques = fetchFromLocalDatabase(location, radiusKm)
            mosques.addAll(localMosques)
            
            // Remove duplicates and apply filters
            val uniqueMosques = removeDuplicates(mosques)
            val filteredMosques = applyFilters(uniqueMosques, filter)
            val sortedMosques = sortByDistance(filteredMosques, location)
            
            // Cache results for offline use
            cacheMosques(sortedMosques)
            
            emit(Result.success(sortedMosques))
            
        } catch (e: Exception) {
            // Fallback to cached data
            val cachedMosques = getCachedMosquesSync(location, radiusKm)
            if (cachedMosques.isNotEmpty()) {
                emit(Result.success(cachedMosques))
            } else {
                emit(Result.failure(e))
            }
        }
    }
    
    override suspend fun getLocalMosques(): Flow<List<Mosque>> {
        return mosqueDao.getAllMosques().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getMosquesWithFilters(filter: MosqueFilter): Flow<List<Mosque>> {
        return mosqueDao.getMosquesWithFilters(
            hasJummah = filter.hasJummah,
            hasWomenPrayer = filter.hasWomenPrayer,
            hasWudu = filter.hasWudu
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getMosqueById(id: String): Mosque? {
        return mosqueDao.getMosqueById(id)?.toDomainModel()
    }
    
    override suspend fun searchMosques(
        query: String,
        location: Location?
    ): Flow<Result<List<Mosque>>> = flow {
        try {
            val mosques = mutableListOf<Mosque>()
            
            // Search in Google Places
            if (location != null && googleApiKey.isNotEmpty()) {
                try {
                    val response = googlePlacesApi.searchMosques(
                        query = "$query mosque masjid",
                        location = "${location.latitude},${location.longitude}",
                        radius = GooglePlacesApi.RADIUS_25KM,
                        apiKey = googleApiKey
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        val googleMosques = response.body()!!.results.map { 
                            it.toDomainModel(location) 
                        }
                        mosques.addAll(googleMosques)
                    }
                } catch (e: Exception) {
                    // Continue with other sources
                }
            }
            
            // Search in local database
            val localResults = searchInLocalDatabase(query)
            mosques.addAll(localResults)
            
            // Search in Supabase
            try {
                val supabaseResults = searchInSupabase(query, location)
                mosques.addAll(supabaseResults)
            } catch (e: Exception) {
                // Continue
            }
            
            val uniqueMosques = removeDuplicates(mosques)
            val sortedMosques = if (location != null) {
                sortByDistance(uniqueMosques, location)
            } else {
                uniqueMosques.sortedBy { it.name }
            }
            
            emit(Result.success(sortedMosques))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    override suspend fun addMosque(mosque: Mosque): Result<Unit> {
        return try {
            // Add to local database
            mosqueDao.insertMosque(mosque.toEntity())
            
            // Add to Supabase for community sharing
            try {
                supabaseClient.client.from("mosques").insert(mosque.toSupabaseModel())
            } catch (e: Exception) {
                // Local storage succeeded, Supabase failed - still success
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMosque(mosque: Mosque): Result<Unit> {
        return try {
            mosqueDao.updateMosque(mosque.toEntity())
            
            // Update in Supabase if it exists there
            try {
                supabaseClient.client.from("mosques")
                    .update(mosque.toSupabaseModel()) {
                        filter {
                            eq("id", mosque.id)
                        }
                    }
            } catch (e: Exception) {
                // Continue even if Supabase update fails
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reportMosque(
        mosqueId: String,
        issue: String,
        userEmail: String?
    ): Result<Unit> {
        return try {
            val report = MosqueReport(
                id = UUID.randomUUID().toString(),
                mosqueId = mosqueId,
                issue = issue,
                userEmail = userEmail,
                createdAt = System.currentTimeMillis()
            )
            
            supabaseClient.client.from("mosque_reports").insert(report)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cacheMosques(mosques: List<Mosque>) {
        try {
            val entities = mosques.map { it.toEntity() }
            mosqueDao.insertMosques(entities)
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }
    
    override suspend fun getCachedMosques(
        location: Location,
        radiusKm: Double
    ): Flow<List<Mosque>> {
        return mosqueDao.getAllMosques().map { entities ->
            entities.map { it.toDomainModel() }
                .filter { mosque ->
                    calculateDistance(
                        location.latitude, location.longitude,
                        mosque.latitude, mosque.longitude
                    ) <= radiusKm
                }
                .sortedBy { mosque ->
                    calculateDistance(
                        location.latitude, location.longitude,
                        mosque.latitude, mosque.longitude
                    )
                }
        }
    }
    
    override suspend fun clearOldCache() {
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            // Keep user-added mosques, only clear cached ones
            // This would require adding a source field to distinguish
        } catch (e: Exception) {
            // Log error
        }
    }
    
    override suspend fun getMosqueStats(location: Location): MosqueStats {
        val nearbyMosques = getCachedMosquesSync(location, 25.0)
        
        return MosqueStats(
            totalMosques = nearbyMosques.size,
            mosquesWithJummah = nearbyMosques.count { it.hasJummah },
            mosquesWithWomenFacilities = nearbyMosques.count { it.hasWomenPrayer },
            mosquesWithWudu = nearbyMosques.count { it.hasWudu },
            averageDistance = if (nearbyMosques.isNotEmpty()) {
                nearbyMosques.mapNotNull { it.distance }.average()
            } else 0.0
        )
    }
    
    // Private helper methods
    
    private suspend fun fetchFromGooglePlaces(
        location: Location,
        radiusKm: Double
    ): List<Mosque> {
        if (googleApiKey.isEmpty()) return emptyList()
        
        val radiusMeters = (radiusKm * 1000).toInt()
        val response = googlePlacesApi.getNearbyMosques(
            location = "${location.latitude},${location.longitude}",
            radius = radiusMeters,
            apiKey = googleApiKey
        )
        
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.results.map { it.toDomainModel(location) }
        } else {
            emptyList()
        }
    }
    
    private suspend fun fetchFromSupabase(
        location: Location,
        radiusKm: Double
    ): List<Mosque> {
        return try {
            val mosques = supabaseClient.client.from("mosques")
                .select(columns = Columns.ALL)
                .decodeList<SupabaseMosque>()
            
            mosques.filter { mosque ->
                calculateDistance(
                    location.latitude, location.longitude,
                    mosque.latitude, mosque.longitude
                ) <= radiusKm
            }.map { it.toDomainModel(location) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun fetchFromLocalDatabase(
        location: Location,
        radiusKm: Double
    ): List<Mosque> {
        return try {
            val allMosques = mosqueDao.getAllMosques()
            // This is a simplified version - in real implementation,
            // you'd want to use a proper Flow collection
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun getCachedMosquesSync(
        location: Location,
        radiusKm: Double
    ): List<Mosque> {
        return try {
            // This would need to be implemented as a suspend function
            // that returns the cached mosques synchronously
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun searchInLocalDatabase(query: String): List<Mosque> {
        return try {
            // Implementation would search in local database
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun searchInSupabase(
        query: String,
        location: Location?
    ): List<Mosque> {
        return try {
            val mosques = supabaseClient.client.from("mosques")
                .select(columns = Columns.ALL)
                .decodeList<SupabaseMosque>()
            
            mosques.map { it.toDomainModel(location) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun removeDuplicates(mosques: List<Mosque>): List<Mosque> {
        return mosques.distinctBy { mosque ->
            // Consider mosques duplicate if they're within 50 meters of each other
            "${mosque.name.lowercase()}_${(mosque.latitude * 1000).toInt()}_${(mosque.longitude * 1000).toInt()}"
        }
    }
    
    private fun applyFilters(mosques: List<Mosque>, filter: MosqueFilter?): List<Mosque> {
        if (filter == null) return mosques
        
        return mosques.filter { mosque ->
            (!filter.hasJummah || mosque.hasJummah) &&
            (!filter.hasWomenPrayer || mosque.hasWomenPrayer) &&
            (!filter.hasWudu || mosque.hasWudu) &&
            (filter.maxDistance == null || (mosque.distance ?: 0.0) <= filter.maxDistance)
        }
    }
    
    private fun sortByDistance(mosques: List<Mosque>, location: Location): List<Mosque> {
        return mosques.map { mosque ->
            mosque.copy(
                distance = calculateDistance(
                    location.latitude, location.longitude,
                    mosque.latitude, mosque.longitude
                )
            )
        }.sortedBy { it.distance }
    }
    
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    private fun getGoogleApiKey(): String {
        return try {
            val properties = java.util.Properties()
            val inputStream = context.assets.open("local.properties")
            properties.load(inputStream)
            properties.getProperty("MAPS_API_KEY", "")
        } catch (e: Exception) {
            ""
        }
    }
    
    // Extension functions for data mapping
    
    private fun PlaceResult.toDomainModel(userLocation: Location): Mosque {
        val distance = calculateDistance(
            userLocation.latitude, userLocation.longitude,
            geometry.location.lat, geometry.location.lng
        )
        
        return Mosque(
            id = placeId,
            name = name,
            address = formattedAddress ?: vicinity ?: "",
            latitude = geometry.location.lat,
            longitude = geometry.location.lng,
            phoneNumber = null, // Would need place details call
            hasJummah = true, // Default assumption for mosques
            hasWomenPrayer = false, // Would need to be determined from reviews/details
            hasWudu = true, // Default assumption
            description = null,
            imageUrl = photos?.firstOrNull()?.let { 
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${it.photoReference}&key=$googleApiKey"
            },
            website = null, // Would need place details call
            distance = distance
        )
    }
    
    private fun MosqueEntity.toDomainModel(): Mosque {
        return Mosque(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            phoneNumber = phoneNumber,
            hasJummah = hasJummah,
            hasWomenPrayer = hasWomenPrayer,
            hasWudu = hasWudu,
            description = description,
            imageUrl = imageUrl,
            website = website,
            distance = null,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun Mosque.toEntity(): MosqueEntity {
        return MosqueEntity(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            phoneNumber = phoneNumber,
            hasJummah = hasJummah,
            hasWomenPrayer = hasWomenPrayer,
            hasWudu = hasWudu,
            description = description,
            imageUrl = imageUrl,
            website = website,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun Mosque.toSupabaseModel(): SupabaseMosque {
        return SupabaseMosque(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            phoneNumber = phoneNumber,
            hasJummah = hasJummah,
            hasWomenPrayer = hasWomenPrayer,
            hasWudu = hasWudu,
            description = description,
            imageUrl = imageUrl,
            website = website,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun SupabaseMosque.toDomainModel(userLocation: Location?): Mosque {
        val distance = if (userLocation != null) {
            calculateDistance(
                userLocation.latitude, userLocation.longitude,
                latitude, longitude
            )
        } else null
        
        return Mosque(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            phoneNumber = phoneNumber,
            hasJummah = hasJummah,
            hasWomenPrayer = hasWomenPrayer,
            hasWudu = hasWudu,
            description = description,
            imageUrl = imageUrl,
            website = website,
            distance = distance,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

// Data models for Supabase
@Serializable
data class SupabaseMosque(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String? = null,
    val hasJummah: Boolean = true,
    val hasWomenPrayer: Boolean = false,
    val hasWudu: Boolean = true,
    val description: String? = null,
    val imageUrl: String? = null,
    val website: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class MosqueReport(
    val id: String,
    val mosqueId: String,
    val issue: String,
    val userEmail: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 