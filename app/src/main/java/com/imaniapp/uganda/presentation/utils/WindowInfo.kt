package com.imaniapp.uganda.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

enum class DevicePosture { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberDevicePosture(): DevicePosture {
    val context = LocalContext.current
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
    val widthPx = metrics.bounds.width()
    val density = LocalDensity.current
    val widthDp: Dp = with(density) { widthPx.toDp() }

    return when {
        widthDp < 600.dp -> DevicePosture.COMPACT
        widthDp < 840.dp -> DevicePosture.MEDIUM
        else -> DevicePosture.EXPANDED
    }
} 