package com.imaniapp.uganda.data.remote.api

import com.imaniapp.uganda.data.remote.dto.GooglePlacesResponse
import com.imaniapp.uganda.data.remote.dto.PlaceDetailsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyMosques(
        @Query("location") location: String, // "lat,lng"
        @Query("radius") radius: Int, // in meters
        @Query("type") type: String = "mosque",
        @Query("keyword") keyword: String = "mosque masjid",
        @Query("key") apiKey: String
    ): Response<GooglePlacesResponse>
    
    @GET("maps/api/place/textsearch/json")
    suspend fun searchMosques(
        @Query("query") query: String,
        @Query("location") location: String? = null,
        @Query("radius") radius: Int? = null,
        @Query("type") type: String = "mosque",
        @Query("key") apiKey: String
    ): Response<GooglePlacesResponse>
    
    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "name,formatted_address,geometry,formatted_phone_number,website,opening_hours,photos,rating,reviews",
        @Query("key") apiKey: String
    ): Response<PlaceDetailsResponse>
    
    companion object {
        const val BASE_URL = "https://maps.googleapis.com/"
        
        // Search radius constants (in meters)
        const val RADIUS_1KM = 1000
        const val RADIUS_5KM = 5000
        const val RADIUS_10KM = 10000
        const val RADIUS_25KM = 25000
        const val RADIUS_50KM = 50000
        
        // Place types
        const val TYPE_MOSQUE = "mosque"
        const val TYPE_PLACE_OF_WORSHIP = "place_of_worship"
        
        // Keywords for better mosque detection
        const val KEYWORDS_MOSQUE = "mosque masjid islamic center jamia"
    }
} 