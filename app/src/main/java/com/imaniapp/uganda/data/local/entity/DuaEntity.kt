package com.imaniapp.uganda.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duas")
data class DuaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val category: String, // Store as string for Room
    val source: String, // Store as string for Room
    val reference: String? = null,
    val benefits: String? = null,
    val occasion: String? = null,
    val isFavorite: Boolean = false,
    val isAiGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) 