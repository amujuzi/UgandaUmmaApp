package com.imaniapp.uganda.data.remote.api

import com.imaniapp.uganda.data.remote.dto.QuranResponse
import com.imaniapp.uganda.data.remote.dto.SurahListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApi {
    
    companion object {
        const val BASE_URL = "https://api.alquran.cloud/v1/"
    }
    
    /**
     * Get list of all Surahs
     */
    @GET("surah")
    suspend fun getSurahs(): Response<SurahListResponse>
    
    /**
     * Get specific Surah with Arabic text
     * @param surahNumber Surah number (1-114)
     */
    @GET("surah/{number}")
    suspend fun getSurah(
        @Path("number") surahNumber: Int
    ): Response<QuranResponse>
    
    /**
     * Get specific Surah with translation
     * @param surahNumber Surah number (1-114)
     * @param edition Translation edition (e.g., "en.sahih", "en.pickthall")
     */
    @GET("surah/{number}/{edition}")
    suspend fun getSurahWithTranslation(
        @Path("number") surahNumber: Int,
        @Path("edition") edition: String = "en.sahih"
    ): Response<QuranResponse>
    
    /**
     * Get specific Ayah
     * @param reference Ayah reference (e.g., "2:255" for Ayat al-Kursi)
     */
    @GET("ayah/{reference}")
    suspend fun getAyah(
        @Path("reference") reference: String
    ): Response<QuranResponse>
    
    /**
     * Get specific Ayah with translation
     * @param reference Ayah reference (e.g., "2:255")
     * @param edition Translation edition
     */
    @GET("ayah/{reference}/{edition}")
    suspend fun getAyahWithTranslation(
        @Path("reference") reference: String,
        @Path("edition") edition: String = "en.sahih"
    ): Response<QuranResponse>
    
    /**
     * Search in Quran
     * @param query Search query
     * @param surah Optional: specific surah to search in
     */
    @GET("search/{query}")
    suspend fun searchQuran(
        @Path("query") query: String,
        @Query("surah") surah: Int? = null
    ): Response<QuranResponse>
} 