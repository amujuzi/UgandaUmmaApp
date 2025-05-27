package com.imaniapp.uganda.data.repository

import com.imaniapp.uganda.data.local.dao.QuranBookmarkDao
import com.imaniapp.uganda.data.local.dao.ReadingProgressDao
import com.imaniapp.uganda.data.local.entity.QuranBookmarkEntity
import com.imaniapp.uganda.data.local.entity.ReadingProgressEntity
import com.imaniapp.uganda.data.remote.api.QuranApi
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val quranApi: QuranApi,
    private val bookmarkDao: QuranBookmarkDao,
    private val readingProgressDao: ReadingProgressDao
) : QuranRepository {
    
    override suspend fun getSurahs(): Flow<Result<List<Surah>>> = flow {
        try {
            println("QuranRepository: Making API call to getSurahs")
            val response = quranApi.getSurahs()
            println("QuranRepository: API response - Success: ${response.isSuccessful}, Code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                println("QuranRepository: Response body received with ${responseBody.data.size} surahs")
                
                val surahs = responseBody.data.map { dto ->
                    Surah(
                        number = dto.number,
                        name = dto.name,
                        englishName = dto.englishName,
                        englishNameTranslation = dto.englishNameTranslation,
                        numberOfAyahs = dto.numberOfAyahs,
                        revelationType = dto.revelationType,
                        ayahs = emptyList() // Will be loaded separately
                    )
                }
                println("QuranRepository: Successfully mapped ${surahs.size} surahs")
                emit(Result.success(surahs))
            } else {
                val errorMsg = "Failed to fetch Surahs: ${response.code()} - ${response.message()}"
                println("QuranRepository: API Error - $errorMsg")
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            println("QuranRepository: Exception occurred - ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getSurah(
        surahNumber: Int,
        includeTranslation: Boolean
    ): Flow<Result<Surah>> = flow {
        try {
            // First get Arabic text
            val arabicResponse = quranApi.getSurah(surahNumber)
            if (!arabicResponse.isSuccessful || arabicResponse.body() == null) {
                emit(Result.failure(Exception("Failed to fetch Arabic text: ${arabicResponse.message()}")))
                return@flow
            }
            
            val arabicData = arabicResponse.body()!!.data
            
            // Then get translation if requested
            val translationResponse = if (includeTranslation) {
                quranApi.getSurahWithTranslation(surahNumber, "en.sahih")
            } else null
            
            val translationData = translationResponse?.body()?.data
            
            // Combine Arabic and translation
            val ayahs = arabicData.ayahs?.mapIndexed { index, arabicAyah ->
                val translationAyah = translationData?.ayahs?.getOrNull(index)
                
                Ayah(
                    number = arabicAyah.number,
                    text = arabicAyah.text,
                    translation = Translation(
                        text = translationAyah?.text ?: "",
                        language = "en",
                        translator = "Sahih International"
                    ),
                    numberInSurah = arabicAyah.numberInSurah,
                    juz = arabicAyah.juz,
                    manzil = arabicAyah.manzil,
                    page = arabicAyah.page,
                    ruku = arabicAyah.ruku,
                    hizbQuarter = arabicAyah.hizbQuarter,
                    surahNumber = surahNumber
                )
            } ?: emptyList()
            
            val surah = Surah(
                number = arabicData.number ?: surahNumber,
                name = arabicData.name ?: "",
                englishName = arabicData.englishName ?: "",
                englishNameTranslation = arabicData.englishNameTranslation ?: "",
                numberOfAyahs = arabicData.numberOfAyahs ?: ayahs.size,
                revelationType = arabicData.revelationType ?: "",
                ayahs = ayahs
            )
            
            emit(Result.success(surah))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getAyah(
        surahNumber: Int,
        ayahNumber: Int
    ): Flow<Result<Ayah>> = flow {
        try {
            val reference = "$surahNumber:$ayahNumber"
            
            // Get Arabic text
            val arabicResponse = quranApi.getAyah(reference)
            if (!arabicResponse.isSuccessful || arabicResponse.body() == null) {
                emit(Result.failure(Exception("Failed to fetch Ayah: ${arabicResponse.message()}")))
                return@flow
            }
            
            // Get translation
            val translationResponse = quranApi.getAyahWithTranslation(reference, "en.sahih")
            
            val arabicAyah = arabicResponse.body()!!.data.ayahs?.firstOrNull()
            val translationAyah = translationResponse.body()?.data?.ayahs?.firstOrNull()
            
            if (arabicAyah != null) {
                val ayah = Ayah(
                    number = arabicAyah.number,
                    text = arabicAyah.text,
                    translation = Translation(
                        text = translationAyah?.text ?: "",
                        language = "en",
                        translator = "Sahih International"
                    ),
                    numberInSurah = arabicAyah.numberInSurah,
                    juz = arabicAyah.juz,
                    manzil = arabicAyah.manzil,
                    page = arabicAyah.page,
                    ruku = arabicAyah.ruku,
                    hizbQuarter = arabicAyah.hizbQuarter,
                    surahNumber = surahNumber
                )
                emit(Result.success(ayah))
            } else {
                emit(Result.failure(Exception("Ayah not found")))
            }
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    override suspend fun searchQuran(
        query: String,
        surahNumber: Int?
    ): Flow<Result<List<Ayah>>> = flow {
        try {
            val response = quranApi.searchQuran(query, surahNumber)
            if (response.isSuccessful && response.body() != null) {
                val ayahs = response.body()!!.data.ayahs?.map { dto ->
                    Ayah(
                        number = dto.number,
                        text = dto.text,
                        translation = Translation(
                            text = "",
                            language = "en",
                            translator = "Search Result"
                        ),
                        numberInSurah = dto.numberInSurah,
                        juz = dto.juz,
                        manzil = dto.manzil,
                        page = dto.page,
                        ruku = dto.ruku,
                        hizbQuarter = dto.hizbQuarter,
                        surahNumber = dto.surah?.number ?: 1
                    )
                } ?: emptyList()
                
                emit(Result.success(ayahs))
            } else {
                emit(Result.failure(Exception("Search failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getBookmarks(): Flow<List<QuranBookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { entity ->
                QuranBookmark(
                    id = entity.id,
                    surahNumber = entity.surahNumber,
                    ayahNumber = entity.ayahNumber,
                    surahName = entity.surahName,
                    ayahText = entity.ayahText,
                    translation = entity.translation ?: "",
                    note = entity.note,
                    createdAt = entity.createdAt
                )
            }
        }
    }
    
    override suspend fun addBookmark(bookmark: QuranBookmark) {
        val entity = QuranBookmarkEntity(
            id = bookmark.id,
            surahNumber = bookmark.surahNumber,
            ayahNumber = bookmark.ayahNumber,
            surahName = bookmark.surahName,
            ayahText = bookmark.ayahText,
            translation = bookmark.translation,
            note = bookmark.note,
            createdAt = bookmark.createdAt
        )
        bookmarkDao.insertBookmark(entity)
    }
    
    override suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        val bookmark = bookmarkDao.getBookmarkByAyah(surahNumber, ayahNumber)
        bookmark?.let { bookmarkDao.deleteBookmark(it) }
    }
    
    override suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean {
        return bookmarkDao.isBookmarked(surahNumber, ayahNumber)
    }
    
    override suspend fun getReadingProgress(surahNumber: Int): Int {
        return readingProgressDao.getReadingProgress(surahNumber)?.lastAyahRead ?: 0
    }
    
    override suspend fun updateReadingProgress(surahNumber: Int, lastAyahRead: Int) {
        val progress = ReadingProgressEntity(
            surahNumber = surahNumber,
            lastAyahRead = lastAyahRead
        )
        readingProgressDao.insertOrUpdateProgress(progress)
    }
} 