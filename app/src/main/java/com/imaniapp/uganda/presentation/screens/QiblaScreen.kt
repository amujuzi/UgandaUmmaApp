package com.imaniapp.uganda.presentation.screens

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.imaniapp.uganda.R
import com.imaniapp.uganda.presentation.components.PrimaryButton
import com.imaniapp.uganda.presentation.components.PrimaryCard
import com.imaniapp.uganda.presentation.components.SectionHeader
import com.imaniapp.uganda.presentation.theme.*
import com.imaniapp.uganda.presentation.utils.QiblaCalculator
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen() {
    val context = LocalContext.current
    
    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Mock data for demonstration (Kampala coordinates)
    val userLatitude = 0.3476
    val userLongitude = 32.5825
    
    val qiblaDirection = remember {
        QiblaCalculator.calculateQiblaDirection(userLatitude, userLongitude)
    }
    
    // Compass rotation animation
    var compassRotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = compassRotation,
        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
        label = "compass_rotation"
    )
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glass-morphic header
            QiblaHeader()
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            when {
                !locationPermissions.allPermissionsGranted -> {
                    LocationPermissionContent(
                        onRequestPermissions = { locationPermissions.launchMultiplePermissionRequest() }
                    )
                }
                else -> {
                    // 3D Compass with enhanced visuals
                    Enhanced3DCompass(
                        qiblaBearing = qiblaDirection.bearing,
                        compassRotation = animatedRotation,
                        onCompassRotationChange = { compassRotation = it }
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    
                    // Modern info cards
                    QiblaInfoCards(
                        bearing = qiblaDirection.bearing,
                        distance = qiblaDirection.distance
                    )
                }
            }
        }
    }
}

@Composable
private fun QiblaHeader() {
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
                // Animated compass icon
                val infiniteTransition = rememberInfiniteTransition(label = "compass_icon")
                val iconRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "icon_rotation"
                )
                
                Icon(
                    Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(iconRotation),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(Spacing.sm))
                
                Text(
                    text = stringResource(id = R.string.qibla_heading),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = stringResource(id = R.string.qibla_subheading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Enhanced3DCompass(
    qiblaBearing: Float,
    compassRotation: Float,
    onCompassRotationChange: (Float) -> Unit
) {
    // Simulate compass rotation for demo
    LaunchedEffect(Unit) {
        var rotation = 0f
        while (true) {
            kotlinx.coroutines.delay(100)
            rotation += 1f
            if (rotation >= 360f) rotation = 0f
            onCompassRotationChange(rotation)
        }
    }
    
    Box(
        modifier = Modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow effect
        Box(
            modifier = Modifier
                .size(340.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        radius = 170.dp.value
                    )
                )
        )
        
        // Main compass with 3D effect
        Card(
            modifier = Modifier
                .size(300.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Compass background with gradient
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    draw3DCompass(
                        compassRotation = compassRotation,
                        qiblaBearing = qiblaBearing
                    )
                }
                
                // Center jewel
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFFFB300),
                                    Color(0xFFFF8F00)
                                )
                            )
                        )
                        .shadow(4.dp, CircleShape)
                )
                
                // Qibla indicator with enhanced design
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(qiblaBearing - compassRotation)
                ) {
                    drawEnhancedQiblaIndicator()
                }
            }
        }
    }
}

private fun DrawScope.draw3DCompass(
    compassRotation: Float,
    qiblaBearing: Float
) {
    val center = size.center
    val radius = size.minDimension / 2 * 0.9f
    
    // Gradient background
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2E7D32).copy(alpha = 0.1f),
                Color(0xFF2E7D32).copy(alpha = 0.05f),
                Color.Transparent
            ),
            radius = radius
        ),
        radius = radius,
        center = center
    )
    
    // Outer ring with 3D effect
    drawCircle(
        color = Color(0xFF2E7D32),
        radius = radius,
        center = center,
        style = Stroke(width = 6.dp.toPx())
    )
    
    // Inner decorative rings
    drawCircle(
        color = Color(0xFF2E7D32).copy(alpha = 0.3f),
        radius = radius * 0.85f,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    
    drawCircle(
        color = Color(0xFF2E7D32).copy(alpha = 0.2f),
        radius = radius * 0.7f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    
    // Enhanced compass markings
    rotate(degrees = -compassRotation, pivot = center) {
        for (i in 0 until 360 step 10) {
            val angle = Math.toRadians(i.toDouble())
            val isMainDirection = i % 90 == 0
            val isSubDirection = i % 30 == 0
            
            val startRadius = when {
                isMainDirection -> radius * 0.75f
                isSubDirection -> radius * 0.8f
                else -> radius * 0.85f
            }
            val endRadius = radius * 0.95f
            
            val startX = center.x + cos(angle).toFloat() * startRadius
            val startY = center.y + sin(angle).toFloat() * startRadius
            val endX = center.x + cos(angle).toFloat() * endRadius
            val endY = center.y + sin(angle).toFloat() * endRadius
            
            val strokeWidth = when {
                isMainDirection -> 4.dp.toPx()
                isSubDirection -> 2.dp.toPx()
                else -> 1.dp.toPx()
            }
            
            val color = when {
                isMainDirection -> Color(0xFF2E7D32)
                isSubDirection -> Color(0xFF2E7D32).copy(alpha = 0.7f)
                else -> Color(0xFF2E7D32).copy(alpha = 0.4f)
            }
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        
        // Cardinal direction indicators
        val directions = listOf(
            0 to "N", 90 to "E", 180 to "S", 270 to "W"
        )
        
        directions.forEach { (angle, _) ->
            val radians = Math.toRadians(angle.toDouble())
            val indicatorRadius = radius * 0.65f
            val x = center.x + cos(radians).toFloat() * indicatorRadius
            val y = center.y + sin(radians).toFloat() * indicatorRadius
            
            // Draw direction indicator circles
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = 8.dp.toPx(),
                center = Offset(x, y)
            )
            
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private fun DrawScope.drawEnhancedQiblaIndicator() {
    val center = size.center
    val radius = size.minDimension / 2 * 0.5f
    
    // Qibla arrow with gradient and shadow effect
    val arrowPath = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x - 20.dp.toPx(), center.y - radius + 40.dp.toPx())
        lineTo(center.x - 8.dp.toPx(), center.y - radius + 30.dp.toPx())
        lineTo(center.x, center.y - radius + 35.dp.toPx())
        lineTo(center.x + 8.dp.toPx(), center.y - radius + 30.dp.toPx())
        lineTo(center.x + 20.dp.toPx(), center.y - radius + 40.dp.toPx())
        close()
    }
    
    // Shadow
    val shadowPath = Path().apply {
        addPath(arrowPath, Offset(2.dp.toPx(), 2.dp.toPx()))
    }
    
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.3f),
        style = Fill
    )
    
    // Main arrow with gradient
    drawPath(
        path = arrowPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFB300),
                Color(0xFFFF8F00)
            ),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y - radius + 40.dp.toPx())
        ),
        style = Fill
    )
    
    // Arrow outline
    drawPath(
        path = arrowPath,
        color = Color(0xFFFF8F00),
        style = Stroke(width = 2.dp.toPx())
    )
    
    // Qibla text indicator
    val textY = center.y - radius + 60.dp.toPx()
    drawCircle(
        color = Color(0xFF2E7D32),
        radius = 25.dp.toPx(),
        center = Offset(center.x, textY)
    )
    
    drawCircle(
        color = Color.White,
        radius = 22.dp.toPx(),
        center = Offset(center.x, textY)
    )
}

@Composable
private fun QiblaInfoCards(
    bearing: Float,
    distance: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SectionHeader(text = "Qibla Information")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Direction card
            PrimaryCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Icon(
                        Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    
                    Text(
                        text = stringResource(id = R.string.qibla_info_direction_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${bearing.toInt()}°",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = QiblaCalculator.getCompassDirection(bearing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Distance card
            PrimaryCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Icon(
                        Icons.Default.Straighten,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    
                    Text(
                        text = stringResource(id = R.string.qibla_info_distance_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = QiblaCalculator.formatDistance(distance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = stringResource(id = R.string.qibla_info_distance_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Calibration tip card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius.md)
        ) {
            Row(
                modifier = Modifier.padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(id = R.string.qibla_info_calibration_tip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun LocationPermissionContent(
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(id = R.string.qibla_permission_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = stringResource(id = R.string.qibla_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        PrimaryButton(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.qibla_permission_button))
        }
    }
} 