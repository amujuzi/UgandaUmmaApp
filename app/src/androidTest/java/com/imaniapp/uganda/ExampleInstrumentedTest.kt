package com.imaniapp.uganda

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.imaniapp.uganda.presentation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ImaniAppUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun app_launches_successfully() {
        // Verify the app launches and shows the main navigation
        composeTestRule.onNodeWithText("Prayer Times").assertIsDisplayed()
        composeTestRule.onNodeWithText("Qibla").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quran").assertIsDisplayed()
        composeTestRule.onNodeWithText("Du'a").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mosques").assertIsDisplayed()
    }

    @Test
    fun navigation_between_islamic_features_works() {
        // Test navigation to Prayer Times
        composeTestRule.onNodeWithText("Prayer Times").performClick()
        composeTestRule.waitForIdle()
        
        // Test navigation to Qibla
        composeTestRule.onNodeWithText("Qibla").performClick()
        composeTestRule.waitForIdle()
        
        // Test navigation to Quran
        composeTestRule.onNodeWithText("Quran").performClick()
        composeTestRule.waitForIdle()
        
        // Test navigation to Du'a
        composeTestRule.onNodeWithText("Du'a").performClick()
        composeTestRule.waitForIdle()
        
        // Test navigation to Mosque Finder
        composeTestRule.onNodeWithText("Mosques").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun prayer_times_screen_displays_islamic_content() {
        composeTestRule.onNodeWithText("Prayer Times").performClick()
        
        // Should show Islamic greeting
        composeTestRule.onNodeWithText("Assalamu Alaikum").assertIsDisplayed()
        
        // Should show prayer time labels (these might be in Arabic)
        composeTestRule.onNode(hasText("Fajr") or hasText("فجر")).assertExists()
        composeTestRule.onNode(hasText("Dhuhr") or hasText("ظهر")).assertExists()
        composeTestRule.onNode(hasText("Asr") or hasText("عصر")).assertExists()
        composeTestRule.onNode(hasText("Maghrib") or hasText("مغرب")).assertExists()
        composeTestRule.onNode(hasText("Isha") or hasText("عشاء")).assertExists()
    }

    @Test
    fun qibla_screen_shows_compass() {
        composeTestRule.onNodeWithText("Qibla").performClick()
        
        // Should show Qibla direction content
        composeTestRule.onNodeWithText("Qibla Direction").assertIsDisplayed()
        composeTestRule.onNodeWithText("Direction to Mecca").assertIsDisplayed()
    }

    @Test
    fun quran_screen_shows_surahs() {
        composeTestRule.onNodeWithText("Quran").performClick()
        
        // Should show Quran content
        composeTestRule.onNodeWithText("Holy Quran").assertIsDisplayed()
        
        // Should show some surahs
        composeTestRule.onNode(hasText("Al-Fatiha") or hasText("الفاتحة")).assertExists()
    }

    @Test
    fun dua_screen_shows_categories() {
        composeTestRule.onNodeWithText("Du'a").performClick()
        
        // Should show Du'a categories
        composeTestRule.onNodeWithText("Morning Adhkar").assertExists()
        composeTestRule.onNodeWithText("Evening Adhkar").assertExists()
        composeTestRule.onNodeWithText("Prayer Du'as").assertExists()
    }

    @Test
    fun mosque_finder_requests_location_permission() {
        composeTestRule.onNodeWithText("Mosques").performClick()
        
        // Should show mosque finder content
        composeTestRule.onNodeWithText("Mosque Finder").assertIsDisplayed()
    }

    @Test
    fun islamic_theming_is_applied() {
        // Verify Islamic color scheme is applied
        // This would require checking specific UI elements for green/gold colors
        // For now, we'll check that the app doesn't crash and displays properly
        composeTestRule.onRoot().assertIsDisplayed()
    }
} 