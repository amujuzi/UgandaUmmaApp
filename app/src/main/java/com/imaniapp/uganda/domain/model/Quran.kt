package com.imaniapp.uganda.domain.model

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
    val ayahs: List<Ayah> = emptyList()
)

data class Ayah(
    val number: Int,
    val text: String,
    val translation: Translation,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val surahNumber: Int,
    val sajda: Boolean = false
)

data class Translation(
    val text: String,
    val language: String,
    val translator: String
)

data class QuranBookmark(
    val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahText: String,
    val translation: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReadingProgress(
    val lastReadSurah: Int,
    val lastReadAyah: Int,
    val totalAyahsRead: Int,
    val readingStreak: Int,
    val lastReadDate: String
) 