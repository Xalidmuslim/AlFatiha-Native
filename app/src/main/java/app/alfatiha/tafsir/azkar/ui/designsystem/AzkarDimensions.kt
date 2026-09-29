package app.alfatiha.tafsir.azkar.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AzkarSpacing {
    val shellHorizontal = 10.dp
    val shellBottomBase = 44.dp
    val topBarHorizontal = 2.dp
    val topBarVertical = 10.dp
    val brandGap = 10.dp
    val brandSubtitleTop = 4.dp
    val sourceNoteVertical = 10.dp
    val sourceNoteHorizontal = 11.dp
    val periodTop = 12.dp
    val periodBottom = 10.dp
    val periodInternal = 4.dp
    val periodGap = 6.dp
    val progressVertical = 10.dp
    val progressHorizontal = 12.dp
    val progressBottom = 10.dp
    val progressHeaderBottom = 9.dp
    val progressHeaderGap = 8.dp
    val readingToolbarGap = 8.dp
    val readingToolbarPadding = 6.dp
    val toolbarButtonHorizontal = 10.dp
    val readingToolbarBottom = 10.dp
    val cardListGap = 10.dp
    val cardTop = 15.dp
    val cardHorizontal = 14.dp
    val cardBottom = 13.dp
    val compactCard = 12.dp
    val cardHeaderGap = 9.dp
    val headingTop = 2.dp
    val badgeTop = 5.dp
    val badgeVertical = 3.dp
    val badgeHorizontal = 7.dp
    val arabicTop = 18.dp
    val arabicBottom = 14.dp
    val translationTop = 12.dp
    val sourceTop = 12.dp
    val sourcePaddingTop = 10.dp
    val sourceGap = 8.dp
    val noteTop = 9.dp
    val noteVertical = 9.dp
    val noteHorizontal = 10.dp
    val noteGap = 7.dp
    val explainTop = 10.dp
    val counterTop = 12.dp
    val counterGap = 12.dp
    val countButtonHorizontal = 18.dp
    val pagerGap = 8.dp
    val pagerTop = 10.dp
    val pagerPadding = 8.dp
    val pagerCenterHorizontalGap = 4.dp
    val pagerHelperTop = 2.dp
    val footerTop = 18.dp
    val footerHorizontal = 8.dp
    val footerBottom = 4.dp
    val sheetHandleTop = 8.dp
    val sheetHeaderTop = 9.dp
    val sheetHeaderHorizontal = 16.dp
    val sheetHeaderBottom = 7.dp
    val settingsHeaderTop = 7.dp
    val contentsSwitchGap = 5.dp
    val contentsSwitchHorizontal = 13.dp
    val contentsSwitchBottom = 7.dp
    val contentsSwitchPadding = 5.dp
    val contentsListGap = 5.dp
    val contentsListHorizontal = 13.dp
    val contentsListBottomBase = 14.dp
    val contentsItemVertical = 7.dp
    val contentsItemHorizontal = 8.dp
    val contentsItemGap = 9.dp
    val insightBodyGap = 8.dp
    val insightBodyHorizontal = 14.dp
    val insightBodyBottomBase = 18.dp
    val insightSectionPadding = 11.dp
    val scholarGap = 8.dp
    val scholarTop = 7.dp
    val referenceGap = 5.dp
    val settingsBodyGap = 8.dp
    val settingsBodyHorizontal = 13.dp
    val settingsBodyBottomBase = 14.dp
    val livePreviewGap = 3.dp
    val livePreviewVertical = 6.dp
    val livePreviewHorizontal = 8.dp
    val settingsSectionGap = 6.dp
    val fontGridGap = 5.dp
    val fontTileGap = 1.dp
    val fontTileVertical = 6.dp
    val fontTileHorizontal = 7.dp
    val controlGap = 6.dp
    val controlVertical = 7.dp
    val controlHorizontal = 8.dp
    val styleGridGap = 5.dp
    val toggleGridGap = 5.dp
    val toggleGridPadding = 6.dp
    val toggleItemGap = 5.dp
    val toggleItemVertical = 5.dp
    val toggleItemHorizontal = 6.dp
    val switchThumbInset = 2.dp
    val switchCheckedLeft = 15.dp
    val themeRowGap = 10.dp
    val themeRowVertical = 5.dp
    val themeRowHorizontal = 7.dp
    val themeSelectPadding = 6.dp
}

object AzkarRadius {
    val brandIcon = 13.dp
    val settingsIcon = 12.dp
    val sourceNote = 12.dp
    val periodTabs = 12.dp
    val periodButton = 9.dp
    val progressCard = 14.dp
    val pill = 999.dp
    val readingToolbar = 13.dp
    val toolbarButton = 9.dp
    val dhikrCard = 15.dp
    val numberChip = 8.dp
    val noteBox = 10.dp
    val explainButton = 10.dp
    val countButton = 14.dp
    val pager = 13.dp
    val pagerButton = 11.dp
    val sheetTop = 24.dp
    val contentsModeSwitch = 11.dp
    val contentsModeButton = 8.dp
    val contentsItem = 10.dp
    val insightSection = 12.dp
    val scholarIndex = 7.dp
    val settingsPreview = 10.dp
    val fontTile = 9.dp
    val control = 10.dp
    val toggleGrid = 10.dp
    val toggleItem = 8.dp
    val themeRow = 8.dp
    val themeSelect = 8.dp
}

object AzkarBorders {
    val thin = 1.dp
    val completedStrip = 3.dp
    const val dashedSegmentMultiplier = 3f
}

@Immutable
data class AzkarShadowLayer(
    val offsetY: Dp,
    val blurRadius: Dp,
    val color: Color,
)

@Immutable
data class AzkarElevationScheme(val card: List<AzkarShadowLayer>)

private fun rgba(red: Int, green: Int, blue: Int, alpha: Float) = Color(
    red = red / 255f, green = green / 255f, blue = blue / 255f, alpha = alpha,
)

object AzkarElevation {
    val LightCard = listOf(
        AzkarShadowLayer(2.dp, 4.dp, rgba(24, 31, 27, 0.05f)),
        AzkarShadowLayer(14.dp, 32.dp, rgba(24, 31, 27, 0.06f)),
    )
    val DarkCard = listOf(AzkarShadowLayer(6.dp, 22.dp, rgba(0, 0, 0, 0.28f)))
    val BottomSheet = listOf(AzkarShadowLayer((-12).dp, 40.dp, rgba(0, 0, 0, 0.18f)))
    val ActivePeriod = listOf(AzkarShadowLayer(1.dp, 4.dp, rgba(0, 0, 0, 0.06f)))
    val Light = AzkarElevationScheme(LightCard)
    val Dark = AzkarElevationScheme(DarkCard)
}

object AzkarDimensions {
    val shellMaxWidth = 760.dp
    val topBarMinHeight = 72.dp
    val brandIcon = 40.dp
    val settingsIconButton = 42.dp
    val compactIconButton = 36.dp
    val periodButtonMinHeight = 42.dp
    val progressTrackHeight = 5.dp
    val readingToolbarStickyTopBase = 5.dp
    val toolbarButtonMinHeight = 36.dp
    val dhikrScrollMarginTop = 62.dp
    val pagedViewportReservedHeight = 172.dp
    val numberChip = 29.dp
    val doneMarker = 28.dp
    val explainButtonMinHeight = 40.dp
    val countButtonMinWidth = 150.dp
    val countButtonMinHeight = 48.dp
    val pagerButtonMinHeight = 44.dp
    val pagerCenterMinWidth = 70.dp
    val sheetMaxWidth = 760.dp
    const val sheetMaxHeightFraction = 0.82f
    const val insightSheetMaxHeightFraction = 0.84f
    const val settingsSheetMaxHeightFraction = 0.72f
    val sheetHandleWidth = 42.dp
    val sheetHandleHeight = 4.dp
    val sheetCloseButton = 30.dp
    val sheetCloseTop = 34.dp
    val sheetCloseRight = 14.dp
    val contentsItemMinHeight = 50.dp
    val scholarIndex = 24.dp
    val settingsFontTileMinHeight = 62.dp
    val toggleItemMinHeight = 42.dp
    val switchTrackWidth = 30.dp
    val switchTrackHeight = 17.dp
    val switchThumb = 13.dp
    val themeSelectMaxWidth = 120.dp
    val responsiveBreakpoint = 370.dp
    const val brandIconGlyphSp = 23f
    const val settingsIconGlyphSp = 19f
    const val compactIconGlyphSp = 16f
    const val defaultArabicSizeSp = 32f
    const val defaultRussianSizeSp = 17f
    const val defaultReaderLineHeight = 1.65f
    const val arabicSizeMinSp = 22f
    const val arabicSizeMaxSp = 44f
    const val arabicSizeStepSp = 1f
    const val russianSizeMinSp = 13f
    const val russianSizeMaxSp = 25f
    const val russianSizeStepSp = 1f
    const val lineHeightMin = 1.25f
    const val lineHeightMax = 2.00f
    const val lineHeightStep = 0.05f
    const val arabicPreviewMaxSp = 24f
    const val russianPreviewMaxSp = 15f
}
