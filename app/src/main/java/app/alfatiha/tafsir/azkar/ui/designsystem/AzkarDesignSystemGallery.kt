package app.alfatiha.tafsir.azkar.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

private const val ArabicSample = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"
private const val RussianSample = "О Аллах, Ты — мой Господь."

@Preview(
    name = "Azkar Phase 1 — Light + Dark",
    widthDp = 760,
    heightDp = 900,
    showBackground = true,
)
@Composable
private fun AzkarDesignSystemGalleryPreview() {
    Row(modifier = Modifier.fillMaxSize()) {
        AzkarTheme(themeMode = AzkarThemeMode.Light, prepareEdgeToEdge = false) {
            GalleryPane(title = "Light", modifier = Modifier.weight(1f).fillMaxHeight())
        }
        AzkarTheme(themeMode = AzkarThemeMode.Dark, prepareEdgeToEdge = false) {
            GalleryPane(title = "Dark", modifier = Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun GalleryPane(title: String, modifier: Modifier = Modifier) {
    AzkarSurface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AzkarSpacing.shellHorizontal),
            verticalArrangement = Arrangement.spacedBy(AzkarSpacing.cardListGap),
        ) {
            BasicText(
                text = title,
                style = AzkarThemeValues.typography.sheetTitle.copy(
                    color = AzkarThemeValues.colors.foreground,
                ),
            )
            AzkarCardSurface(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AzkarSpacing.cardListGap)) {
                    AzkarBadge(text = "есть разногласие")
                    BasicText(
                        text = RussianSample,
                        style = AzkarThemeValues.typography.translation.copy(
                            color = AzkarThemeValues.colors.foreground,
                        ),
                    )
                    BasicText(
                        text = ArabicSample,
                        modifier = Modifier.fillMaxWidth(),
                        style = AzkarThemeValues.typography.arabicBody.copy(
                            color = AzkarThemeValues.colors.foreground,
                        ),
                    )
                    AzkarProgressBar(progress = 0.64f, animate = false)
                }
            }
            AzkarPrimaryButton(
                text = "Прочитано",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            AzkarOutlineButton(
                text = "Разъяснение · история · слова учёных",
                onClick = {},
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.readingToolbarGap)) {
                AzkarIconButton(onClick = {}) {
                    BasicText(
                        text = "⋮",
                        style = AzkarThemeValues.typography.toolbarButton.copy(
                            color = AzkarThemeValues.colors.foreground,
                        ),
                    )
                }
                AzkarIconButton(onClick = {}, size = AzkarIconButtonSize.Compact) {
                    BasicText(
                        text = "⚙",
                        style = AzkarThemeValues.typography.toolbarButton.copy(
                            color = AzkarThemeValues.colors.foreground,
                        ),
                    )
                }
            }
        }
    }
}
