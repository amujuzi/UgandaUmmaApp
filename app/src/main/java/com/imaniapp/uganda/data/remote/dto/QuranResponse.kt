package com.imaniapp.uganda.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuranResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: QuranDataDto
)

data class SurahListResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<SurahInfoDto>
)

data class QuranDataDto(
    @SerializedName("number")
    val number: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("englishName")
    val englishName: String? = null,
    @SerializedName("englishNameTranslation")
    val englishNameTranslation: String? = null,
    @SerializedName("numberOfAyahs")
    val numberOfAyahs: Int? = null,
    @SerializedName("revelationType")
    val revelationType: String? = null,
    @SerializedName("ayahs")
    val ayahs: List<AyahDto>? = null,
    @SerializedName("edition")
    val edition: EditionDto? = null
)

data class SurahInfoDto(
    @SerializedName("number")
    val number: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("englishName")
    val englishName: String,
    @SerializedName("englishNameTranslation")
    val englishNameTranslation: String,
    @SerializedName("numberOfAyahs")
    val numberOfAyahs: Int,
    @SerializedName("revelationType")
    val revelationType: String
)

data class AyahDto(
    @SerializedName("number")
    val number: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("numberInSurah")
    val numberInSurah: Int,
    @SerializedName("juz")
    val juz: Int,
    @SerializedName("manzil")
    val manzil: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("ruku")
    val ruku: Int,
    @SerializedName("hizbQuarter")
    val hizbQuarter: Int,
    @SerializedName("sajda")
    val sajda: com.google.gson.JsonElement? = null,
    @SerializedName("surah")
    val surah: SurahInfoDto? = null
)

data class EditionDto(
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("englishName")
    val englishName: String,
    @SerializedName("format")
    val format: String,
    @SerializedName("type")
    val type: String
)

data class SajdaDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("recommended")
    val recommended: Boolean,
    @SerializedName("obligatory")
    val obligatory: Boolean
) 