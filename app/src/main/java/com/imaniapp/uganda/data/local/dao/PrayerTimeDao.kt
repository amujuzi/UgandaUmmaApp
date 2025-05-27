package com.imaniapp.uganda.data.local.dao

import androidx.room.*
import com.imaniapp.uganda.data.local.entity.PrayerTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {
    
    @Query("SELECT * FROM prayer_times WHERE id = :date")
    suspend fun getPrayerTimeByDate(date: String): PrayerTimeEntity?
    
    @Query("SELECT * FROM prayer_times WHERE id = :date")
    fun getPrayerTimeByDateFlow(date: String): Flow<PrayerTimeEntity?>
    
    @Query("SELECT * FROM prayer_times ORDER BY date DESC LIMIT 30")
    fun getRecentPrayerTimes(): Flow<List<PrayerTimeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTimeEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTimes: List<PrayerTimeEntity>)
    
    @Delete
    suspend fun deletePrayerTime(prayerTime: PrayerTimeEntity)
    
    @Query("DELETE FROM prayer_times WHERE createdAt < :timestamp")
    suspend fun deleteOldPrayerTimes(timestamp: Long)
    
    @Query("DELETE FROM prayer_times")
    suspend fun deleteAllPrayerTimes()
} 