package app.alfatiha.tafsir.azkar.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AzkarDesignSystemTest {
    @Test
    fun themeModeMapping_isDeterministic() {
        assertEquals(false, resolveAzkarDarkTheme(AzkarThemeMode.Light, true))
        assertEquals(true, resolveAzkarDarkTheme(AzkarThemeMode.Dark, false))
        assertEquals(false, resolveAzkarDarkTheme(AzkarThemeMode.System, false))
        assertEquals(true, resolveAzkarDarkTheme(AzkarThemeMode.System, true))
    }

    @Test
    fun everyFontOption_mapsToAComposeFamily() {
        RussianFontFamily.entries.forEach { assertNotNull(AzkarFontFamilies.russian(it)) }
        ArabicFontFamily.entries.forEach { assertNotNull(AzkarFontFamilies.arabic(it)) }
    }

    @Test
    fun goldenMasterTokenInvariants_arePreserved() {
        assertEquals(15.dp, AzkarRadius.dhikrCard)
        assertEquals(13.dp, AzkarRadius.readingToolbar)
        assertEquals(14.dp, AzkarRadius.countButton)
        assertEquals(1.dp, AzkarBorders.thin)
        assertEquals(620, AzkarMotion.dhikrPageDurationMillis)
        assertEquals(32f, AzkarDimensions.defaultArabicSizeSp)
        assertEquals(17f, AzkarDimensions.defaultRussianSizeSp)
        assertEquals(1.65f, AzkarDimensions.defaultReaderLineHeight)
    }

    @Test
    fun lightAndDarkSemanticColors_matchContract() {
        assertEquals(Color(0xFFF4F0E7), AzkarColors.Light.background)
        assertEquals(Color(0xFF171C19), AzkarColors.Light.foreground)
        assertEquals(Color(0xFF1F5C48), AzkarColors.Light.primary)
        assertEquals(Color(0xFF111613), AzkarColors.Dark.background)
        assertEquals(Color(0xFFEDF2EE), AzkarColors.Dark.foreground)
        assertEquals(Color(0xFF7FB99E), AzkarColors.Dark.primary)
        assertEquals(Color(0xFF0F1713), AzkarColors.Dark.countButtonText)
    }
}
