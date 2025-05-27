package com.imaniapp.uganda.domain.model

data class Mosque(
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
    val distance: Double? = null, // Distance from user in kilometers
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MosqueFilter(
    val hasJummah: Boolean = false,
    val hasWomenPrayer: Boolean = false,
    val hasWudu: Boolean = false,
    val maxDistance: Double? = null // in kilometers
)

data class Location(
    val latitude: Double,
    val longitude: Double
) 