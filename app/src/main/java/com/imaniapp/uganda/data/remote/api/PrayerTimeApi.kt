package com.imaniapp.uganda.data.remote.api

import com.imaniapp.uganda.data.remote.dto.PrayerTimeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerTimeApi {
    
    @GET("v1/timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2, // ISNA method
        @Query("school") school: Int = 0, // Shafi school
        @Query("tune") tune: String = "0,0,0,0,0,0,0,0,0"
    ): Response<PrayerTimeResponse>
    
    @GET("v1/timings")
    suspend fun getPrayerTimesByDate(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("date") date: String, // Format: DD-MM-YYYY
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0,
        @Query("tune") tune: String = "0,0,0,0,0,0,0,0,0"
    ): Response<PrayerTimeResponse>
    
    companion object {
        const val BASE_URL = "https://api.aladhan.com/"
        
        // Prayer calculation methods
        const val METHOD_ISNA = 2
        const val METHOD_MWL = 3
        const val METHOD_EGYPT = 5
        const val METHOD_MAKKAH = 4
        const val METHOD_KARACHI = 1
        
        // Juristic schools
        const val SCHOOL_SHAFI = 0
        const val SCHOOL_HANAFI = 1
    }
} 