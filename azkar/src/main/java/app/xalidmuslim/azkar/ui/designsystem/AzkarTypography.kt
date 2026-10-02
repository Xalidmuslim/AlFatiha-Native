package app.xalidmuslim.azkar.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.xalidmuslim.azkar.R

enum class RussianFontFamily {
    LITERATA, PT_SERIF, INTER, MANROPE, ANDROID_SANS, ANDROID_SERIF
}

enum class ArabicFontFamily {
    NOTO_NASKH_ARABIC, NOTO_SANS_ARABIC, AMIRI, SCHEHERAZADE_NEW
}

object AzkarFontFamilies {
    private fun variableFamily(resId: Int): FontFamily = FontFamily(
        Font(resId, weight = FontWeight.Normal),
        Font(resId, weight = FontWeight.Medium),
        Font(resId, weight = FontWeight.SemiBold),
        Font(resId, weight = FontWeight.Bold),
    )

    val Literata = variableFamily(R.font.literata_variable)
    val PtSerif = FontFamily(
        Font(R.font.pt_serif_regular, weight = FontWeight.Normal),
        Font(R.font.pt_serif_bold, weight = FontWeight.Bold),
    )
    val Inter = variableFamily(R.font.inter_variable)
    val Manrope = variableFamily(R.font.manrope_variable)
    val NotoNaskhArabic = variableFamily(R.font.noto_naskh_arabic_variable)
    val UthmanicHafs = FontFamily(
        Font(R.font.uthmanic_hafs_v18, weight = FontWeight.Normal),
        Font(R.font.uthmanic_hafs_v18, weight = FontWeight.Medium),
        Font(R.font.uthmanic_hafs_v18, weight = FontWeight.SemiBold),
        Font(R.font.uthmanic_hafs_v18, weight = FontWeight.Bold),
    )
    val AmiriQuran = FontFamily(
        Font(R.font.amiri_quran_regular, weight = FontWeight.Normal),
        Font(R.font.amiri_quran_regular, weight = FontWeight.Medium),
        Font(R.font.amiri_quran_regular, weight = FontWeight.SemiBold),
        Font(R.font.amiri_quran_regular, weight = FontWeight.Bold),
    )
    val Lateef = FontFamily(
        Font(R.font.lateef_regular, weight = FontWeight.Normal),
        Font(R.font.lateef_medium, weight = FontWeight.Medium),
        Font(R.font.lateef_semibold, weight = FontWeight.SemiBold),
        Font(R.font.lateef_bold, weight = FontWeight.Bold),
    )

    fun russian(value: RussianFontFamily): FontFamily = when (value) {
        RussianFontFamily.LITERATA -> Literata
        RussianFontFamily.PT_SERIF -> PtSerif
        RussianFontFamily.INTER -> Inter
        RussianFontFamily.MANROPE -> Manrope
        RussianFontFamily.ANDROID_SANS -> FontFamily.SansSerif
        RussianFontFamily.ANDROID_SERIF -> FontFamily.Serif
    }

    fun arabic(value: ArabicFontFamily): FontFamily = when (value) {
        ArabicFontFamily.NOTO_NASKH_ARABIC -> NotoNaskhArabic
        ArabicFontFamily.NOTO_SANS_ARABIC -> UthmanicHafs
        ArabicFontFamily.AMIRI -> AmiriQuran
        ArabicFontFamily.SCHEHERAZADE_NEW -> Lateef
    }
}

@Immutable
data class AzkarTypographySet(
    val brandTitle: TextStyle,
    val brandSubtitle: TextStyle,
    val sourceNote: TextStyle,
    val periodButton: TextStyle,
    val progressLabel: TextStyle,
    val progressCount: TextStyle,
    val resetTextButton: TextStyle,
    val toolbarButton: TextStyle,
    val toolbarPosition: TextStyle,
    val cardNumber: TextStyle,
    val cardHeading: TextStyle,
    val disputeBadge: TextStyle,
    val arabicBody: TextStyle,
    val translation: TextStyle,
    val sourceRow: TextStyle,
    val noteBox: TextStyle,
    val explainButton: TextStyle,
    val counter: TextStyle,
    val counterNumber: TextStyle,
    val countButton: TextStyle,
    val pagerButton: TextStyle,
    val pagerCenter: TextStyle,
    val pagerActiveNumber: TextStyle,
    val pagerHelper: TextStyle,
    val footer: TextStyle,
    val sheetEyebrow: TextStyle,
    val sheetTitle: TextStyle,
    val sheetSubtitle: TextStyle,
    val contentsTitle: TextStyle,
    val contentsSubtitle: TextStyle,
    val sectionHeading: TextStyle,
    val insightBody: TextStyle,
    val insightReferences: TextStyle,
    val fontTileTitle: TextStyle,
    val fontTileSubtitle: TextStyle,
    val fontTileSample: TextStyle,
    val arabicFontTileSample: TextStyle,
    val sliderLabel: TextStyle,
    val sliderValue: TextStyle,
    val toggleText: TextStyle,
    val themeRowTitle: TextStyle,
    val themeRowSubtitle: TextStyle,
    val themeSelect: TextStyle,
)

object AzkarTypography {
    private val noTrim = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )

    fun create(
        russianFont: RussianFontFamily = RussianFontFamily.ANDROID_SANS,
        arabicFont: ArabicFontFamily = ArabicFontFamily.NOTO_SANS_ARABIC,
        arabicSizeSp: Float = AzkarDimensions.defaultArabicSizeSp,
        russianSizeSp: Float = AzkarDimensions.defaultRussianSizeSp,
        readerLineHeight: Float = AzkarDimensions.defaultReaderLineHeight,
    ): AzkarTypographySet {
        val reader = AzkarFontFamilies.russian(russianFont)
        val arabic = AzkarFontFamilies.arabic(arabicFont)
        val inter = AzkarFontFamilies.Inter
        val literata = AzkarFontFamilies.Literata

        return AzkarTypographySet(
            brandTitle = TextStyle(fontFamily = literata, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 26.sp),
            brandSubtitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.11.em),
            sourceNote = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
            periodButton = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            progressLabel = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 13.sp),
            progressCount = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 14.sp),
            resetTextButton = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            toolbarButton = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            toolbarPosition = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 12.sp),
            cardNumber = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 12.sp),
            cardHeading = TextStyle(fontFamily = reader, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp),
            disputeBadge = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
            arabicBody = TextStyle(
                fontFamily = arabic,
                fontWeight = FontWeight.Medium,
                fontSize = arabicSizeSp.sp,
                lineHeight = (arabicSizeSp * readerLineHeight).sp,
                textAlign = TextAlign.Right,
                textDirection = TextDirection.Rtl,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = noTrim,
            ),
            translation = TextStyle(
                fontFamily = reader,
                fontWeight = FontWeight.Normal,
                fontSize = russianSizeSp.sp,
                lineHeight = (russianSizeSp * readerLineHeight).sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = noTrim,
            ),
            sourceRow = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
            noteBox = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
            explainButton = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 14.sp),
            counter = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 14.sp),
            counterNumber = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 22.sp),
            countButton = TextStyle(fontFamily = reader, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
            pagerButton = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            pagerCenter = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 11.sp),
            pagerActiveNumber = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 15.sp),
            pagerHelper = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 10.sp),
            footer = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
            sheetEyebrow = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 0.08.em),
            sheetTitle = TextStyle(fontFamily = literata, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp),
            sheetSubtitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
            contentsTitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
            contentsSubtitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 10.sp),
            sectionHeading = TextStyle(fontFamily = inter, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            insightBody = TextStyle(fontFamily = reader, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.5.sp),
            insightReferences = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp),
            fontTileTitle = TextStyle(fontFamily = reader, fontSize = 13.sp, lineHeight = 15.sp),
            fontTileSubtitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 12.sp),
            fontTileSample = TextStyle(fontFamily = reader, fontSize = 12.sp, lineHeight = 14.sp),
            arabicFontTileSample = TextStyle(
                fontFamily = arabic,
                fontSize = 18.sp,
                lineHeight = 25.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            ),
            sliderLabel = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 11.sp),
            sliderValue = TextStyle(fontFamily = inter, fontWeight = FontWeight.Bold, fontSize = 12.sp),
            toggleText = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
            themeRowTitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            themeRowSubtitle = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 10.sp),
            themeSelect = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        )
    }
}
