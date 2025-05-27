package com.imaniapp.uganda.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranUiState(
    val isLoading: Boolean = false,
    val surahs: List<Surah> = emptyList(),
    val currentSurah: Surah? = null,
    val searchResults: List<Ayah> = emptyList(),
    val bookmarks: List<QuranBookmark> = emptyList(),
    val readingProgress: Map<Int, Int> = emptyMap(),
    val error: String? = null,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val selectedAyah: Ayah? = null,
    val showTranslation: Boolean = true,
    val fontSize: Float = 18f,
    val currentPage: Int = 1
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()
    
    init {
        loadSurahs()
        loadBookmarks()
    }
    
    fun loadSurahs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                quranRepository.getSurahs()
                    .catch { exception ->
                        println("QuranViewModel: Exception in getSurahs flow: ${exception.message}")
                        exception.printStackTrace()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load Surahs: ${exception.message}"
                        )
                    }
                    .collect { result ->
                        println("QuranViewModel: Received result from getSurahs")
                        result.fold(
                            onSuccess = { surahs ->
                                println("QuranViewModel: Successfully loaded ${surahs.size} surahs")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    surahs = surahs,
                                    error = null
                                )
                            },
                            onFailure = { exception ->
                                println("QuranViewModel: Error in result: ${exception.message}")
                                exception.printStackTrace()
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Error loading Surahs: ${exception.message}"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                println("QuranViewModel: Unexpected exception: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun loadSurah(surahNumber: Int, includeTranslation: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                quranRepository.getSurah(surahNumber, includeTranslation)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load Surah: ${exception.message}"
                        )
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { surah ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    currentSurah = surah,
                                    error = null
                                )
                                
                                // Update reading progress
                                updateReadingProgress(surahNumber, 1)
                            },
                            onFailure = { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Error loading Surah: ${exception.message}"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun searchQuran(query: String, surahNumber: Int? = null) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false,
                searchQuery = ""
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                searchQuery = query,
                error = null
            )
            
            try {
                quranRepository.searchQuran(query, surahNumber)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            error = "Search failed: ${exception.message}"
                        )
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { searchResults ->
                                _uiState.value = _uiState.value.copy(
                                    isSearching = false,
                                    searchResults = searchResults,
                                    error = null
                                )
                            },
                            onFailure = { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isSearching = false,
                                    error = "Search error: ${exception.message}"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = "Unexpected search error: ${e.message}"
                )
            }
        }
    }
    
    fun addBookmark(surah: Surah, ayah: Ayah, note: String? = null) {
        viewModelScope.launch {
            try {
                val bookmark = QuranBookmark(
                    surahNumber = surah.number,
                    ayahNumber = ayah.numberInSurah,
                    surahName = surah.englishName,
                    ayahText = ayah.text,
                    translation = ayah.translation.text,
                    note = note
                )
                
                quranRepository.addBookmark(bookmark)
                loadBookmarks() // Refresh bookmarks
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add bookmark: ${e.message}"
                )
            }
        }
    }
    
    fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            try {
                quranRepository.removeBookmark(surahNumber, ayahNumber)
                loadBookmarks() // Refresh bookmarks
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove bookmark: ${e.message}"
                )
            }
        }
    }
    
    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean {
        return _uiState.value.bookmarks.any { 
            it.surahNumber == surahNumber && it.ayahNumber == ayahNumber 
        }
    }
    
    private fun loadBookmarks() {
        viewModelScope.launch {
            try {
                quranRepository.getBookmarks()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load bookmarks: ${exception.message}"
                        )
                    }
                    .collect { bookmarks ->
                        _uiState.value = _uiState.value.copy(bookmarks = bookmarks)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error loading bookmarks: ${e.message}"
                )
            }
        }
    }
    
    private fun updateReadingProgress(surahNumber: Int, lastAyahRead: Int) {
        viewModelScope.launch {
            try {
                quranRepository.updateReadingProgress(surahNumber, lastAyahRead)
                
                // Update local state
                val currentProgress = _uiState.value.readingProgress.toMutableMap()
                currentProgress[surahNumber] = lastAyahRead
                _uiState.value = _uiState.value.copy(readingProgress = currentProgress)
                
            } catch (e: Exception) {
                // Silent fail for reading progress
            }
        }
    }
    
    fun toggleTranslation() {
        _uiState.value = _uiState.value.copy(
            showTranslation = !_uiState.value.showTranslation
        )
    }
    
    fun updateFontSize(size: Float) {
        _uiState.value = _uiState.value.copy(fontSize = size)
    }
    
    fun selectAyah(ayah: Ayah) {
        _uiState.value = _uiState.value.copy(selectedAyah = ayah)
    }
    
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedAyah = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            isSearching = false,
            searchQuery = ""
        )
    }
} 