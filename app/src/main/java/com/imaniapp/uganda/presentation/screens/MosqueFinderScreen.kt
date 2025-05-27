package com.imaniapp.uganda.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.imaniapp.uganda.R
import com.imaniapp.uganda.domain.model.*
import com.imaniapp.uganda.domain.repository.MosqueStats
import com.imaniapp.uganda.presentation.theme.*
import com.imaniapp.uganda.presentation.viewmodel.MosqueViewModel
import com.imaniapp.uganda.presentation.viewmodel.ViewMode
import com.imaniapp.uganda.presentation.components.*
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MosqueFinderScreen(
    viewModel: MosqueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchRadius by viewModel.searchRadius.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.loadNearbyMosques()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Header with Islamic greeting and stats
        MosqueFinderHeader(
            stats = uiState.mosqueStats,
            currentLocation = uiState.currentLocation,
            onRefresh = { viewModel.refreshMosques() }
        )
        
        // Search and Filter Bar
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.searchMosques(it) },
            selectedFilter = selectedFilter,
            searchRadius = searchRadius,
            onShowFilter = { showFilterDialog = true },
            onShowRadius = { showRadiusDialog = true },
            onToggleView = { viewModel.toggleViewMode() },
            viewMode = uiState.viewMode
        )
        
        // Permission handling
        if (!locationPermissionState.status.isGranted) {
            LocationPermissionContent(
                onRequestPermission = { locationPermissionState.launchPermissionRequest() }
            )
        } else {
            // Main content
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadNearbyMosques() }
                    )
                }
                else -> {
                    when (uiState.viewMode) {
                        ViewMode.MAP -> {
                            MosqueMapView(
                                mosques = uiState.filteredMosques,
                                selectedMosque = uiState.selectedMosque,
                                currentLocation = uiState.currentLocation,
                                onMosqueSelected = { viewModel.selectMosque(it) },
                                onAddMosque = { viewModel.showAddMosqueDialog() }
                            )
                        }
                        ViewMode.LIST -> {
                            MosqueListView(
                                mosques = uiState.filteredMosques,
                                onMosqueClick = { viewModel.selectMosque(it) },
                                onGetDirections = { viewModel.getDirectionsToMosque(it) },
                                onAddMosque = { viewModel.showAddMosqueDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = selectedFilter,
            onFilterApplied = { filter ->
                viewModel.applyFilter(filter)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
    
    if (showRadiusDialog) {
        RadiusDialog(
            currentRadius = searchRadius,
            onRadiusSelected = { radius ->
                viewModel.updateSearchRadius(radius)
                showRadiusDialog = false
            },
            onDismiss = { showRadiusDialog = false }
        )
    }
    
    if (uiState.showAddMosqueDialog) {
        AddMosqueDialog(
            currentLocation = uiState.currentLocation,
            onMosqueAdded = { mosque ->
                viewModel.addMosque(mosque)
            },
            onDismiss = { viewModel.hideAddMosqueDialog() }
        )
    }
    
    if (uiState.showReportDialog && uiState.selectedMosque != null) {
        ReportMosqueDialog(
            mosque = uiState.selectedMosque!!,
            onReportSubmitted = { issue, email ->
                viewModel.reportMosque(uiState.selectedMosque!!.id, issue, email)
            },
            onDismiss = { viewModel.hideReportDialog() }
        )
    }
    
    // Selected mosque bottom sheet
    if (uiState.selectedMosque != null) {
        MosqueDetailsBottomSheet(
            mosque = uiState.selectedMosque!!,
            onDismiss = { viewModel.clearSelectedMosque() },
            onGetDirections = { viewModel.getDirectionsToMosque(it) },
            onReport = { viewModel.showReportDialog() }
        )
    }
    
    // Snackbar for messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun MosqueFinderHeader(
    stats: MosqueStats?,
    currentLocation: Location?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.assalamu_alaikum),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.mosque_finder),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            if (stats != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip(
                        label = "${stats.totalMosques}",
                        description = "Mosques",
                        icon = Icons.Default.Place
                    )
                    StatChip(
                        label = "${stats.mosquesWithJummah}",
                        description = "Jummah",
                        icon = Icons.Default.Event
                    )
                    StatChip(
                        label = "${stats.mosquesWithWomenFacilities}",
                        description = "Women",
                        icon = Icons.Default.Female
                    )
                }
            }
            
            if (currentLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Location: ${currentLocation.latitude.toString().take(7)}, ${currentLocation.longitude.toString().take(7)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: MosqueFilter,
    searchRadius: Double,
    onShowFilter: () -> Unit,
    onShowRadius: () -> Unit,
    onToggleView: () -> Unit,
    viewMode: ViewMode
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search mosques...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filter and control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filter button
            FilterChip(
                onClick = onShowFilter,
                label = {
                    Text(
                        text = if (selectedFilter.hasAnyFilter()) "Filtered" else "Filter",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = selectedFilter.hasAnyFilter(),
                leadingIcon = {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            // Radius button
            FilterChip(
                onClick = onShowRadius,
                label = {
                    Text(
                        text = "${searchRadius.roundToInt()}km",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = false,
                leadingIcon = {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // View toggle button
            IconButton(
                onClick = onToggleView,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    if (viewMode == ViewMode.MAP) Icons.Default.List else Icons.Default.Map,
                    contentDescription = "Toggle View",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun MosqueMapView(
    mosques: List<Mosque>,
    selectedMosque: Mosque?,
    currentLocation: Location?,
    onMosqueSelected: (Mosque) -> Unit,
    onAddMosque: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Placeholder for Google Maps
        // In a real implementation, this would be GoogleMap composable
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Interactive Map View",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Google Maps integration would show ${mosques.size} nearby mosques",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (currentLocation != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your location: ${currentLocation.latitude.toString().take(7)}, ${currentLocation.longitude.toString().take(7)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Add mosque FAB
        FloatingActionButton(
            onClick = onAddMosque,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Mosque",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun MosqueListView(
    mosques: List<Mosque>,
    onMosqueClick: (Mosque) -> Unit,
    onGetDirections: (Mosque) -> Unit,
    onAddMosque: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (mosques.isEmpty()) {
            EmptyMosqueList(onAddMosque = onAddMosque)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mosques) { mosque ->
                    MosqueCard(
                        mosque = mosque,
                        onClick = { onMosqueClick(mosque) },
                        onGetDirections = { onGetDirections(mosque) }
                    )
                }
                
                // Add mosque item at the end
                item {
                    AddMosqueCard(onClick = onAddMosque)
                }
            }
        }
    }
}

@Composable
private fun MosqueCard(
    mosque: Mosque,
    onClick: () -> Unit,
    onGetDirections: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = mosque.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mosque.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (mosque.distance != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", mosque.distance)} km away",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                IconButton(onClick = onGetDirections) {
                    Icon(
                        Icons.Default.Directions,
                        contentDescription = "Get Directions",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Facility indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (mosque.hasJummah) {
                    FacilityChip(
                        text = "Jummah",
                        icon = Icons.Default.Event,
                        isAvailable = true
                    )
                }
                if (mosque.hasWomenPrayer) {
                    FacilityChip(
                        text = "Women",
                        icon = Icons.Default.Female,
                        isAvailable = true
                    )
                }
                if (mosque.hasWudu) {
                    FacilityChip(
                        text = "Wudu",
                        icon = Icons.Default.WaterDrop,
                        isAvailable = true
                    )
                }
            }
        }
    }
}

@Composable
private fun FacilityChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAvailable: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAvailable) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isAvailable) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (isAvailable) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun AddMosqueCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add a Mosque",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyMosqueList(onAddMosque: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Mosques Found",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try adjusting your search radius or filters, or help the community by adding a mosque.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddMosque) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Mosque")
        }
    }
}

// Additional composables for dialogs would be implemented here
// FilterDialog, RadiusDialog, AddMosqueDialog, ReportMosqueDialog, MosqueDetailsBottomSheet

@Composable
private fun LocationPermissionContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_location_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.permission_location_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Location Permission")
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Finding nearby mosques...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// Extension function for filter checking
private fun MosqueFilter.hasAnyFilter(): Boolean {
    return hasJummah || hasWomenPrayer || hasWudu || maxDistance != null
} 