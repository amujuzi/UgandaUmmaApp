package com.imaniapp.uganda.data.repository

import com.imaniapp.uganda.data.local.dao.DuaDao
import com.imaniapp.uganda.data.local.entity.DuaEntity
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.DuaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuaRepositoryImpl @Inject constructor(
    private val duaDao: DuaDao
) : DuaRepository {
    
    override suspend fun getAllDuas(): Flow<List<Dua>> {
        return duaDao.getAllDuas().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getDuasByCategory(category: DuaCategory): Flow<List<Dua>> {
        return duaDao.getDuasByCategory(category.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getFavoriteDuas(): Flow<List<Dua>> {
        return duaDao.getFavoriteDuas().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getAiGeneratedDuas(): Flow<List<Dua>> {
        return duaDao.getAiGeneratedDuas().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getHisnulMuslimDuas(): Flow<List<Dua>> {
        return duaDao.getDuasBySource(DuaSource.HISNUL_MUSLIM.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun searchDuas(query: String): Flow<List<Dua>> {
        return duaDao.searchDuas(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getDuaById(id: Long): Dua? {
        return duaDao.getDuaById(id)?.toDomainModel()
    }
    
    override suspend fun addToFavorites(duaId: Long) {
        duaDao.updateFavoriteStatus(duaId, true)
    }
    
    override suspend fun removeFromFavorites(duaId: Long) {
        duaDao.updateFavoriteStatus(duaId, false)
    }
    
    override suspend fun saveDua(dua: Dua): Long {
        return duaDao.insertDua(dua.toEntity())
    }
    
    override suspend fun deleteDua(duaId: Long) {
        duaDao.deleteDuaById(duaId)
    }
    
    override suspend fun generateAiDua(request: DuaRequest): Result<AiDuaResponse> {
        return try {
            // For now, we'll create a mock AI response
            // In a real implementation, this would call an AI service like OpenAI
            val mockResponse = generateMockAiResponse(request)
            Result.success(mockResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveAiDua(aiResponse: AiDuaResponse, request: DuaRequest): Long {
        val dua = Dua(
            title = "AI Generated Du'a for ${request.situation}",
            arabicText = aiResponse.arabicText,
            transliteration = aiResponse.transliteration,
            translation = aiResponse.translation,
            category = request.category,
            source = DuaSource.AI_GENERATED,
            reference = "AI Generated",
            benefits = aiResponse.explanation,
            occasion = request.situation,
            isAiGenerated = true
        )
        return saveDua(dua)
    }
    
    override suspend fun initializeHisnulMuslimDuas() {
        if (!isHisnulMuslimInitialized()) {
            val hisnulMuslimDuas = HisnulMuslimDuas.getAllDuas()
            hisnulMuslimDuas.forEach { dua ->
                duaDao.insertDua(dua.toEntity())
            }
        }
    }
    
    override suspend fun isHisnulMuslimInitialized(): Boolean {
        return duaDao.getHisnulMuslimCount() > 0
    }
    
    private fun generateMockAiResponse(request: DuaRequest): AiDuaResponse {
        // This is a mock implementation. In production, you would integrate with:
        // - OpenAI API
        // - Google Gemini
        // - Anthropic Claude
        // - Or a custom Islamic AI model
        
        val mockResponses = mapOf(
            "travel" to AiDuaResponse(
                arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                transliteration = "Rabbana atina fi'd-dunya hasanatan wa fi'l-akhirati hasanatan wa qina 'adhab an-nar",
                translation = "Our Lord, give us good in this world and good in the next world, and save us from the punishment of the Fire.",
                explanation = "This is a comprehensive du'a from the Quran (2:201) that asks Allah for goodness in both this life and the hereafter, while seeking protection from hellfire.",
                sources = listOf("Quran 2:201", "Sahih Bukhari", "Sahih Muslim")
            ),
            "exam" to AiDuaResponse(
                arabicText = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَاحْلُلْ عُقْدَةً مِنْ لِسَانِي يَفْقَهُوا قَوْلِي",
                transliteration = "Rabbi ishrah li sadri wa yassir li amri wahlul 'uqdatan min lisani yafqahu qawli",
                translation = "My Lord, expand for me my breast and ease for me my task and untie the knot from my tongue that they may understand my speech.",
                explanation = "This is the du'a of Prophet Musa (Moses) when he was given the task to speak to Pharaoh. It's perfect for seeking clarity and ease in communication and understanding.",
                sources = listOf("Quran 20:25-28")
            ),
            "health" to AiDuaResponse(
                arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي اللَّهُمَّ عَافِنِي فِي سَمْعِي اللَّهُمَّ عَافِنِي فِي بَصَرِي",
                transliteration = "Allahumma 'afini fi badani, Allahumma 'afini fi sam'i, Allahumma 'afini fi basari",
                translation = "O Allah, grant me health in my body. O Allah, grant me health in my hearing. O Allah, grant me health in my sight.",
                explanation = "This du'a seeks Allah's protection and health for the body and senses. It's recommended to recite this daily for overall well-being.",
                sources = listOf("Abu Dawud", "Ahmad")
            )
        )
        
        // Find the most relevant response based on the request situation
        val situation = request.situation.lowercase()
        val response = when {
            situation.contains("travel") || situation.contains("journey") -> mockResponses["travel"]
            situation.contains("exam") || situation.contains("test") || situation.contains("study") -> mockResponses["exam"]
            situation.contains("health") || situation.contains("sick") || situation.contains("healing") -> mockResponses["health"]
            else -> mockResponses["travel"] // Default fallback
        }
        
        return response ?: mockResponses["travel"]!!
    }
    
    private fun DuaEntity.toDomainModel(): Dua {
        return Dua(
            id = id,
            title = title,
            arabicText = arabicText,
            transliteration = transliteration,
            translation = translation,
            category = DuaCategory.valueOf(category),
            source = DuaSource.valueOf(source),
            reference = reference,
            benefits = benefits,
            occasion = occasion,
            isFavorite = isFavorite,
            isAiGenerated = isAiGenerated,
            createdAt = createdAt
        )
    }
    
    private fun Dua.toEntity(): DuaEntity {
        return DuaEntity(
            id = if (id == 0L) 0 else id, // Let Room auto-generate if 0
            title = title,
            arabicText = arabicText,
            transliteration = transliteration,
            translation = translation,
            category = category.name,
            source = source.name,
            reference = reference,
            benefits = benefits,
            occasion = occasion,
            isFavorite = isFavorite,
            isAiGenerated = isAiGenerated,
            createdAt = createdAt
        )
    }
} 