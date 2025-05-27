package com.imaniapp.uganda.presentation.screens

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.imaniapp.uganda.R
import com.imaniapp.uganda.domain.model.Prayer
import com.imaniapp.uganda.domain.model.PrayerTime
import com.imaniapp.uganda.domain.model.PrayerTimeStatus
import com.imaniapp.uganda.presentation.components.PrimaryButton
import com.imaniapp.uganda.presentation.components.PrimaryCard
import com.imaniapp.uganda.presentation.components.SectionHeader
import com.imaniapp.uganda.presentation.theme.*
import com.imaniapp.uganda.presentation.viewmodel.PrayerTimesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerTimesScreen(
    viewModel: PrayerTimesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.onLocationPermissionGranted()
            viewModel.startLocationUpdates()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    radius = 1200f
                )
            )
    ) {
        when {
            !locationPermissions.allPermissionsGranted -> {
                LocationPermissionContent(
                    onRequestPermissions = { locationPermissions.launchMultiplePermissionRequest() }
                )
            }
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.refreshPrayerTimes() }
                )
            }
            uiState.prayerTime != null -> {
                PrayerTimesContent(
                    prayerTime = uiState.prayerTime!!,
                    prayerStatus = uiState.prayerStatus,
                    currentLocation = uiState.currentLocation,
                    onRefresh = { viewModel.refreshPrayerTimes() }
                )
            }
        }
    }
}

@Composable
private fun PrayerTimesContent(
    prayerTime: PrayerTime,
    prayerStatus: PrayerTimeStatus?,
    currentLocation: String,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Glass-morphic header
        item {
            GlassMorphicHeader(
                currentLocation = currentLocation,
                prayerStatus = prayerStatus
            )
        }
        
        // Today's prayer times section
        item {
            SectionHeader(text = "Today's Prayer Times")
            Spacer(modifier = Modifier.height(Spacing.sm))
        }
        
        // Prayer times grid
        val prayers = listOf(
            Prayer.FAJR to prayerTime.fajr,
            Prayer.SUNRISE to prayerTime.sunrise,
            Prayer.DHUHR to prayerTime.dhuhr,
            Prayer.ASR to prayerTime.asr,
            Prayer.MAGHRIB to prayerTime.maghrib,
            Prayer.ISHA to prayerTime.isha
        )
        
        items(prayers) { (prayer, time) ->
            ModernPrayerTimeCard(
                prayer = prayer,
                time = time,
                isCurrentPrayer = prayerStatus?.currentPrayer == prayer,
                isNextPrayer = prayerStatus?.nextPrayer == prayer
            )
        }
        
        // Refresh section
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
            PrimaryButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("Refresh Prayer Times")
            }
        }
    }
}

@Composable
private fun GlassMorphicHeader(
    currentLocation: String,
    prayerStatus: PrayerTimeStatus?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.lg)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(Spacing.lg)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Islamic greeting
                Text(
                    text = stringResource(R.string.bismillah),
                    style = ArabicLargeTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(Spacing.sm))
                
                Text(
                    text = stringResource(R.string.assalamu_alaikum),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(Spacing.xs))
                
                Text(
                    text = getCurrentDateString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(Spacing.xs))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = currentLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Next prayer countdown with progress ring
                if (prayerStatus != null) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    NextPrayerCountdown(prayerStatus)
                }
            }
        }
    }
}

@Composable
private fun NextPrayerCountdown(prayerStatus: PrayerTimeStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Next Prayer",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Spacing.sm))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            // Animated progress ring
            CircularProgressRing(
                progress = 0.7f, // Mock progress - you can calculate actual progress
                modifier = Modifier.fillMaxSize()
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = prayerStatus.nextPrayer.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = prayerStatus.nextPrayer.arabicName,
                    style = ArabicTextStyle.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = prayerStatus.timeUntilNext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic),
        label = "progress_animation"
    )
    
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = size.center
        
        // Background ring
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        
        // Progress ring
        drawArc(
            color = Color(0xFF2E7D32), // Islamic green
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            ),
            topLeft = center - androidx.compose.ui.geometry.Offset(radius, radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

@Composable
private fun ModernPrayerTimeCard(
    prayer: Prayer,
    time: String,
    isCurrentPrayer: Boolean,
    isNextPrayer: Boolean
) {
    val cardColors = when {
        isCurrentPrayer -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        isNextPrayer -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.md)),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentPrayer || isNextPrayer) Spacing.sm else Spacing.xs
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prayer icon indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isCurrentPrayer || isNextPrayer) 
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (prayer) {
                            Prayer.FAJR -> Icons.Default.WbTwilight
                            Prayer.SUNRISE -> Icons.Default.WbSunny
                            Prayer.DHUHR -> Icons.Default.LightMode
                            Prayer.ASR -> Icons.Default.WbCloudy
                            Prayer.MAGHRIB -> Icons.Default.WbTwilight
                            Prayer.ISHA -> Icons.Default.NightsStay
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isCurrentPrayer) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(Spacing.md))
                
                Column {
                    Text(
                        text = prayer.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentPrayer || isNextPrayer) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = prayer.arabicName,
                        style = ArabicTextStyle.copy(fontSize = 12.sp),
                        color = LocalContentColor.current.copy(alpha = 0.7f)
                    )
                }
            }
            
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LocationPermissionContent(
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.permission_location_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = stringResource(R.string.permission_location_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        PrimaryButton(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Location Permission")
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.loading),
            style = MaterialTheme.typography.bodyLarge
        )
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
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.error),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        PrimaryButton(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

private fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date())
} 