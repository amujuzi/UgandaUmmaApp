package com.imaniapp.uganda.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey
    val id: String, // Format: "YYYY-MM-DD"
    val date: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val method: Int = 2, // Islamic Society of North America (ISNA)
    val createdAt: Long = System.currentTimeMillis()
) 