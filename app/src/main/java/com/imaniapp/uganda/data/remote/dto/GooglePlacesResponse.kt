package com.imaniapp.uganda.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GooglePlacesResponse(
    @SerializedName("results")
    val results: List<PlaceResult>,
    @SerializedName("status")
    val status: String,
    @SerializedName("next_page_token")
    val nextPageToken: String? = null,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class PlaceResult(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("formatted_address")
    val formattedAddress: String? = null,
    @SerializedName("vicinity")
    val vicinity: String? = null,
    @SerializedName("geometry")
    val geometry: PlaceGeometry,
    @SerializedName("rating")
    val rating: Double? = null,
    @SerializedName("user_ratings_total")
    val userRatingsTotal: Int? = null,
    @SerializedName("price_level")
    val priceLevel: Int? = null,
    @SerializedName("photos")
    val photos: List<PlacePhoto>? = null,
    @SerializedName("opening_hours")
    val openingHours: PlaceOpeningHours? = null,
    @SerializedName("plus_code")
    val plusCode: PlusCode? = null,
    @SerializedName("types")
    val types: List<String> = emptyList(),
    @SerializedName("business_status")
    val businessStatus: String? = null
)

data class PlaceGeometry(
    @SerializedName("location")
    val location: PlaceLocation,
    @SerializedName("viewport")
    val viewport: PlaceViewport? = null
)

data class PlaceLocation(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)

data class PlaceViewport(
    @SerializedName("northeast")
    val northeast: PlaceLocation,
    @SerializedName("southwest")
    val southwest: PlaceLocation
)

data class PlacePhoto(
    @SerializedName("height")
    val height: Int,
    @SerializedName("width")
    val width: Int,
    @SerializedName("photo_reference")
    val photoReference: String,
    @SerializedName("html_attributions")
    val htmlAttributions: List<String>
)

data class PlaceOpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean? = null,
    @SerializedName("periods")
    val periods: List<OpeningPeriod>? = null,
    @SerializedName("weekday_text")
    val weekdayText: List<String>? = null
)

data class OpeningPeriod(
    @SerializedName("close")
    val close: OpeningTime? = null,
    @SerializedName("open")
    val open: OpeningTime
)

data class OpeningTime(
    @SerializedName("day")
    val day: Int,
    @SerializedName("time")
    val time: String
)

data class PlusCode(
    @SerializedName("compound_code")
    val compoundCode: String? = null,
    @SerializedName("global_code")
    val globalCode: String
)

// Place Details Response
data class PlaceDetailsResponse(
    @SerializedName("result")
    val result: PlaceDetails,
    @SerializedName("status")
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class PlaceDetails(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    @SerializedName("geometry")
    val geometry: PlaceGeometry,
    @SerializedName("formatted_phone_number")
    val formattedPhoneNumber: String? = null,
    @SerializedName("international_phone_number")
    val internationalPhoneNumber: String? = null,
    @SerializedName("website")
    val website: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("rating")
    val rating: Double? = null,
    @SerializedName("user_ratings_total")
    val userRatingsTotal: Int? = null,
    @SerializedName("reviews")
    val reviews: List<PlaceReview>? = null,
    @SerializedName("photos")
    val photos: List<PlacePhoto>? = null,
    @SerializedName("opening_hours")
    val openingHours: PlaceOpeningHours? = null,
    @SerializedName("types")
    val types: List<String> = emptyList(),
    @SerializedName("business_status")
    val businessStatus: String? = null
)

data class PlaceReview(
    @SerializedName("author_name")
    val authorName: String,
    @SerializedName("author_url")
    val authorUrl: String? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String? = null,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("relative_time_description")
    val relativeTimeDescription: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("time")
    val time: Long
) 