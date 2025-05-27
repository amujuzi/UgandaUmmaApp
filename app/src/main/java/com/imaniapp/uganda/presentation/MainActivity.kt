package com.imaniapp.uganda.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.imaniapp.uganda.R
import com.imaniapp.uganda.presentation.theme.ImaniAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.annotation.StringRes
import com.imaniapp.uganda.presentation.utils.rememberDevicePosture
import com.imaniapp.uganda.presentation.utils.DevicePosture

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ImaniAppTheme {
                ImaniApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImaniApp() {
    val navController = rememberNavController()
    val devicePosture = rememberDevicePosture()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) }
            )
        },
        bottomBar = if (devicePosture == DevicePosture.COMPACT) {
            {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(id = item.titleRes)) },
                            label = { Text(stringResource(id = item.titleRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        } else null,
        content = { innerPadding ->
            Row(modifier = Modifier.padding(innerPadding)) {
                if (devicePosture != DevicePosture.COMPACT) {
                    NavigationRail {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        bottomNavItems.forEach { item ->
                            NavigationRailItem(
                                icon = { Icon(item.icon, contentDescription = stringResource(id = item.titleRes)) },
                                label = { Text(stringResource(id = item.titleRes)) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "prayer_times",
                    modifier = Modifier.weight(1f)
                ) {
                    composable("prayer_times") {
                        com.imaniapp.uganda.presentation.screens.PrayerTimesScreen()
                    }
                    composable("qibla") {
                        com.imaniapp.uganda.presentation.screens.QiblaScreen()
                    }
                    composable("quran") {
                        com.imaniapp.uganda.presentation.screens.QuranScreen(
                            onNavigateToSurah = { surahNumber ->
                                navController.navigate("surah/$surahNumber")
                            }
                        )
                    }
                    composable("dua") {
                        com.imaniapp.uganda.presentation.screens.DuaScreen()
                    }
                    composable("mosques") {
                        com.imaniapp.uganda.presentation.screens.MosqueFinderScreen()
                    }
                    composable("surah/{number}") { backStackEntry ->
                        val surahNumber = backStackEntry.arguments?.getString("number")?.toIntOrNull() ?: 1
                        com.imaniapp.uganda.presentation.screens.SurahReadingScreen(
                            surahNumber = surahNumber,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    ) { /* removed old content as now inside scaffold content parameter */ }
}

// Navigation items
data class BottomNavItem(
    @StringRes val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_prayer_times, Icons.Default.Schedule, "prayer_times"),
    BottomNavItem(R.string.nav_qibla, Icons.Default.Explore, "qibla"),
    BottomNavItem(R.string.nav_quran, Icons.AutoMirrored.Filled.MenuBook, "quran"),
    BottomNavItem(R.string.nav_dua, Icons.Default.Favorite, "dua"),
    BottomNavItem(R.string.nav_mosques, Icons.Default.Place, "mosques")
)

// Keep only the main app preview for designers
@Preview(showBackground = true)
@Composable
fun ImaniAppPreview() {
    ImaniAppTheme {
        ImaniApp()
    }
} 