package com.imaniapp.uganda.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val surahNumber: Int,
    val lastAyahRead: Int,
    val updatedAt: Long = System.currentTimeMillis()
) 