package app.alfatiha.tafsir.azkar.ui.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarTheme
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarThemeMode
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarThemeValues

@Preview(name = "360 Light", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun Golden360Light() {
    GoldenPreview(AzkarThemeMode.Light, AzkarGoldenReadingFixtures.GoldenMorning)
}

@Preview(name = "393 Light", widthDp = 393, heightDp = 873, showSystemUi = true)
@Composable
private fun Golden393Light() {
    GoldenPreview(AzkarThemeMode.Light, AzkarGoldenReadingFixtures.GoldenMorning)
}

@Preview(name = "412 Light", widthDp = 412, heightDp = 915, showSystemUi = true)
@Composable
private fun Golden412Light() {
    GoldenPreview(AzkarThemeMode.Light, AzkarGoldenReadingFixtures.GoldenMorning)
}

@Preview(name = "393 Dark", widthDp = 393, heightDp = 873, showSystemUi = true)
@Composable
private fun Golden393Dark() {
    GoldenPreview(AzkarThemeMode.Dark, AzkarGoldenReadingFixtures.GoldenMorning)
}

@Preview(name = "393 Long Existing Dhikr", widthDp = 393, heightDp = 873, showSystemUi = true)
@Composable
private fun Golden393Long() {
    GoldenPreview(AzkarThemeMode.Light, AzkarGoldenReadingFixtures.LongEvening)
}

@Composable
private fun GoldenPreview(
    themeMode: AzkarThemeMode,
    state: AzkarGoldenReadingUiState,
) {
    AzkarTheme(
        themeMode = themeMode,
        prepareEdgeToEdge = false,
    ) {
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
