package app.xalidmuslim.azkar.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.xalidmuslim.azkar.content.AzkarExplanationEnrichment
import app.xalidmuslim.azkar.ui.designsystem.ArabicFontFamily
import app.xalidmuslim.azkar.ui.designsystem.AzkarBorders
import app.xalidmuslim.azkar.ui.designsystem.AzkarConfirmDialog
import app.xalidmuslim.azkar.ui.designsystem.AzkarDimensions
import app.xalidmuslim.azkar.ui.designsystem.AzkarElevation
import app.xalidmuslim.azkar.ui.designsystem.AzkarFontFamilies
import app.xalidmuslim.azkar.ui.designsystem.AzkarMotion
import app.xalidmuslim.azkar.ui.designsystem.AzkarOutlineButton
import app.xalidmuslim.azkar.ui.designsystem.AzkarRadius
import app.xalidmuslim.azkar.ui.designsystem.AzkarSpacing
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeMode
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeValues
import app.xalidmuslim.azkar.ui.designsystem.RussianFontFamily
import app.xalidmuslim.azkar.ui.designsystem.azkarShadow
import kotlin.math.roundToInt

internal object AzkarSheetTestTags {
    const val Overlay = "azkar-sheet-overlay"
    const val Sheet = "azkar-sheet"
    const val Close = "azkar-sheet-close"
    const val Settings = "azkar-settings-sheet"
    const val Contents = "azkar-contents-sheet"
    const val Explanation = "azkar-explanation-sheet"
    const val ContentsItemPrefix = "azkar-contents-item-"
    const val ContentsCardsMode = "azkar-contents-mode-cards"
    const val ContentsListMode = "azkar-contents-mode-list"
    const val SettingTranslation = "azkar-setting-translation"
    const val SettingTransliteration = "azkar-setting-transliteration"
    const val SettingSources = "azkar-setting-sources"
    const val SettingNotes = "azkar-setting-notes"
    const val ThemeLight = "azkar-theme-light"
    const val ThemeDark = "azkar-theme-dark"
    const val ThemeSystem = "azkar-theme-system"
}

@Composable
internal fun AzkarReaderSheetHost(
    activeSheet: AzkarReaderSheet,
    entries: List<AzkarReaderEntry>,
    activeIndex: Int,
    selectedExplanationId: String?,
    selectedActionId: String?,
    settings: AzkarReaderSettings,
    viewMode: AzkarReaderViewMode,
    onDismiss: () -> Unit,
    onResetSettings: () -> Unit,
    onDecrementProgress: (String, Int) -> Unit,
    onResetItemProgress: (String) -> Unit,
    onSelectContents: (Int) -> Unit,
    onViewModeChange: (AzkarReaderViewMode) -> Unit,
    onUpdateSettings: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
) {
    AnimatedVisibility(
        visible = activeSheet != AzkarReaderSheet.None,
        enter = fadeIn(tween(AzkarMotion.sheetDurationMillis)) +
            slideInVertically(
                animationSpec = tween(
                    durationMillis = AzkarMotion.sheetDurationMillis,
                    easing = AzkarMotion.sheetEasing,
                ),
                initialOffsetY = { (it * 0.02f).roundToInt().coerceAtLeast(1) },
            ),
        exit = fadeOut(tween(AzkarMotion.sheetDurationMillis)) +
            slideOutVertically(
                animationSpec = tween(
                    durationMillis = AzkarMotion.sheetDurationMillis,
                    easing = AzkarMotion.sheetEasing,
                ),
                targetOffsetY = { (it * 0.02f).roundToInt().coerceAtLeast(1) },
            ),
    ) {
        when (activeSheet) {
            AzkarReaderSheet.Settings -> AzkarSettingsSheet(
                settings = settings,
                viewMode = viewMode,
                onDismiss = onDismiss,
                onUpdateSettings = onUpdateSettings,
                onViewModeChange = onViewModeChange,
                onResetSettings = onResetSettings,
            )
            AzkarReaderSheet.Contents -> AzkarContentsSheet(
                entries = entries,
                activeIndex = activeIndex,
                onDismiss = onDismiss,
                onSelect = onSelectContents,
            )
            AzkarReaderSheet.Explanation -> {
                val item = entries.firstOrNull { it.item.id == selectedExplanationId }?.item
                    ?: entries.getOrNull(activeIndex)?.item
                if (item != null) {
                    AzkarExplanationSheet(item = item, onDismiss = onDismiss)
                }
            }
            AzkarReaderSheet.SourceInfo -> AzkarSourceInfoSheet(onDismiss = onDismiss)
            AzkarReaderSheet.Actions -> {
                val entry = entries.firstOrNull { it.item.id == selectedActionId }
                    ?: entries.getOrNull(activeIndex)
                if (entry != null) {
                    AzkarActionsSheet(
                        entry = entry,
                        onDismiss = onDismiss,
                        onDecrementProgress = onDecrementProgress,
                        onResetItemProgress = onResetItemProgress,
                    )
                }
            }
            AzkarReaderSheet.None -> Unit
        }
    }
}

@Composable
private fun AzkarBottomSheet(
    title: String,
    eyebrow: String,
    subtitle: String?,
    maxHeightFraction: Float,
    specificTestTag: String,
    headerTopPadding: Dp = AzkarSpacing.sheetHeaderTop,
    fixedHeight: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val overlayInteractionSource = remember { MutableInteractionSource() }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 72.dp.toPx() }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        dragOffsetPx = (dragOffsetPx + delta).coerceAtLeast(0f)
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag(AzkarSheetTestTags.Overlay)
            .clickable(
                interactionSource = overlayInteractionSource,
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val sheetShape = RoundedCornerShape(
            topStart = AzkarRadius.sheetTop,
            topEnd = AzkarRadius.sheetTop,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = AzkarDimensions.sheetMaxWidth)
                .then(
                    if (fixedHeight) {
                        Modifier.height(maxHeight * maxHeightFraction)
                    } else {
                        Modifier.heightIn(max = maxHeight * maxHeightFraction)
                    },
                )
                .graphicsLayer { translationY = dragOffsetPx }
                .azkarShadow(AzkarElevation.BottomSheet, AzkarRadius.sheetTop)
                .clip(sheetShape)
                .background(colors.card)
                .border(AzkarBorders.thin, colors.border, sheetShape)
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = {},
                )
                .testTag(specificTestTag),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = {
                            if (dragOffsetPx >= dismissThresholdPx) {
                                onDismiss()
                            } else {
                                dragOffsetPx = 0f
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(AzkarDimensions.sheetHandleWidth)
                        .height(AzkarDimensions.sheetHandleHeight)
                        .clip(RoundedCornerShape(AzkarRadius.pill))
                        .background(colors.sheetHandle),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AzkarSpacing.sheetHeaderHorizontal,
                        end = AzkarSpacing.sheetHeaderHorizontal,
                        top = headerTopPadding,
                        bottom = AzkarSpacing.sheetHeaderBottom,
                    ),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    if (eyebrow.isNotBlank()) {
                        BasicText(
                            text = eyebrow.uppercase(),
                            style = AzkarThemeValues.typography.sheetEyebrow.copy(color = colors.primary),
                        )
                    }
                    BasicText(
                        text = title,
                        style = AzkarThemeValues.typography.sheetTitle.copy(color = colors.foreground),
                    )
                    subtitle?.let {
                        BasicText(
                            text = it,
                            style = AzkarThemeValues.typography.sheetSubtitle.copy(color = colors.muted),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(AzkarDimensions.sheetCloseButton)
                        .clickable(onClick = onDismiss)
                        .testTag(AzkarSheetTestTags.Close),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "×",
                        style = AzkarThemeValues.typography.sheetTitle.copy(
                            color = colors.foreground,
                            fontSize = 27.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun AzkarContentsSheet(
    entries: List<AzkarReaderEntry>,
    activeIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = AzkarThemeValues.colors
    AzkarBottomSheet(
        title = "Содержание",
        eyebrow = "Навигация",
        subtitle = "Выберите азкар",
        maxHeightFraction = AzkarDimensions.sheetMaxHeightFraction,
        specificTestTag = AzkarSheetTestTags.Contents,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(
                    start = AzkarSpacing.contentsListHorizontal,
                    end = AzkarSpacing.contentsListHorizontal,
                    bottom = AzkarSpacing.contentsListBottomBase,
                ),
            verticalArrangement = Arrangement.spacedBy(AzkarSpacing.contentsListGap),
        ) {
            entries.forEachIndexed { index, entry ->
                val active = index == activeIndex
                val completed = entry.currentCount >= entry.item.count
                val shape = RoundedCornerShape(AzkarRadius.contentsItem)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = AzkarDimensions.contentsItemMinHeight)
                        .clip(shape)
                        .background(if (active) colors.activeItemBackground else colors.card)
                        .border(
                            AzkarBorders.thin,
                            if (active) colors.primary else colors.border,
                            shape,
                        )
                        .clickable { onSelect(index) }
                        .padding(
                            horizontal = AzkarSpacing.contentsItemHorizontal,
                            vertical = AzkarSpacing.contentsItemVertical,
                        )
                        .testTag(AzkarSheetTestTags.ContentsItemPrefix + index),
                    horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.contentsItemGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = (index + 1).toString().padStart(2, '0'),
                        style = AzkarThemeValues.typography.cardNumber.copy(
                            color = if (active) colors.primary else colors.muted,
                        ),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = entry.item.title,
                            style = AzkarThemeValues.typography.contentsTitle.copy(
                                color = colors.foreground,
                            ),
                        )
                        BasicText(
                            text = "Повторений: ${entry.item.count}",
                            style = AzkarThemeValues.typography.contentsSubtitle.copy(
                                color = colors.muted,
                            ),
                        )
                    }
                    BasicText(
                        text = when {
                            active -> "Сейчас"
                            completed -> "✓"
                            else -> ""
                        },
                        style = AzkarThemeValues.typography.contentsSubtitle.copy(
                            color = if (completed || active) colors.success else colors.muted,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AzkarExplanationSheet(
    item: AzkarReadingItem,
    onDismiss: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val explanation = item.explanation
    val extra = AzkarExplanationEnrichment.forId(item.id)
    val meaning = explanation?.meaning?.takeIf { it.isNotBlank() } ?: item.translation
    val relatedReports = buildList {
        explanation?.relatedReport?.takeIf { it.isNotBlank() }?.let(::add)
        AzkarExplanationEnrichment.reportsForId(item.id)
            .filter { it.isNotBlank() }
            .let(::addAll)
    }.distinct()
    val deepDive = AzkarExplanationEnrichment.deepDiveForId(item.id)
        .takeIf { it.isNotBlank() }
    val keyMeanings = extra.keyMeanings.filter { it.isNotBlank() }
    val heartFocus = extra.heartFocus?.takeIf { it.isNotBlank() }
    val benefits = extra.benefits.filter { it.isNotBlank() }
    val practicalApplication = extra.practicalApplication?.takeIf { it.isNotBlank() }
    val itemNote = item.note?.takeIf { it.isNotBlank() }
    val scholarNotes = buildList {
        explanation?.scholarNotes
            ?.filter { it.isNotBlank() }
            ?.let(::addAll)
        extra.scholarNotes.filter { it.isNotBlank() }.let(::addAll)
    }.distinct()
    val references = buildList {
        explanation?.references?.filter { it.isNotBlank() }?.let(::addAll)
        extra.references.filter { it.isNotBlank() }.let(::addAll)
        item.source.takeIf { it.isNotBlank() }?.let(::add)
    }.distinct()

    AzkarBottomSheet(
        title = item.title,
        eyebrow = "Разъяснение",
        subtitle = null,
        maxHeightFraction = AzkarDimensions.insightSheetMaxHeightFraction,
        specificTestTag = AzkarSheetTestTags.Explanation,
        fixedHeight = true,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(
                    start = AzkarSpacing.insightBodyHorizontal,
                    end = AzkarSpacing.insightBodyHorizontal,
                    bottom = AzkarSpacing.insightBodyBottomBase,
                ),
            verticalArrangement = Arrangement.spacedBy(AzkarSpacing.insightBodyGap),
        ) {
            AzkarInsightSection(
                title = "Смысл и разбор",
                tone = AzkarInsightTone.Meaning,
                initiallyExpanded = true,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText(
                        text = meaning,
                        style = AzkarThemeValues.typography.insightBody.copy(
                            color = colors.foreground,
                        ),
                    )
                    if (keyMeanings.isNotEmpty()) {
                        BasicText(
                            text = keyMeanings.joinToString("\n\n") { "• $it" },
                            style = AzkarThemeValues.typography.insightBody.copy(
                                color = colors.foreground,
                            ),
                        )
                    }
                    deepDive?.let { details ->
                        BasicText(
                            text = details,
                            style = AzkarThemeValues.typography.insightBody.copy(
                                color = colors.foreground,
                            ),
                        )
                    }
                }
            }

            if (heartFocus != null || benefits.isNotEmpty() || practicalApplication != null) {
                AzkarInsightSection("Размышление и польза", AzkarInsightTone.Heart) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        heartFocus?.let { focus ->
                            BasicText(
                                text = focus,
                                style = AzkarThemeValues.typography.insightBody.copy(
                                    color = colors.foreground,
                                ),
                            )
                        }
                        if (benefits.isNotEmpty()) {
                            BasicText(
                                text = benefits.joinToString("\n\n") { "• $it" },
                                style = AzkarThemeValues.typography.insightBody.copy(
                                    color = colors.foreground,
                                ),
                            )
                        }
                        practicalApplication?.let { application ->
                            BasicText(
                                text = "Практика: $application",
                                style = AzkarThemeValues.typography.insightBody.copy(
                                    color = colors.foreground,
                                ),
                            )
                        }
                    }
                }
            }

            if (relatedReports.isNotEmpty()) {
                AzkarInsightSection("Хадисы и истории", AzkarInsightTone.Report) {
                    BasicText(
                        text = relatedReports.joinToString("\n\n") { "• $it" },
                        style = AzkarThemeValues.typography.insightBody.copy(
                            color = colors.foreground,
                        ),
                    )
                }
            }

            if (scholarNotes.isNotEmpty()) {
                AzkarInsightSection("Слова учёных", AzkarInsightTone.Scholar) {
                    BasicText(
                        text = scholarNotes.joinToString("\n\n") { "• $it" },
                        style = AzkarThemeValues.typography.insightBody.copy(
                            color = colors.foreground,
                        ),
                    )
                }
            }

            if (itemNote != null || references.isNotEmpty()) {
                AzkarInsightSection("Примечания и источники", AzkarInsightTone.Reference) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemNote?.let { note ->
                            BasicText(
                                text = note,
                                style = AzkarThemeValues.typography.insightBody.copy(
                                    color = colors.foreground,
                                ),
                            )
                        }
                        if (references.isNotEmpty()) {
                            BasicText(
                                text = references.joinToString("\n"),
                                style = AzkarThemeValues.typography.insightReferences.copy(
                                    color = colors.muted,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class AzkarInsightTone {
    Meaning,
    KeyMeaning,
    Heart,
    Report,
    Note,
    Scholar,
    Benefit,
    Practice,
    Reference,
}

@Composable
private fun AzkarInsightSection(
    title: String,
    tone: AzkarInsightTone,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.insightSection)
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    val accent = when (tone) {
        AzkarInsightTone.Meaning -> Color(0xFFA8865E)
        AzkarInsightTone.KeyMeaning -> Color(0xFF72866F)
        AzkarInsightTone.Heart -> Color(0xFF71859D)
        AzkarInsightTone.Report -> Color(0xFF8D7968)
        AzkarInsightTone.Note -> Color(0xFFA17D51)
        AzkarInsightTone.Scholar -> Color(0xFF83758D)
        AzkarInsightTone.Benefit -> Color(0xFF70907C)
        AzkarInsightTone.Practice -> Color(0xFFA17A68)
        AzkarInsightTone.Reference -> Color(0xFF798386)
    }
    val background = lerp(colors.surface, accent, if (expanded) 0.10f else 0.06f)
    val border = lerp(colors.border, accent, 0.30f)
    val heading = lerp(colors.foreground, accent, 0.48f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(AzkarBorders.thin, border, shape)
            .padding(AzkarSpacing.insightSectionPadding),
        verticalArrangement = Arrangement.spacedBy(AzkarSpacing.scholarTop),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .semantics {
                    contentDescription = if (expanded) "$title. Свернуть" else "$title. Раскрыть"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(heading),
            )
            BasicText(
                text = title,
                modifier = Modifier.weight(1f),
                style = AzkarThemeValues.typography.sectionHeading.copy(color = heading),
            )
            BasicText(
                text = if (expanded) "⌃" else "⌄",
                style = AzkarThemeValues.typography.sectionHeading.copy(color = colors.muted),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(
                    durationMillis = AzkarMotion.toggleDurationMillis,
                    easing = AzkarMotion.sheetEasing,
                ),
                expandFrom = Alignment.Top,
            ) +
                fadeIn(tween(AzkarMotion.toggleDurationMillis)) +
                slideInVertically(
                    animationSpec = tween(
                        durationMillis = AzkarMotion.toggleDurationMillis,
                        easing = AzkarMotion.sheetEasing,
                    ),
                    initialOffsetY = { (-it * 0.02f).roundToInt() },
                ),
            exit = shrinkVertically(
                animationSpec = tween(
                    durationMillis = AzkarMotion.toggleDurationMillis,
                    easing = AzkarMotion.sheetEasing,
                ),
                shrinkTowards = Alignment.Top,
            ) +
                fadeOut(tween(AzkarMotion.toggleDurationMillis)) +
                slideOutVertically(
                    animationSpec = tween(
                        durationMillis = AzkarMotion.toggleDurationMillis,
                        easing = AzkarMotion.sheetEasing,
                    ),
                    targetOffsetY = { (-it * 0.02f).roundToInt() },
                ),
        ) {
            Box(modifier = Modifier.padding(top = 2.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AzkarSourceInfoSheet(onDismiss: () -> Unit) {
    val colors = AzkarThemeValues.colors
    AzkarBottomSheet(
        title = "Об источниках и переводе",
        eyebrow = "",
        subtitle = "Подборка, оценки передач и русский текст",
        maxHeightFraction = 0.56f,
        specificTestTag = "azkar-source-info-sheet",
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = AzkarSpacing.insightBodyHorizontal,
                    end = AzkarSpacing.insightBodyHorizontal,
                    bottom = AzkarSpacing.insightBodyBottomBase,
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicText(
                text = "Основа списка — Абдуль-Азиз ат-Тарифи, «Утренние и вечерние азкары: передача и исследование».",
                style = AzkarThemeValues.typography.insightBody.copy(color = colors.foreground),
            )
            BasicText(
                text = "Спорные оценки отмечены отдельно; подробности вынесены в примечания и разъяснения.",
                style = AzkarThemeValues.typography.sheetSubtitle.copy(color = colors.muted),
            )
            BasicText(
                text = "Русский текст — смысловой перевод. Дополнительные оценки и разногласия указываются отдельно.",
                style = AzkarThemeValues.typography.sheetSubtitle.copy(color = colors.muted),
            )
        }
    }
}

@Composable
private fun AzkarActionsSheet(
    entry: AzkarReaderEntry,
    onDismiss: () -> Unit,
    onDecrementProgress: (String, Int) -> Unit,
    onResetItemProgress: (String) -> Unit,
) {
    val item = entry.item
    val clipboard = LocalClipboardManager.current
    var showResetDialog by rememberSaveable(item.id) { mutableStateOf(false) }

    AzkarBottomSheet(
        title = "Ещё",
        eyebrow = "",
        subtitle = item.title,
        maxHeightFraction = 0.68f,
        specificTestTag = "azkar-actions-sheet",
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AzkarSpacing.settingsBodyHorizontal,
                    end = AzkarSpacing.settingsBodyHorizontal,
                    bottom = AzkarSpacing.settingsBodyBottomBase,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entry.currentCount > 0) {
                AzkarOutlineButton(
                    text = "Отменить последнее (${entry.currentCount} → ${entry.currentCount - 1})",
                    onClick = {
                        onDecrementProgress(item.id, item.count)
                        onDismiss()
                    },
                )
                AzkarOutlineButton(
                    text = "Сбросить только этот азкар",
                    onClick = { showResetDialog = true },
                )
            }
            AzkarOutlineButton(
                text = "Копировать арабский текст",
                onClick = {
                    clipboard.setText(AnnotatedString(item.arabic))
                    onDismiss()
                },
            )
            if (item.transliteration.isNotBlank()) {
                AzkarOutlineButton(
                    text = "Копировать транскрипцию",
                    onClick = {
                        clipboard.setText(AnnotatedString(item.transliteration))
                        onDismiss()
                    },
                )
            }
            AzkarOutlineButton(
                text = "Копировать перевод",
                onClick = {
                    clipboard.setText(AnnotatedString(item.translation))
                    onDismiss()
                },
            )
            AzkarOutlineButton(
                text = "Копировать целиком",
                onClick = {
                    val completeText = buildString {
                        appendLine(item.title)
                        appendLine()
                        appendLine(item.arabic)
                        if (item.transliteration.isNotBlank()) {
                            appendLine()
                            appendLine(item.transliteration)
                        }
                        appendLine()
                        appendLine(item.translation)
                        if (item.source.isNotBlank()) {
                            appendLine()
                            append("Источник: ")
                            append(item.source)
                        }
                    }
                    clipboard.setText(AnnotatedString(completeText.trim()))
                    onDismiss()
                },
            )
        }
    }

    if (showResetDialog) {
        AzkarConfirmDialog(
            title = "Сбросить этот азкар?",
            message = "Счётчик «${item.title}» будет обнулён только за сегодня.",
            confirmText = "Сбросить",
            onConfirm = {
                onResetItemProgress(item.id)
                onDismiss()
            },
            onDismiss = { showResetDialog = false },
        )
    }
}

@Composable
private fun AzkarSettingsSheet(
    settings: AzkarReaderSettings,
    viewMode: AzkarReaderViewMode,
    onDismiss: () -> Unit,
    onUpdateSettings: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
    onViewModeChange: (AzkarReaderViewMode) -> Unit,
    onResetSettings: () -> Unit,
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    AzkarBottomSheet(
        title = "Настройки чтения",
        eyebrow = "",
        subtitle = "Изменения применяются сразу",
        maxHeightFraction = AzkarDimensions.settingsSheetMaxHeightFraction,
        specificTestTag = AzkarSheetTestTags.Settings,
        headerTopPadding = AzkarSpacing.settingsHeaderTop,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier.padding(
                    start = AzkarSpacing.settingsBodyHorizontal,
                    end = AzkarSpacing.settingsBodyHorizontal,
                    bottom = AzkarSpacing.settingsBodyGap,
                ),
            ) {
                AzkarSettingsPreview(settings)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(
                        start = AzkarSpacing.settingsBodyHorizontal,
                        end = AzkarSpacing.settingsBodyHorizontal,
                        bottom = AzkarSpacing.settingsBodyBottomBase,
                    ),
                verticalArrangement = Arrangement.spacedBy(AzkarSpacing.settingsBodyGap),
            ) {
                AzkarFontSettings(settings, onUpdateSettings)
                AzkarSizeSettings(settings, onUpdateSettings)
                AzkarVisibilitySettings(settings, onUpdateSettings)
                AzkarReadingBehaviorSettings(settings, onUpdateSettings)
                AzkarThemeSettings(settings, onUpdateSettings)
                AzkarOutlineButton(
                    text = "Сбросить настройки",
                    onClick = { showResetDialog = true },
                )
            }
        }
    }

    if (showResetDialog) {
        AzkarConfirmDialog(
            title = "Сбросить настройки?",
            message = "Шрифты, размеры, отображение блоков и тема вернутся к значениям по умолчанию. Прогресс чтения не изменится.",
            confirmText = "Сбросить",
            onConfirm = onResetSettings,
            onDismiss = { showResetDialog = false },
        )
    }
}

@Composable
private fun AzkarSettingsPreview(settings: AzkarReaderSettings) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.settingsPreview)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(
                horizontal = AzkarSpacing.livePreviewHorizontal,
                vertical = AzkarSpacing.livePreviewVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(AzkarSpacing.livePreviewGap),
    ) {
        BasicText(
            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
            modifier = Modifier.fillMaxWidth(),
            style = AzkarThemeValues.typography.arabicBody.copy(
                color = colors.foreground,
                fontSize = minOf(settings.arabicSizeSp, AzkarDimensions.arabicPreviewMaxSp).sp,
                textAlign = TextAlign.Right,
            ),
        )
        BasicText(
            text = "Предпросмотр русского текста",
            style = AzkarThemeValues.typography.translation.copy(
                color = colors.foreground,
                fontSize = minOf(settings.russianSizeSp, AzkarDimensions.russianPreviewMaxSp).sp,
            ),
        )
    }
}

@Composable
private fun AzkarFontSettings(
    settings: AzkarReaderSettings,
    update: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
) {
    AzkarSettingsSection("Русский шрифт") {
        val options = listOf(
            RussianFontFamily.LITERATA to "Книжный",
            RussianFontFamily.PT_SERIF to "Классика",
            RussianFontFamily.INTER to "Современный",
            RussianFontFamily.ANDROID_SANS to "Компактный",
        )
        Column(verticalArrangement = Arrangement.spacedBy(AzkarSpacing.fontGridGap)) {
            options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.fontGridGap)) {
                    row.forEach { (font, label) ->
                        AzkarFontTile(
                            title = label,
                            sample = "Азкар",
                            active = settings.russianFontFamily == font,
                            modifier = Modifier.weight(1f),
                            fontFamily = AzkarFontFamilies.russian(font),
                            onClick = { update { it.copy(russianFontFamily = font) } },
                        )
                    }
                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }

    AzkarSettingsSection("Арабский шрифт") {
        val options = listOf(
            ArabicFontFamily.NOTO_NASKH_ARABIC to "Чёткий",
            ArabicFontFamily.NOTO_SANS_ARABIC to "Османский",
            ArabicFontFamily.AMIRI to "Мусхаф",
            ArabicFontFamily.SCHEHERAZADE_NEW to "Каллиграфический",
        )
        Column(verticalArrangement = Arrangement.spacedBy(AzkarSpacing.fontGridGap)) {
            options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.fontGridGap)) {
                    row.forEach { (font, label) ->
                        AzkarFontTile(
                            title = label,
                            sample = "سُبْحَانَ اللَّهِ",
                            active = settings.arabicFontFamily == font,
                            modifier = Modifier.weight(1f),
                            fontFamily = AzkarFontFamilies.arabic(font),
                            arabicPreview = true,
                            sampleFontSizeSp = if (font == ArabicFontFamily.AMIRI) 16f else 18f,
                            sampleLineHeightSp = if (font == ArabicFontFamily.AMIRI) 34f else 28f,
                            onClick = { update { it.copy(arabicFontFamily = font) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AzkarFontTile(
    title: String,
    sample: String,
    active: Boolean,
    modifier: Modifier,
    fontFamily: FontFamily,
    arabicPreview: Boolean = false,
    sampleFontSizeSp: Float = 18f,
    sampleLineHeightSp: Float = 25f,
    onClick: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.fontTile)
    val backgroundColor = animateColorAsState(
        targetValue = if (active) colors.activeItemBackground else colors.surface,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "font-tile-background",
    ).value
    val borderColor = animateColorAsState(
        targetValue = if (active) colors.primary else colors.border,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "font-tile-border",
    ).value
    Column(
        modifier = modifier
            .height(
                if (arabicPreview) {
                    AzkarDimensions.settingsArabicFontTileHeight
                } else {
                    AzkarDimensions.settingsFontTileHeight
                },
            )
            .clip(shape)
            .background(backgroundColor)
            .border(AzkarBorders.thin, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = AzkarSpacing.fontTileHorizontal,
                vertical = AzkarSpacing.fontTileVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(AzkarSpacing.fontTileGap),
    ) {
        BasicText(
            text = title,
            style = AzkarThemeValues.typography.fontTileTitle.copy(
                color = colors.foreground,
                fontFamily = if (arabicPreview) {
                    AzkarThemeValues.typography.fontTileTitle.fontFamily
                } else {
                    fontFamily
                },
            ),
        )
        if (arabicPreview) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AzkarDimensions.settingsArabicFontSampleHeight),
                contentAlignment = Alignment.CenterEnd,
            ) {
                BasicText(
                    text = sample,
                    modifier = Modifier.fillMaxWidth(),
                    style = AzkarThemeValues.typography.arabicFontTileSample.copy(
                        color = colors.muted,
                        fontFamily = fontFamily,
                        fontSize = sampleFontSizeSp.sp,
                        lineHeight = sampleLineHeightSp.sp,
                        textAlign = TextAlign.Right,
                    ),
                )
            }
        } else {
            BasicText(
                text = sample,
                modifier = Modifier.padding(bottom = 2.dp),
                style = AzkarThemeValues.typography.fontTileSample.copy(
                    color = colors.muted,
                    fontFamily = fontFamily,
                ),
            )
        }
    }
}

@Composable
private fun AzkarSizeSettings(
    settings: AzkarReaderSettings,
    update: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.control)
    AzkarSettingsSection("Размер и интервал") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(AzkarBorders.thin, colors.border, shape)
                .padding(
                    horizontal = AzkarSpacing.controlHorizontal,
                    vertical = 4.dp,
                ),
        ) {
            AzkarValueControl(
                label = "Арабский",
                valueLabel = settings.arabicSizeSp.roundToInt().toString(),
                value = settings.arabicSizeSp,
                min = AzkarDimensions.arabicSizeMinSp,
                max = AzkarDimensions.arabicSizeMaxSp,
                step = AzkarDimensions.arabicSizeStepSp,
                onValueChange = { v -> update { it.copy(arabicSizeSp = v) } },
            )
            AzkarControlDivider()
            AzkarValueControl(
                label = "Русский",
                valueLabel = settings.russianSizeSp.roundToInt().toString(),
                value = settings.russianSizeSp,
                min = AzkarDimensions.russianSizeMinSp,
                max = AzkarDimensions.russianSizeMaxSp,
                step = AzkarDimensions.russianSizeStepSp,
                onValueChange = { v -> update { it.copy(russianSizeSp = v) } },
            )
            AzkarControlDivider()
            AzkarValueControl(
                label = "Транскрипция",
                valueLabel = settings.transliterationSizeSp.roundToInt().toString(),
                value = settings.transliterationSizeSp,
                min = AzkarDimensions.transliterationSizeMinSp,
                max = AzkarDimensions.transliterationSizeMaxSp,
                step = AzkarDimensions.transliterationSizeStepSp,
                onValueChange = { v -> update { it.copy(transliterationSizeSp = v) } },
            )
            AzkarControlDivider()
            AzkarValueControl(
                label = "Межстрочный",
                valueLabel = String.format("%.2f", settings.lineHeight),
                value = settings.lineHeight,
                min = AzkarDimensions.lineHeightMin,
                max = AzkarDimensions.lineHeightMax,
                step = AzkarDimensions.lineHeightStep,
                onValueChange = { v -> update { it.copy(lineHeight = v) } },
            )
        }
    }
}

@Composable
private fun AzkarControlDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AzkarBorders.thin)
            .background(AzkarThemeValues.colors.border),
    )
}

@Composable
private fun AzkarValueControl(
    label: String,
    valueLabel: String,
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    onValueChange: (Float) -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BasicText(
                text = label,
                modifier = Modifier.weight(1f),
                style = AzkarThemeValues.typography.sliderLabel.copy(color = colors.muted),
            )
            AzkarStepButton(
                text = "−",
                enabled = value > min,
                onClick = { onValueChange((value - step).coerceAtLeast(min)) },
            )
            BasicText(
                text = valueLabel,
                modifier = Modifier.widthIn(min = 38.dp),
                style = AzkarThemeValues.typography.sliderValue.copy(
                    color = colors.foreground,
                    textAlign = TextAlign.Center,
                ),
            )
            AzkarStepButton(
                text = "+",
                enabled = value < max,
                onClick = { onValueChange((value + step).coerceAtMost(max)) },
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .pointerInput(min, max, step) {
                    fun valueFor(x: Float): Float {
                        val raw = (x / size.width).coerceIn(0f, 1f)
                        val stepped = ((raw * (max - min)) / step).roundToInt()
                        return (min + stepped * step).coerceIn(min, max)
                    }
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> onValueChange(valueFor(offset.x)) },
                        onHorizontalDrag = { change, _ ->
                            onValueChange(valueFor(change.position.x))
                            change.consume()
                        },
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(AzkarRadius.pill))
                    .background(colors.border),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(AzkarRadius.pill))
                    .background(colors.primary),
            )
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - 14.dp) * fraction)
                    .size(14.dp)
                    .clip(RoundedCornerShape(AzkarRadius.pill))
                    .background(colors.primary),
            )
        }
    }
}

@Composable
private fun AzkarStepButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(shape)
            .background(colors.card)
            .border(AzkarBorders.thin, colors.border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.toggleText.copy(
                color = if (enabled) colors.foreground else colors.muted,
            ),
        )
    }
}

@Composable
private fun AzkarViewModeSettings(
    viewMode: AzkarReaderViewMode,
    onViewModeChange: (AzkarReaderViewMode) -> Unit,
) {
    AzkarSettingsSection("Режим просмотра") {
        Row(horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.styleGridGap)) {
            AzkarChoiceTile(
                text = "По одному",
                active = viewMode == AzkarReaderViewMode.Cards,
                modifier = Modifier.weight(1f),
                onClick = { onViewModeChange(AzkarReaderViewMode.Cards) },
            )
            AzkarChoiceTile(
                text = "Список",
                active = viewMode == AzkarReaderViewMode.List,
                modifier = Modifier.weight(1f),
                onClick = { onViewModeChange(AzkarReaderViewMode.List) },
            )
        }
    }
}

@Composable
private fun AzkarVisibilitySettings(
    settings: AzkarReaderSettings,
    update: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
) {
    AzkarSettingsSection("Показывать") {
        Column(verticalArrangement = Arrangement.spacedBy(AzkarSpacing.toggleGridGap)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.toggleGridGap),
            ) {
                AzkarToggleTile(
                    text = "Перевод",
                    checked = settings.showTranslation,
                    modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.SettingTranslation),
                    onClick = { update { it.copy(showTranslation = !it.showTranslation) } },
                )
                AzkarToggleTile(
                    text = "Транскрипция",
                    checked = settings.showTransliteration,
                    modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.SettingTransliteration),
                    onClick = {
                        update { it.copy(showTransliteration = !it.showTransliteration) }
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.toggleGridGap),
            ) {
                AzkarToggleTile(
                    text = "Источники",
                    checked = settings.showSources,
                    modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.SettingSources),
                    onClick = { update { it.copy(showSources = !it.showSources) } },
                )
                AzkarToggleTile(
                    text = "Примечания",
                    checked = settings.showNotes,
                    modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.SettingNotes),
                    onClick = { update { it.copy(showNotes = !it.showNotes) } },
                )
            }
        }
    }
}

@Composable
private fun AzkarToggleTile(
    text: String,
    checked: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.toggleItem)
    val backgroundColor = animateColorAsState(
        targetValue = if (checked) colors.activeItemBackground else colors.surface,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "toggle-background",
    ).value
    val borderColor = animateColorAsState(
        targetValue = if (checked) colors.primary else colors.border,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "toggle-border",
    ).value
    Column(
        modifier = modifier
            .height(AzkarDimensions.settingsToggleTileHeight)
            .semantics {
                stateDescription = if (checked) "Включено" else "Выключено"
            }
            .clip(shape)
            .background(backgroundColor)
            .border(AzkarBorders.thin, borderColor, shape)
            .clickable(role = Role.Switch, onClick = onClick)
            .padding(
                horizontal = AzkarSpacing.toggleItemHorizontal,
                vertical = AzkarSpacing.toggleItemVertical,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            text = text,
            maxLines = 1,
            style = AzkarThemeValues.typography.toggleText.copy(color = colors.foreground),
        )
        Row(
            modifier = Modifier.padding(top = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = if (checked) "Вкл" else "Выкл",
                style = AzkarThemeValues.typography.fontTileSubtitle.copy(
                    color = if (checked) colors.primary else colors.muted,
                ),
            )
            AzkarSwitchIndicator(checked = checked)
        }
    }
}

@Composable
private fun AzkarSwitchIndicator(checked: Boolean) {
    val colors = AzkarThemeValues.colors
    val trackColor = animateColorAsState(
        targetValue = if (checked) colors.primary else colors.border,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "switch-track",
    ).value
    val thumbColor = animateColorAsState(
        targetValue = if (checked) colors.card else colors.muted,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "switch-thumb",
    ).value
    val thumbOffset = animateDpAsState(
        targetValue = if (checked) AzkarSpacing.switchCheckedLeft
            else AzkarSpacing.switchThumbInset,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "switch-thumb-offset",
    ).value

    Box(
        modifier = Modifier
            .width(AzkarDimensions.switchTrackWidth)
            .height(AzkarDimensions.switchTrackHeight)
            .clip(RoundedCornerShape(AzkarRadius.pill))
            .background(trackColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(AzkarDimensions.switchThumb)
                .clip(RoundedCornerShape(AzkarRadius.pill))
                .background(thumbColor),
        )
    }
}

@Composable
private fun AzkarReadingBehaviorSettings(
    settings: AzkarReaderSettings,
    update: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
) {
    AzkarSettingsSection("Навигация") {
        AzkarToggleTile(
            text = "Скрывать выполненные",
            checked = settings.hideCompleted,
            modifier = Modifier.fillMaxWidth(),
            onClick = { update { it.copy(hideCompleted = !it.hideCompleted) } },
        )
    }
}

@Composable
private fun AzkarThemeSettings(
    settings: AzkarReaderSettings,
    update: ((AzkarReaderSettings) -> AzkarReaderSettings) -> Unit,
) {
    AzkarSettingsSection("Тема") {
        Row(horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.themeRowGap)) {
            AzkarChoiceTile(
                text = "Система",
                active = settings.themeMode == AzkarThemeMode.System,
                modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.ThemeSystem),
                onClick = { update { it.copy(themeMode = AzkarThemeMode.System) } },
            )
            AzkarChoiceTile(
                text = "Светлая",
                active = settings.themeMode == AzkarThemeMode.Light,
                modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.ThemeLight),
                onClick = { update { it.copy(themeMode = AzkarThemeMode.Light) } },
            )
            AzkarChoiceTile(
                text = "Тёмная",
                active = settings.themeMode == AzkarThemeMode.Dark,
                modifier = Modifier.weight(1f).testTag(AzkarSheetTestTags.ThemeDark),
                onClick = { update { it.copy(themeMode = AzkarThemeMode.Dark) } },
            )
        }
    }
}

@Composable
private fun AzkarSettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    Column(verticalArrangement = Arrangement.spacedBy(AzkarSpacing.settingsSectionGap)) {
        BasicText(
            text = title,
            style = AzkarThemeValues.typography.sectionHeading.copy(color = colors.foreground),
        )
        content()
    }
}

@Composable
private fun AzkarChoiceTile(
    text: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.themeRow)
    val backgroundColor = animateColorAsState(
        targetValue = if (active) colors.activeItemBackground else colors.surface,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "choice-background",
    ).value
    val borderColor = animateColorAsState(
        targetValue = if (active) colors.primary else colors.border,
        animationSpec = tween(AzkarMotion.stateTransitionDurationMillis),
        label = "choice-border",
    ).value
    Box(
        modifier = modifier
            .height(AzkarDimensions.settingsChoiceTileHeight)
            .clip(shape)
            .background(backgroundColor)
            .border(AzkarBorders.thin, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.themeRowTitle.copy(
                color = if (active) colors.primary else colors.foreground,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
