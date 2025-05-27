package com.imaniapp.uganda.domain.model

data class PrayerTime(
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
    val method: Int = 2
)

data class PrayerTimeStatus(
    val currentPrayer: Prayer,
    val nextPrayer: Prayer,
    val timeUntilNext: String,
    val isTimeForPrayer: Boolean
)

enum class Prayer(val displayName: String, val arabicName: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء")
} 