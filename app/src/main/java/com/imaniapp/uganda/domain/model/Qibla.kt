package com.imaniapp.uganda.domain.model

data class QiblaDirection(
    val bearing: Float,
    val distance: Double,
    val isAccurate: Boolean,
    val userLatitude: Double,
    val userLongitude: Double
)

data class CompassReading(
    val azimuth: Float,
    val pitch: Float,
    val roll: Float,
    val accuracy: Int
)

object QiblaConstants {
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262
} 