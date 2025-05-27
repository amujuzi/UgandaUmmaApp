package com.imaniapp.uganda.domain.repository

import com.imaniapp.uganda.domain.model.PrayerTime
import com.imaniapp.uganda.domain.model.PrayerTimeStatus
import kotlinx.coroutines.flow.Flow

interface PrayerTimeRepository {
    
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String? = null
    ): Result<PrayerTime>
    
    fun getPrayerTimesFlow(date: String): Flow<PrayerTime?>
    
    fun getRecentPrayerTimes(): Flow<List<PrayerTime>>
    
    suspend fun getCurrentPrayerStatus(
        prayerTime: PrayerTime
    ): PrayerTimeStatus
    
    suspend fun cachePrayerTimes(prayerTime: PrayerTime)
    
    suspend fun clearOldCache()
} 