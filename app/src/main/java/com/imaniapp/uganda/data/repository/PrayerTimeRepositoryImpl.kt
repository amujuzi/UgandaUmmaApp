package com.imaniapp.uganda.data.repository

import com.imaniapp.uganda.data.local.dao.PrayerTimeDao
import com.imaniapp.uganda.data.local.entity.PrayerTimeEntity
import com.imaniapp.uganda.data.remote.api.PrayerTimeApi
import com.imaniapp.uganda.domain.model.Prayer
import com.imaniapp.uganda.domain.model.PrayerTime
import com.imaniapp.uganda.domain.model.PrayerTimeStatus
import com.imaniapp.uganda.domain.repository.PrayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerTimeRepositoryImpl @Inject constructor(
    private val api: PrayerTimeApi,
    private val dao: PrayerTimeDao
) : PrayerTimeRepository {
    
    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: String?
    ): Result<PrayerTime> {
        return try {
            val response = if (date != null) {
                api.getPrayerTimesByDate(latitude, longitude, date)
            } else {
                api.getPrayerTimes(latitude, longitude)
            }
            
            if (response.isSuccessful && response.body() != null) {
                val prayerTimeResponse = response.body()!!
                val timings = prayerTimeResponse.data.timings
                val dateInfo = prayerTimeResponse.data.date.gregorian.date
                
                val prayerTime = PrayerTime(
                    date = dateInfo,
                    fajr = timings.fajr.substringBefore(" "),
                    sunrise = timings.sunrise.substringBefore(" "),
                    dhuhr = timings.dhuhr.substringBefore(" "),
                    asr = timings.asr.substringBefore(" "),
                    maghrib = timings.maghrib.substringBefore(" "),
                    isha = timings.isha.substringBefore(" "),
                    latitude = latitude,
                    longitude = longitude,
                    timezone = prayerTimeResponse.data.meta.timezone,
                    method = prayerTimeResponse.data.meta.method.id
                )
                
                // Cache the result
                cachePrayerTimes(prayerTime)
                
                Result.success(prayerTime)
            } else {
                Result.failure(Exception("Failed to fetch prayer times: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Try to get from cache if network fails
            val cachedData = dao.getPrayerTimeByDate(date ?: getCurrentDateString())
            if (cachedData != null) {
                Result.success(cachedData.toDomainModel())
            } else {
                Result.failure(e)
            }
        }
    }
    
    override fun getPrayerTimesFlow(date: String): Flow<PrayerTime?> {
        return dao.getPrayerTimeByDateFlow(date).map { it?.toDomainModel() }
    }
    
    override fun getRecentPrayerTimes(): Flow<List<PrayerTime>> {
        return dao.getRecentPrayerTimes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCurrentPrayerStatus(prayerTime: PrayerTime): PrayerTimeStatus {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        
        val prayerTimes = listOf(
            Prayer.FAJR to timeToMinutes(prayerTime.fajr),
            Prayer.SUNRISE to timeToMinutes(prayerTime.sunrise),
            Prayer.DHUHR to timeToMinutes(prayerTime.dhuhr),
            Prayer.ASR to timeToMinutes(prayerTime.asr),
            Prayer.MAGHRIB to timeToMinutes(prayerTime.maghrib),
            Prayer.ISHA to timeToMinutes(prayerTime.isha)
        ).sortedBy { it.second }
        
        var currentPrayer = Prayer.ISHA
        var nextPrayer = Prayer.FAJR
        var timeUntilNext = 0
        
        for (i in prayerTimes.indices) {
            val (prayer, time) = prayerTimes[i]
            if (currentTimeInMinutes < time) {
                nextPrayer = prayer
                timeUntilNext = time - currentTimeInMinutes
                currentPrayer = if (i > 0) prayerTimes[i - 1].first else Prayer.ISHA
                break
            }
        }
        
        // If we're past all prayers for today, next prayer is Fajr tomorrow
        if (timeUntilNext == 0) {
            nextPrayer = Prayer.FAJR
            timeUntilNext = (24 * 60) - currentTimeInMinutes + timeToMinutes(prayerTime.fajr)
            currentPrayer = Prayer.ISHA
        }
        
        val timeUntilNextString = formatTimeUntil(timeUntilNext)
        val isTimeForPrayer = timeUntilNext <= 15 // 15 minutes before prayer time
        
        return PrayerTimeStatus(
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            timeUntilNext = timeUntilNextString,
            isTimeForPrayer = isTimeForPrayer
        )
    }
    
    override suspend fun cachePrayerTimes(prayerTime: PrayerTime) {
        val entity = PrayerTimeEntity(
            id = prayerTime.date,
            date = prayerTime.date,
            fajr = prayerTime.fajr,
            sunrise = prayerTime.sunrise,
            dhuhr = prayerTime.dhuhr,
            asr = prayerTime.asr,
            maghrib = prayerTime.maghrib,
            isha = prayerTime.isha,
            latitude = prayerTime.latitude,
            longitude = prayerTime.longitude,
            timezone = prayerTime.timezone,
            method = prayerTime.method
        )
        dao.insertPrayerTime(entity)
    }
    
    override suspend fun clearOldCache() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        dao.deleteOldPrayerTimes(thirtyDaysAgo)
    }
    
    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
    
    private fun formatTimeUntil(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins}m"
        }
    }
    
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    
    private fun PrayerTimeEntity.toDomainModel(): PrayerTime {
        return PrayerTime(
            date = date,
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha,
            latitude = latitude,
            longitude = longitude,
            timezone = timezone,
            method = method
        )
    }
} 