package app.alfatiha.tafsir.azkar.ui.reading

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarTheme
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarThemeMode
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarThemeValues
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AzkarGoldenReadingScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun capture360Light() {
        renderAndCapture(
            themeMode = AzkarThemeMode.Light,
            state = AzkarGoldenReadingFixtures.GoldenMorning,
            fileName = "azkar_360x800_light.png",
            expectedWidth = 360,
            expectedHeight = 800,
            expectDisputed = true,
        )
    }

    @Test
    fun capture393Light() {
        renderAndCapture(
            themeMode = AzkarThemeMode.Light,
            state = AzkarGoldenReadingFixtures.GoldenMorning,
            fileName = "azkar_393x873_light.png",
            expectedWidth = 393,
            expectedHeight = 873,
            expectDisputed = true,
        )
    }

    @Test
    fun capture412Light() {
        renderAndCapture(
            themeMode = AzkarThemeMode.Light,
            state = AzkarGoldenReadingFixtures.GoldenMorning,
            fileName = "azkar_412x915_light.png",
            expectedWidth = 412,
            expectedHeight = 915,
            expectDisputed = true,
        )
    }

    @Test
    fun capture393Dark() {
        renderAndCapture(
            themeMode = AzkarThemeMode.Dark,
            state = AzkarGoldenReadingFixtures.GoldenMorning,
            fileName = "azkar_393x873_dark.png",
            expectedWidth = 393,
            expectedHeight = 873,
            expectDisputed = true,
        )
    }

    @Test
    fun capture393LongExistingDhikr() {
        renderAndCapture(
            themeMode = AzkarThemeMode.Light,
            state = AzkarGoldenReadingFixtures.LongEvening,
            fileName = "azkar_393x873_long_baqarah.png",
            expectedWidth = 393,
            expectedHeight = 873,
            expectDisputed = false,
        )
    }

    private fun renderAndCapture(
        themeMode: AzkarThemeMode,
        state: AzkarGoldenReadingUiState,
        fileName: String,
        expectedWidth: Int,
        expectedHeight: Int,
        expectDisputed: Boolean,
    ) {
        composeRule.setContent {
            AzkarTheme(themeMode = themeMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AzkarThemeValues.colors.background)
                        .verticalScroll(rememberScrollState()),
                ) {
                    AzkarGoldenReadingScreen(state = state)
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar-reading-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-arabic").assertExists()
        composeRule.onNodeWithTag("azkar-translation").assertExists()
        composeRule.onNodeWithTag("azkar-source").assertExists()
        composeRule.onNodeWithTag("azkar-note").assertExists()
        if (expectDisputed) {
            composeRule.onNodeWithTag("azkar-disputed").assertExists()
        }

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        UiDevice.getInstance(instrumentation).waitForIdle()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        assertEquals(expectedWidth, screenshot.width)
        assertEquals(expectedHeight, screenshot.height)
        saveScreenshot(screenshot, fileName)

        composeRule.onNodeWithTag("azkar-arabic").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-translation").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-source").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-note").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-explain").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-counter").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-pager").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("azkar-footer").performScrollTo().assertIsDisplayed()
    }

    private fun saveScreenshot(bitmap: Bitmap, fileName: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "phase2-evidence")
        check(directory.exists() || directory.mkdirs()) {
            "Could not create screenshot evidence directory: $directory"
        }
        val file = File(directory, fileName)
        FileOutputStream(file).use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                "Could not encode screenshot: $fileName"
            }
        }
        check(file.isFile && file.length() > 0L) {
            "Screenshot evidence was not written: $fileName"
        }
    }
}
