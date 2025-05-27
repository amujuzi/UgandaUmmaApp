package com.imaniapp.uganda.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.DuaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuaUiState(
    val isLoading: Boolean = false,
    val duas: List<Dua> = emptyList(),
    val favoriteDuas: List<Dua> = emptyList(),
    val aiGeneratedDuas: List<Dua> = emptyList(),
    val hisnulMuslimDuas: List<Dua> = emptyList(),
    val searchResults: List<Dua> = emptyList(),
    val selectedCategory: DuaCategory? = null,
    val categories: List<DuaCategory> = DuaCategory.values().toList(),
    val error: String? = null,
    val isGeneratingAi: Boolean = false,
    val aiResponse: AiDuaResponse? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val showAiDisclaimer: Boolean = true
)

@HiltViewModel
class DuaViewModel @Inject constructor(
    private val duaRepository: DuaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DuaUiState())
    val uiState: StateFlow<DuaUiState> = _uiState.asStateFlow()
    
    init {
        initializeData()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            // Initialize Hisnul Muslim du'as if not already done
            duaRepository.initializeHisnulMuslimDuas()
            
            // Load all data
            loadAllDuas()
            loadFavoriteDuas()
            loadHisnulMuslimDuas()
            loadAiGeneratedDuas()
        }
    }
    
    private fun loadAllDuas() {
        viewModelScope.launch {
            duaRepository.getAllDuas()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load du'as: ${exception.message}"
                    )
                }
                .collect { duas ->
                    _uiState.value = _uiState.value.copy(duas = duas)
                }
        }
    }
    
    private fun loadFavoriteDuas() {
        viewModelScope.launch {
            duaRepository.getFavoriteDuas()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load favorite du'as: ${exception.message}"
                    )
                }
                .collect { favoriteDuas ->
                    _uiState.value = _uiState.value.copy(favoriteDuas = favoriteDuas)
                }
        }
    }
    
    private fun loadHisnulMuslimDuas() {
        viewModelScope.launch {
            duaRepository.getHisnulMuslimDuas()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load Hisnul Muslim du'as: ${exception.message}"
                    )
                }
                .collect { hisnulMuslimDuas ->
                    _uiState.value = _uiState.value.copy(hisnulMuslimDuas = hisnulMuslimDuas)
                }
        }
    }
    
    private fun loadAiGeneratedDuas() {
        viewModelScope.launch {
            duaRepository.getAiGeneratedDuas()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load AI generated du'as: ${exception.message}"
                    )
                }
                .collect { aiDuas ->
                    _uiState.value = _uiState.value.copy(aiGeneratedDuas = aiDuas)
                }
        }
    }
    
    fun loadDuasByCategory(category: DuaCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedCategory = category,
                error = null
            )
            
            try {
                duaRepository.getDuasByCategory(category)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load category du'as: ${exception.message}"
                        )
                    }
                    .collect { duas ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            duas = duas
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading category: ${e.message}"
                )
            }
        }
    }
    
    fun searchDuas(query: String) {
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
                duaRepository.searchDuas(query)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            error = "Search failed: ${exception.message}"
                        )
                    }
                    .collect { searchResults ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            searchResults = searchResults
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = "Search error: ${e.message}"
                )
            }
        }
    }
    
    fun generateAiDua(request: DuaRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingAi = true,
                error = null,
                aiResponse = null
            )
            
            try {
                val result = duaRepository.generateAiDua(request)
                result.fold(
                    onSuccess = { aiResponse ->
                        _uiState.value = _uiState.value.copy(
                            isGeneratingAi = false,
                            aiResponse = aiResponse
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isGeneratingAi = false,
                            error = "AI generation failed: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingAi = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun saveAiDua(aiResponse: AiDuaResponse, request: DuaRequest) {
        viewModelScope.launch {
            try {
                duaRepository.saveAiDua(aiResponse, request)
                
                // Refresh AI generated du'as
                loadAiGeneratedDuas()
                loadAllDuas()
                
                // Clear the AI response
                _uiState.value = _uiState.value.copy(aiResponse = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save AI du'a: ${e.message}"
                )
            }
        }
    }
    
    fun toggleFavorite(duaId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    duaRepository.addToFavorites(duaId)
                } else {
                    duaRepository.removeFromFavorites(duaId)
                }
                
                // Refresh data
                loadFavoriteDuas()
                loadAllDuas()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update favorite: ${e.message}"
                )
            }
        }
    }
    
    fun deleteDua(duaId: Long) {
        viewModelScope.launch {
            try {
                duaRepository.deleteDua(duaId)
                
                // Refresh data
                loadAllDuas()
                loadAiGeneratedDuas()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete du'a: ${e.message}"
                )
            }
        }
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            isSearching = false,
            searchQuery = ""
        )
    }
    
    fun clearAiResponse() {
        _uiState.value = _uiState.value.copy(aiResponse = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun dismissAiDisclaimer() {
        _uiState.value = _uiState.value.copy(showAiDisclaimer = false)
    }
    
    fun clearSelectedCategory() {
        _uiState.value = _uiState.value.copy(selectedCategory = null)
        loadAllDuas()
    }
} 