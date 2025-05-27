package com.imaniapp.uganda.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mosques")
data class MosqueEntity(
    @PrimaryKey
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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 