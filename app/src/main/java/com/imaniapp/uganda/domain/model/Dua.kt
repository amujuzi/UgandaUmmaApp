package com.imaniapp.uganda.domain.model

data class Dua(
    val id: Long = 0,
    val title: String,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val category: DuaCategory,
    val source: DuaSource,
    val reference: String? = null,
    val benefits: String? = null,
    val occasion: String? = null,
    val isFavorite: Boolean = false,
    val isAiGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class DuaCategory(val displayName: String, val arabicName: String) {
    MORNING("Morning Adhkar", "أذكار الصباح"),
    EVENING("Evening Adhkar", "أذكار المساء"),
    PRAYER("Prayer Du'as", "أدعية الصلاة"),
    EATING("Eating & Drinking", "آداب الطعام والشراب"),
    TRAVEL("Travel Du'as", "أدعية السفر"),
    SLEEP("Sleep Du'as", "أدعية النوم"),
    PROTECTION("Protection", "الحماية والاستعاذة"),
    FORGIVENESS("Seeking Forgiveness", "الاستغفار والتوبة"),
    GUIDANCE("Seeking Guidance", "طلب الهداية"),
    HEALTH("Health & Healing", "الصحة والشفاء"),
    FAMILY("Family & Children", "الأسرة والأولاد"),
    WORK("Work & Livelihood", "العمل والرزق"),
    GRATITUDE("Gratitude & Praise", "الشكر والحمد"),
    DIFFICULTY("Times of Difficulty", "أوقات الضيق"),
    GENERAL("General Du'as", "أدعية عامة"),
    CUSTOM("Custom/AI Generated", "مخصص/مولد بالذكاء الاصطناعي")
}

enum class DuaSource(val displayName: String) {
    HISNUL_MUSLIM("Hisnul Muslim"),
    QURAN("Quran"),
    HADITH("Hadith"),
    SCHOLARS("Islamic Scholars"),
    AI_GENERATED("AI Generated"),
    USER_CUSTOM("User Custom")
}

data class DuaRequest(
    val situation: String,
    val category: DuaCategory = DuaCategory.CUSTOM,
    val language: String = "en",
    val includeArabic: Boolean = true,
    val includeTransliteration: Boolean = true,
    val includeTranslation: Boolean = true
)

data class AiDuaResponse(
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val explanation: String,
    val sources: List<String>,
    val disclaimer: String = "⚠️ This is AI-generated content. Please consult a qualified Islamic scholar for verification and guidance on religious matters."
)

data class DuaCollection(
    val id: Long = 0,
    val name: String,
    val description: String,
    val duas: List<Dua>,
    val createdAt: Long = System.currentTimeMillis()
)

// Predefined Hisnul Muslim Du'as
object HisnulMuslimDuas {
    
    val MORNING_ADHKAR = listOf(
        Dua(
            id = 1,
            title = "Morning Protection",
            arabicText = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            transliteration = "A'udhu billahi min ash-shaytani'r-rajim",
            translation = "I seek refuge in Allah from Satan, the accursed.",
            category = DuaCategory.MORNING,
            source = DuaSource.HISNUL_MUSLIM,
            reference = "Hisnul Muslim #1"
        ),
        Dua(
            id = 2,
            title = "Morning Dhikr",
            arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
            transliteration = "Allahumma bika asbahna wa bika amsayna wa bika nahya wa bika namutu wa ilayka an-nushur",
            translation = "O Allah, by You we enter the morning and by You we enter the evening, by You we live and by You we die, and to You is the resurrection.",
            category = DuaCategory.MORNING,
            source = DuaSource.HISNUL_MUSLIM,
            reference = "Hisnul Muslim #15"
        )
    )
    
    val EVENING_ADHKAR = listOf(
        Dua(
            id = 3,
            title = "Evening Protection",
            arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ",
            transliteration = "Allahumma bika amsayna wa bika asbahna wa bika nahya wa bika namutu wa ilayka al-masir",
            translation = "O Allah, by You we enter the evening and by You we enter the morning, by You we live and by You we die, and to You is our return.",
            category = DuaCategory.EVENING,
            source = DuaSource.HISNUL_MUSLIM,
            reference = "Hisnul Muslim #41"
        )
    )
    
    val EATING_DUAS = listOf(
        Dua(
            id = 4,
            title = "Before Eating",
            arabicText = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah",
            translation = "In the name of Allah.",
            category = DuaCategory.EATING,
            source = DuaSource.HISNUL_MUSLIM,
            reference = "Hisnul Muslim #85"
        ),
        Dua(
            id = 5,
            title = "After Eating",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillahi alladhi at'amani hadha wa razaqanihi min ghayri hawlin minni wa la quwwah",
            translation = "Praise be to Allah Who has fed me this food and provided it for me without any effort on my part or any power.",
            category = DuaCategory.EATING,
            source = DuaSource.HISNUL_MUSLIM,
            reference = "Hisnul Muslim #86"
        )
    )
    
    val TRAVEL_DUAS = listOf(
        Dua(
            id = 6,
            title = "Travel Du'a",
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhana alladhi sakhkhara lana hadha wa ma kunna lahu muqrinin wa inna ila rabbina la munqalibun",
            translation = "Glory be to Him Who has subjected this to us, and we could never have it (by our efforts). And verily, to our Lord we indeed are to return!",
            category = DuaCategory.TRAVEL,
            source = DuaSource.HISNUL_MUSLIM,
            reference = "Hisnul Muslim #124"
        )
    )
    
    fun getAllDuas(): List<Dua> {
        return MORNING_ADHKAR + EVENING_ADHKAR + EATING_DUAS + TRAVEL_DUAS
    }
    
    fun getDuasByCategory(category: DuaCategory): List<Dua> {
        return getAllDuas().filter { it.category == category }
    }
} 