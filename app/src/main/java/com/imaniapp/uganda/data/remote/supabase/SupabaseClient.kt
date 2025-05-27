// =============================================================================
// 1. SECURITY FIX: Remove hardcoded credentials from SupabaseClient.kt
// =============================================================================

// File: app/src/main/java/com/imaniapp/uganda/data/remote/supabase/SupabaseClient.kt
package com.imaniapp.uganda.data.remote.supabase

import com.imaniapp.uganda.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor(
    @Named("supabase_url") private val supabaseUrl: String,
    @Named("supabase_anon_key") private val supabaseAnonKey: String
) {
    
    val client = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseAnonKey
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
    
    // Auth instance for user authentication
    val auth get() = client.pluginManager.getPlugin(Auth)
    
    // Postgrest instance for database operations
    val database get() = client.pluginManager.getPlugin(Postgrest)
    
    // Storage instance for file uploads
    val storage get() = client.pluginManager.getPlugin(Storage)
}

// =============================================================================
// 2. SECURITY FIX: Create ConfigModule for secure configuration
// =============================================================================

// File: app/src/main/java/com/imaniapp/uganda/di/ConfigModule.kt
package com.imaniapp.uganda.di

import com.imaniapp.uganda.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    
    @Provides
    @Named("supabase_url")
    fun provideSupabaseUrl(): String {
        return BuildConfig.SUPABASE_URL
    }
    
    @Provides
    @Named("supabase_anon_key")
    fun provideSupabaseAnonKey(): String {
        return BuildConfig.SUPABASE_ANON_KEY
    }
    
    @Provides
    @Named("maps_api_key")
    fun provideMapsApiKey(): String {
        return BuildConfig.MAPS_API_KEY
    }
    
    @Provides
    @Named("openai_api_key")
    fun provideOpenAIApiKey(): String {
        return BuildConfig.OPENAI_API_KEY ?: ""
    }
}

// =============================================================================
// 3. LOGGING FIX: Create proper logging infrastructure
// =============================================================================

// File: app/src/main/java/com/imaniapp/uganda/core/logging/Logger.kt
package com.imaniapp.uganda.core.logging

import android.util.Log
import com.imaniapp.uganda.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun wtf(tag: String, message: String, throwable: Throwable? = null)
}

@Singleton
class AndroidLogger @Inject constructor() : Logger {
    
    private val isDebug = BuildConfig.DEBUG
    
    override fun d(tag: String, message: String) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }
    
    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    override fun w(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
        // In production, send to crash reporting service
        if (!isDebug) {
            // Firebase Crashlytics or similar
            // Crashlytics.log("$tag: $message")
            // throwable?.let { Crashlytics.recordException(it) }
        }
    }
    
    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
        // In production, send to crash reporting service
        if (!isDebug) {
            // Firebase Crashlytics or similar
            // Crashlytics.log("$tag: $message")
            // throwable?.let { Crashlytics.recordException(it) }
        }
    }
    
    override fun wtf(tag: String, message: String, throwable: Throwable?) {
        Log.wtf(tag, message, throwable)
        // Always send critical errors to crash reporting
        // Crashlytics.log("CRITICAL - $tag: $message")
        // throwable?.let { Crashlytics.recordException(it) }
    }
}

// =============================================================================
// 4. ERROR HANDLING FIX: Create standardized error handling
// =============================================================================

// File: app/src/main/java/com/imaniapp/uganda/core/error/ErrorHandler.kt
package com.imaniapp.uganda.core.error

import com.imaniapp.uganda.core.logging.Logger
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

sealed class ImaniError(
    val message: String,
    val userFriendlyMessage: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    class NetworkError(
        message: String = "Network connection failed",
        userFriendlyMessage: String = "Please check your internet connection and try again"
    ) : ImaniError(message, userFriendlyMessage)
    
    class ApiError(
        message: String,
        userFriendlyMessage: String = "Service temporarily unavailable. Please try again later"
    ) : ImaniError(message, userFriendlyMessage)
    
    class DatabaseError(
        message: String,
        userFriendlyMessage: String = "Failed to save data. Please try again"
    ) : ImaniError(message, userFriendlyMessage)
    
    class LocationError(
        message: String,
        userFriendlyMessage: String = "Unable to get your location. Please enable location services"
    ) : ImaniError(message, userFriendlyMessage)
    
    class IslamicContentError(
        message: String,
        userFriendlyMessage: String = "Failed to load Islamic content. Please try again"
    ) : ImaniError(message, userFriendlyMessage)
    
    class ValidationError(
        message: String,
        userFriendlyMessage: String
    ) : ImaniError(message, userFriendlyMessage)
    
    class UnknownError(
        message: String = "An unexpected error occurred",
        userFriendlyMessage: String = "Something went wrong. Please try again"
    ) : ImaniError(message, userFriendlyMessage)
}

@Singleton
class ErrorHandler @Inject constructor(
    private val logger: Logger
) {
    
    fun handleError(throwable: Throwable, tag: String): ImaniError {
        val error = when (throwable) {
            is ImaniError -> throwable
            is UnknownHostException, is IOException -> {
                ImaniError.NetworkError()
            }
            is SocketTimeoutException -> {
                ImaniError.NetworkError(
                    message = "Request timed out",
                    userFriendlyMessage = "Request is taking too long. Please try again"
                )
            }
            is HttpException -> {
                when (throwable.code()) {
                    401 -> ImaniError.ApiError(
                        message = "Unauthorized access",
                        userFriendlyMessage = "Authentication failed. Please restart the app"
                    )
                    403 -> ImaniError.ApiError(
                        message = "Forbidden access",
                        userFriendlyMessage = "Access denied to this service"
                    )
                    404 -> ImaniError.ApiError(
                        message = "Resource not found",
                        userFriendlyMessage = "Requested content not found"
                    )
                    500, 502, 503, 504 -> ImaniError.ApiError(
                        message = "Server error: ${throwable.code()}",
                        userFriendlyMessage = "Server is temporarily unavailable. Please try again later"
                    )
                    else -> ImaniError.ApiError(
                        message = "HTTP ${throwable.code()}: ${throwable.message()}",
                        userFriendlyMessage = "Service error. Please try again"
                    )
                }
            }
            else -> ImaniError.UnknownError(
                message = throwable.message ?: "Unknown error occurred"
            )
        }
        
        logger.e(tag, "Error handled: ${error.message}", throwable)
        return error
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        logger.e(tag, message, throwable)
    }
    
    fun logWarning(tag: String, message: String, throwable: Throwable? = null) {
        logger.w(tag, message, throwable)
    }
}

// =============================================================================
// 5. RESOURCE MANAGEMENT FIX: LocationHelper with proper cleanup
// =============================================================================

// File: app/src/main/java/com/imaniapp/uganda/presentation/utils/LocationHelper.kt (UPDATED)
package com.imaniapp.uganda.presentation.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.imaniapp.uganda.core.error.ErrorHandler
import com.imaniapp.uganda.core.error.ImaniError
import com.imaniapp.uganda.core.logging.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationHelper @Inject constructor(
    private val context: Context,
    private val logger: Logger,
    private val errorHandler: ErrorHandler
) {
    
    companion object {
        private const val TAG = "LocationHelper"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        
        // Uganda's approximate center coordinates (fallback)
        const val UGANDA_DEFAULT_LATITUDE = 1.3733
        const val UGANDA_DEFAULT_LONGITUDE = 32.2903
    }
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L // 10 seconds
    ).apply {
        setMinUpdateDistanceMeters(100f) // 100 meters
        setMaxUpdateDelayMillis(15000L) // 15 seconds
    }.build()
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                throw ImaniError.LocationError("Location permission not granted")
            }
            
            logger.d(TAG, "Requesting current location")
            
            suspendCancellableCoroutine { continuation ->
                try {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                logger.d(TAG, "Last known location retrieved: ${location.latitude}, ${location.longitude}")
                                continuation.resume(location)
                            } else {
                                logger.w(TAG, "Last known location is null, requesting fresh location")
                                requestFreshLocation { freshLocation ->
                                    continuation.resume(freshLocation)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            val error = errorHandler.handleError(exception, TAG)
                            continuation.resumeWithException(error)
                        }
                } catch (e: SecurityException) {
                    val error = ImaniError.LocationError("Location permission denied")
                    errorHandler.logError(TAG, "Security exception getting location", e)
                    continuation.resumeWithException(error)
                }
            }
        } catch (e: Exception) {
            val error = errorHandler.handleError(e, TAG)
            logger.e(TAG, "Failed to get current location", e)
            throw error
        }
    }
    
    private fun requestFreshLocation(callback: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            callback(null)
            return
        }
        
        var locationCallback: LocationCallback? = null
        
        try {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    val location = result.lastLocation
                    logger.d(TAG, "Fresh location received: ${location?.latitude}, ${location?.longitude}")
                    callback(location)
                }
                
                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        logger.w(TAG, "Location not available")
                        fusedLocationClient.removeLocationUpdates(this)
                        callback(null)
                    }
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            errorHandler.logError(TAG, "Security exception requesting fresh location", e)
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            callback(null)
        }
    }
    
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            val error = ImaniError.LocationError("Location permission not granted")
            errorHandler.logError(TAG, "Location permission not granted for updates")
            close(error)
            return@callbackFlow
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    logger.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")
                    trySend(location).isSuccess
                }
            }
            
            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    logger.w(TAG, "Location updates not available")
                }
            }
        }
        
        try {
            logger.d(TAG, "Starting location updates")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            val error = ImaniError.LocationError("Location permission denied")
            errorHandler.logError(TAG, "Security exception starting location updates", e)
            close(error)
            return@callbackFlow
        } catch (e: Exception) {
            val error = errorHandler.handleError(e, TAG)
            close(error)
            return@callbackFlow
        }
        
        awaitClose {
            logger.d(TAG, "Stopping location updates")
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                errorHandler.logWarning(TAG, "Error removing location updates", e)
            }
        }
    }
}

// =============================================================================
// 6. ERROR HANDLING FIX: Updated QuranRepositoryImpl with proper error handling
// =============================================================================

// File: app/src/main/java/com/imaniapp/uganda/data/repository/QuranRepositoryImpl.kt (UPDATED - key sections)
package com.imaniapp.uganda.data.repository

import com.imaniapp.uganda.core.error.ErrorHandler
import com.imaniapp.uganda.core.error.ImaniError
import com.imaniapp.uganda.core.logging.Logger
import com.imaniapp.uganda.data.local.dao.QuranBookmarkDao
import com.imaniapp.uganda.data.local.dao.ReadingProgressDao
import com.imaniapp.uganda.data.local.entity.QuranBookmarkEntity
import com.imaniapp.uganda.data.local.entity.ReadingProgressEntity
import com.imaniapp.uganda.data.remote.api.QuranApi
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val quranApi: QuranApi,
    private val bookmarkDao: QuranBookmarkDao,
    private val readingProgressDao: ReadingProgressDao,
    private val logger: Logger,
    private val errorHandler: ErrorHandler
) : QuranRepository {
    
    companion object {
        private const val TAG = "QuranRepository"
    }
    
    override suspend fun getSurahs(): Flow<Result<List<Surah>>> = flow {
        try {
            logger.d(TAG, "Fetching Surahs from API")
            val response = quranApi.getSurahs()
            logger.d(TAG, "API response - Success: ${response.isSuccessful}, Code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                logger.d(TAG, "Successfully received ${responseBody.data.size} surahs")
                
                val surahs = responseBody.data.map { dto ->
                    Surah(
                        number = dto.number,
                        name = dto.name,
                        englishName = dto.englishName,
                        englishNameTranslation = dto.englishNameTranslation,
                        numberOfAyahs = dto.numberOfAyahs,
                        revelationType = dto.revelationType,
                        ayahs = emptyList()
                    )
                }
                
                logger.i(TAG, "Successfully mapped ${surahs.size} surahs")
                emit(Result.success(surahs))
            } else {
                val error = ImaniError.IslamicContentError(
                    message = "Failed to fetch Surahs: ${response.code()} - ${response.message()}",
                    userFriendlyMessage = "Unable to load Quran chapters. Please check your connection and try again."
                )
                errorHandler.logError(TAG, "API Error", error)
                emit(Result.failure(error))
            }
        } catch (e: Exception) {
            val error = if (e is ImaniError) e else {
                errorHandler.handleError(e, TAG)
            }
            logger.e(TAG, "Exception occurred while fetching Surahs", error)
            emit(Result.failure(error))
        }
    }
    
    override suspend fun getSurah(
        surahNumber: Int,
        includeTranslation: Boolean
    ): Flow<Result<Surah>> = flow {
        try {
            logger.d(TAG, "Fetching Surah $surahNumber with translation: $includeTranslation")
            
            // Validate input
            if (surahNumber < 1 || surahNumber > 114) {
                val error = ImaniError.ValidationError(
                    message = "Invalid Surah number: $surahNumber",
                    userFriendlyMessage = "Please select a valid Surah (1-114)"
                )
                emit(Result.failure(error))
                return@flow
            }
            
            // First get Arabic text
            val arabicResponse = quranApi.getSurah(surahNumber)
            if (!arabicResponse.isSuccessful || arabicResponse.body() == null) {
                val error = ImaniError.IslamicContentError(
                    message = "Failed to fetch Arabic text: ${arabicResponse.message()}",
                    userFriendlyMessage = "Unable to load Surah content. Please try again."
                )
                emit(Result.failure(error))
                return@flow
            }
            
            val arabicData = arabicResponse.body()!!.data
            
            // Then get translation if requested
            val translationResponse = if (includeTranslation) {
                try {
                    quranApi.getSurahWithTranslation(surahNumber, "en.sahih")
                } catch (e: Exception) {
                    logger.w(TAG, "Failed to fetch translation, continuing with Arabic only", e)
                    null
                }
            } else null
            
            val translationData = translationResponse?.body()?.data
            
            // Combine Arabic and translation
            val ayahs = arabicData.ayahs?.mapIndexed { index, arabicAyah ->
                val translationAyah = translationData?.ayahs?.getOrNull(index)
                
                Ayah(
                    number = arabicAyah.number,
                    text = arabicAyah.text,
                    translation = Translation(
                        text = translationAyah?.text ?: "",
                        language = "en",
                        translator = "Sahih International"
                    ),
                    numberInSurah = arabicAyah.numberInSurah,
                    juz = arabicAyah.juz,
                    manzil = arabicAyah.manzil,
                    page = arabicAyah.page,
                    ruku = arabicAyah.ruku,
                    hizbQuarter = arabicAyah.hizbQuarter,
                    surahNumber = surahNumber
                )
            } ?: emptyList()
            
            val surah = Surah(
                number = arabicData.number ?: surahNumber,
                name = arabicData.name ?: "",
                englishName = arabicData.englishName ?: "",
                englishNameTranslation = arabicData.englishNameTranslation ?: "",
                numberOfAyahs = arabicData.numberOfAyahs ?: ayahs.size,
                revelationType = arabicData.revelationType ?: "",
                ayahs = ayahs
            )
            
            logger.i(TAG, "Successfully loaded Surah $surahNumber with ${ayahs.size} ayahs")
            emit(Result.success(surah))
            
        } catch (e: Exception) {
            val error = if (e is ImaniError) e else {
                errorHandler.handleError(e, TAG)
            }
            logger.e(TAG, "Exception occurred while fetching Surah $surahNumber", error)
            emit(Result.failure(error))
        }
    }
    
    // ... rest of the methods follow similar pattern
}
