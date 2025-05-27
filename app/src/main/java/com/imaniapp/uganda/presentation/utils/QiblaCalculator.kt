package com.imaniapp.uganda.presentation.utils

import com.imaniapp.uganda.domain.model.QiblaConstants
import com.imaniapp.uganda.domain.model.QiblaDirection
import kotlin.math.*

object QiblaCalculator {
    
    /**
     * Calculate Qibla direction from user's location to Kaaba
     * @param userLatitude User's latitude in degrees
     * @param userLongitude User's longitude in degrees
     * @return QiblaDirection object with bearing and distance
     */
    fun calculateQiblaDirection(
        userLatitude: Double,
        userLongitude: Double
    ): QiblaDirection {
        val bearing = calculateBearing(
            userLatitude, userLongitude,
            QiblaConstants.KAABA_LATITUDE, QiblaConstants.KAABA_LONGITUDE
        )
        
        val distance = calculateDistance(
            userLatitude, userLongitude,
            QiblaConstants.KAABA_LATITUDE, QiblaConstants.KAABA_LONGITUDE
        )
        
        return QiblaDirection(
            bearing = bearing.toFloat(),
            distance = distance,
            isAccurate = true,
            userLatitude = userLatitude,
            userLongitude = userLongitude
        )
    }
    
    /**
     * Calculate bearing from point A to point B
     * @param lat1 Latitude of point A
     * @param lon1 Longitude of point A
     * @param lat2 Latitude of point B
     * @param lon2 Longitude of point B
     * @return Bearing in degrees (0-360)
     */
    private fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLonRad = Math.toRadians(lon2 - lon1)
        
        val y = sin(deltaLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)
        
        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        
        // Normalize to 0-360 degrees
        return (bearingDeg + 360) % 360
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of point A
     * @param lon1 Longitude of point A
     * @param lat2 Latitude of point B
     * @param lon2 Longitude of point B
     * @return Distance in kilometers
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLatRad = Math.toRadians(lat2 - lat1)
        val deltaLonRad = Math.toRadians(lon2 - lon1)
        
        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLonRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Normalize compass bearing to 0-360 degrees
     */
    fun normalizeBearing(bearing: Float): Float {
        return ((bearing % 360) + 360) % 360
    }
    
    /**
     * Get compass direction text based on bearing
     */
    fun getCompassDirection(bearing: Float): String {
        val normalizedBearing = normalizeBearing(bearing)
        return when {
            normalizedBearing < 11.25 || normalizedBearing >= 348.75 -> "N"
            normalizedBearing < 33.75 -> "NNE"
            normalizedBearing < 56.25 -> "NE"
            normalizedBearing < 78.75 -> "ENE"
            normalizedBearing < 101.25 -> "E"
            normalizedBearing < 123.75 -> "ESE"
            normalizedBearing < 146.25 -> "SE"
            normalizedBearing < 168.75 -> "SSE"
            normalizedBearing < 191.25 -> "S"
            normalizedBearing < 213.75 -> "SSW"
            normalizedBearing < 236.25 -> "SW"
            normalizedBearing < 258.75 -> "WSW"
            normalizedBearing < 281.25 -> "W"
            normalizedBearing < 303.75 -> "WNW"
            normalizedBearing < 326.25 -> "NW"
            normalizedBearing < 348.75 -> "NNW"
            else -> "N"
        }
    }
    
    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10 -> String.format("%.1f km", distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }
} 