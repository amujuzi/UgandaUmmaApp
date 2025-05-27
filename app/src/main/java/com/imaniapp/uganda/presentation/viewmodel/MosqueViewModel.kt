package com.imaniapp.uganda.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.MosqueRepository
import com.imaniapp.uganda.domain.repository.MosqueStats
import com.imaniapp.uganda.presentation.utils.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MosqueViewModel @Inject constructor(
    private val mosqueRepository: MosqueRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MosqueUiState())
    val uiState: StateFlow<MosqueUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow(MosqueFilter())
    val selectedFilter: StateFlow<MosqueFilter> = _selectedFilter.asStateFlow()
    
    private val _searchRadius = MutableStateFlow(10.0) // Default 10km
    val searchRadius: StateFlow<Double> = _searchRadius.asStateFlow()
    
    private val _mapViewType = MutableStateFlow(MapViewType.NORMAL)
    val mapViewType: StateFlow<MapViewType> = _mapViewType.asStateFlow()
    
    init {
        loadNearbyMosques()
        observeLocationUpdates()
    }
    
    fun loadNearbyMosques() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val androidLocation = locationHelper.getCurrentLocation()
                if (androidLocation != null) {
                    val location = Location(
                        latitude = androidLocation.latitude,
                        longitude = androidLocation.longitude
                    )
                    _uiState.value = _uiState.value.copy(currentLocation = location)
                    
                    mosqueRepository.getNearbyMosques(
                        location = location,
                        radiusKm = _searchRadius.value,
                        filter = _selectedFilter.value.takeIf { it.hasAnyFilter() }
                    ).collect { result ->
                        result.fold(
                            onSuccess = { mosques ->
                                _uiState.value = _uiState.value.copy(
                                    mosques = mosques,
                                    filteredMosques = mosques,
                                    isLoading = false,
                                    error = null
                                )
                                loadMosqueStats(location)
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load mosques"
                                )
                            }
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location permission required"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun searchMosques(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            // Reset to all mosques
            _uiState.value = _uiState.value.copy(
                filteredMosques = _uiState.value.mosques,
                isSearching = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            
            try {
                mosqueRepository.searchMosques(
                    query = query,
                    location = _uiState.value.currentLocation
                ).collect { result ->
                    result.fold(
                        onSuccess = { searchResults ->
                            _uiState.value = _uiState.value.copy(
                                filteredMosques = searchResults,
                                isSearching = false
                            )
                        },
                        onFailure = { error ->
                            // Fallback to local filtering
                            val localResults = _uiState.value.mosques.filter { mosque ->
                                mosque.name.contains(query, ignoreCase = true) ||
                                mosque.address.contains(query, ignoreCase = true)
                            }
                            _uiState.value = _uiState.value.copy(
                                filteredMosques = localResults,
                                isSearching = false
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }
    
    fun applyFilter(filter: MosqueFilter) {
        _selectedFilter.value = filter
        
        val filteredMosques = if (filter.hasAnyFilter()) {
            _uiState.value.mosques.filter { mosque ->
                (!filter.hasJummah || mosque.hasJummah) &&
                (!filter.hasWomenPrayer || mosque.hasWomenPrayer) &&
                (!filter.hasWudu || mosque.hasWudu) &&
                (filter.maxDistance == null || (mosque.distance ?: 0.0) <= filter.maxDistance)
            }
        } else {
            _uiState.value.mosques
        }
        
        _uiState.value = _uiState.value.copy(
            filteredMosques = filteredMosques,
            selectedFilter = filter
        )
    }
    
    fun updateSearchRadius(radiusKm: Double) {
        _searchRadius.value = radiusKm
        loadNearbyMosques() // Reload with new radius
    }
    
    fun selectMosque(mosque: Mosque) {
        _uiState.value = _uiState.value.copy(selectedMosque = mosque)
    }
    
    fun clearSelectedMosque() {
        _uiState.value = _uiState.value.copy(selectedMosque = null)
    }
    
    fun toggleMapViewType() {
        _mapViewType.value = when (_mapViewType.value) {
            MapViewType.NORMAL -> MapViewType.SATELLITE
            MapViewType.SATELLITE -> MapViewType.TERRAIN
            MapViewType.TERRAIN -> MapViewType.NORMAL
        }
    }
    
    fun addMosque(mosque: Mosque) {
        viewModelScope.launch {
            try {
                mosqueRepository.addMosque(mosque).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            showAddMosqueDialog = false,
                            message = "Mosque added successfully!"
                        )
                        loadNearbyMosques() // Refresh the list
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to add mosque: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add mosque: ${e.message}"
                )
            }
        }
    }
    
    fun reportMosque(mosqueId: String, issue: String, userEmail: String? = null) {
        viewModelScope.launch {
            try {
                mosqueRepository.reportMosque(mosqueId, issue, userEmail).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            showReportDialog = false,
                            message = "Report submitted successfully. Thank you!"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to submit report: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to submit report: ${e.message}"
                )
            }
        }
    }
    
    fun showAddMosqueDialog() {
        _uiState.value = _uiState.value.copy(showAddMosqueDialog = true)
    }
    
    fun hideAddMosqueDialog() {
        _uiState.value = _uiState.value.copy(showAddMosqueDialog = false)
    }
    
    fun showReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = true)
    }
    
    fun hideReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(
            viewMode = when (_uiState.value.viewMode) {
                ViewMode.MAP -> ViewMode.LIST
                ViewMode.LIST -> ViewMode.MAP
            }
        )
    }
    
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationHelper.getLocationUpdates().collect { androidLocation ->
                val location = Location(
                    latitude = androidLocation.latitude,
                    longitude = androidLocation.longitude
                )
                _uiState.value = _uiState.value.copy(currentLocation = location)
            }
        }
    }
    
    private suspend fun loadMosqueStats(location: Location) {
        try {
            val stats = mosqueRepository.getMosqueStats(location)
            _uiState.value = _uiState.value.copy(mosqueStats = stats)
        } catch (e: Exception) {
            // Stats are not critical, continue without them
        }
    }
    
    fun refreshMosques() {
        loadNearbyMosques()
    }
    
    fun getDirectionsToMosque(mosque: Mosque) {
        // This would typically open the default maps app
        // Implementation would depend on the specific requirements
        _uiState.value = _uiState.value.copy(
            message = "Opening directions to ${mosque.name}..."
        )
    }
}

data class MosqueUiState(
    val mosques: List<Mosque> = emptyList(),
    val filteredMosques: List<Mosque> = emptyList(),
    val selectedMosque: Mosque? = null,
    val currentLocation: Location? = null,
    val selectedFilter: MosqueFilter = MosqueFilter(),
    val mosqueStats: MosqueStats? = null,
    val viewMode: ViewMode = ViewMode.MAP,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val showAddMosqueDialog: Boolean = false,
    val showReportDialog: Boolean = false
)

enum class ViewMode {
    MAP, LIST
}

enum class MapViewType {
    NORMAL, SATELLITE, TERRAIN
}

// Extension function to check if filter has any active filters
private fun MosqueFilter.hasAnyFilter(): Boolean {
    return hasJummah || hasWomenPrayer || hasWudu || maxDistance != null
} 