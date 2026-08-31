package com.pawedcat.app.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.ui.theme.PawedCatTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        try {
            WorkManager.initialize(context, Configuration.Builder().build())
        } catch (_: Exception) {
            // Already initialized
        }
    }

    @Test
    fun mainNavigation_displaysAllTabsAndRespondsToTaps() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val serviceLocator = ServiceLocator.getInstance(context)

        composeTestRule.setContent {
            PawedCatTheme(darkTheme = true) {
                MainScreen(serviceLocator = serviceLocator)
            }
        }

        composeTestRule.waitForIdle()

        // 1. Initial tab: Podcasts
        composeTestRule.onAllNodesWithText("Podcasts").assertCountEquals(2) // TopBar title + BottomNav label
        composeTestRule.onNodeWithText("Queue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Downloads").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

        // 2. Switch to Queue tab
        composeTestRule.onNodeWithText("Queue").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Play Queue").assertIsDisplayed()

        // 3. Switch to Downloads tab
        composeTestRule.onNodeWithText("Downloads").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Downloads").assertCountEquals(2) // TopBar title + BottomNav label

        // 4. Switch to Settings tab
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Settings").assertCountEquals(2) // TopBar title + BottomNav label
    }
}
