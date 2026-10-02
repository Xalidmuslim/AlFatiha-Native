package app.xalidmuslim.azkar.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.xalidmuslim.azkar.R
import app.xalidmuslim.azkar.ui.designsystem.AzkarBorders
import app.xalidmuslim.azkar.ui.designsystem.AzkarCardSurface
import app.xalidmuslim.azkar.ui.designsystem.AzkarConfirmDialog
import app.xalidmuslim.azkar.ui.designsystem.AzkarDimensions
import app.xalidmuslim.azkar.ui.designsystem.AzkarElevation
import app.xalidmuslim.azkar.ui.designsystem.AzkarFontFamilies
import app.xalidmuslim.azkar.ui.designsystem.AzkarMotion
import app.xalidmuslim.azkar.ui.designsystem.AzkarIconButton
import app.xalidmuslim.azkar.ui.designsystem.AzkarIconButtonSize
import app.xalidmuslim.azkar.ui.designsystem.AzkarPrimaryButton
import app.xalidmuslim.azkar.ui.designsystem.AzkarProgressBar
import app.xalidmuslim.azkar.ui.designsystem.AzkarRadius
import app.xalidmuslim.azkar.ui.designsystem.AzkarSpacing
import app.xalidmuslim.azkar.ui.designsystem.AzkarSurface
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeValues
import app.xalidmuslim.azkar.ui.designsystem.azkarShadow
import kotlin.math.roundToInt

internal object AzkarReadingTestTags {
    const val Screen = "azkar-reading-screen"
    const val Header = "azkar-header"
    const val SourceNote = "azkar-source-note"
    const val PeriodTabs = "azkar-period-tabs"
    const val PeriodMorning = "azkar-period-morning"
    const val PeriodEvening = "azkar-period-evening"
    const val Progress = "azkar-progress"
    const val Toolbar = "azkar-toolbar"
    const val Card = "azkar-card"
    const val Disputed = "azkar-disputed"
    const val Arabic = "azkar-arabic"
    const val Translation = "azkar-translation"
    const val Source = "azkar-source"
    const val Note = "azkar-note"
    const val Explain = "azkar-explain"
    const val Counter = "azkar-counter"
    const val ResetProgress = "azkar-reset-progress"
    const val CountActionPrefix = "azkar-count-action-"
    const val Pager = "azkar-pager"
    const val Footer = "azkar-footer"
    const val ReadingArea = "azkar-reading-area"
    const val Previous = "azkar-previous"
    const val Next = "azkar-next"
    const val OpenContents = "azkar-open-contents"
    const val ViewModeToggle = "azkar-view-mode-toggle"
    const val OpenSettingsTop = "azkar-open-settings-top"
    const val ThemeToggle = "azkar-theme-toggle"
    const val OpenSettingsToolbar = "azkar-open-settings-toolbar"
    const val List = "azkar-reader-list"
    const val ListCardPrefix = "azkar-list-card-"
}


@Composable
fun AzkarGoldenReadingScreen(
    state: AzkarGoldenReadingUiState = AzkarGoldenReadingFixtures.GoldenMorning,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    shellScrollState: ScrollState? = null,
    readingScrollState: ScrollState? = null,
    readingAreaModifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onOpenContents: () -> Unit = {},
    viewMode: AzkarReaderViewMode = AzkarReaderViewMode.Cards,
    onViewModeChange: (AzkarReaderViewMode) -> Unit = {},
    onOpenSourceInfo: () -> Unit = {},
    onOpenExplanation: (String) -> Unit = {},
    onOpenActions: (String) -> Unit = {},
    onIncrementCount: (String, Int) -> Unit = { _, _ -> },
    onResetProgress: () -> Unit = {},
    onPeriodChange: (AzkarPeriod) -> Unit = {},
    compactReader: Boolean = false,
    showTranslation: Boolean = true,
    showTransliteration: Boolean = false,
    transliterationSizeSp: Float = AzkarDimensions.defaultTransliterationSizeSp,
    showSources: Boolean = true,
    showNotes: Boolean = true,
    canPrevious: Boolean = state.position > 1,
    canNext: Boolean = state.position < state.total,
) {
    AzkarSurface(modifier = modifier.fillMaxSize().testTag(AzkarReadingTestTags.Screen)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val narrow = maxWidth <= AzkarDimensions.responsiveBreakpoint
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxHeight()
                    .widthIn(max = AzkarDimensions.shellMaxWidth)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    )
                    .padding(horizontal = AzkarSpacing.shellHorizontal)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(bottom = AzkarSpacing.shellBottomBase),
            ) {
                // Верхняя зона остаётся на месте. Прокручивается только карточка азкара.
                AzkarHeader(
                    onOpenSettings = onOpenSettings,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                )
                AzkarSourceNote(onOpenSourceInfo)
                AzkarPeriodTabs(state.period, onPeriodChange)
                AzkarProgressCard(state, onResetProgress)
                AzkarReaderToolbar(
                    state = state,
                    narrow = narrow,
                    onOpenContents = onOpenContents,
                    viewMode = viewMode,
                    onViewModeChange = onViewModeChange,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    AzkarDhikrCard(
                        state = state,
                        narrow = narrow,
                        scrollState = readingScrollState,
                        maxHeight = null,
                        interactionModifier = readingAreaModifier.fillMaxSize(),
                        compactReader = compactReader,
                        showTranslation = showTranslation,
                        showTransliteration = showTransliteration,
                        transliterationSizeSp = transliterationSizeSp,
                        showSources = showSources,
                        showNotes = showNotes,
                        onOpenExplanation = { onOpenExplanation(state.item.id) },
                        onOpenActions = { onOpenActions(state.item.id) },
                        onIncrementCount = {
                            onIncrementCount(state.item.id, state.item.count)
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun AzkarHeader(
    onOpenSettings: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    val colors = AzkarThemeValues.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AzkarDimensions.topBarMinHeight)
            .padding(
                horizontal = AzkarSpacing.topBarHorizontal,
                vertical = AzkarSpacing.topBarVertical,
            )
            .testTag(AzkarReadingTestTags.Header),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.brandGap),
        ) {
            val brandShape = RoundedCornerShape(AzkarRadius.brandIcon)
            Box(
                modifier = Modifier
                    .size(AzkarDimensions.brandIcon)
                    .clip(brandShape)
                    .background(colors.surface)
                    .border(AzkarBorders.thin, colors.border, brandShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.heart_prayer_azkar_exact),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column {
                BasicText(
                    text = "Азкары",
                    style = AzkarThemeValues.typography.brandTitle.copy(color = colors.foreground),
                )
                BasicText(
                    text = "УТРО · ВЕЧЕР",
                    modifier = Modifier.padding(top = AzkarSpacing.brandSubtitleTop),
                    style = AzkarThemeValues.typography.brandSubtitle.copy(color = colors.muted),
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AzkarIconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .semantics {
                        contentDescription = if (isDarkTheme) {
                            "Включить дневную тему"
                        } else {
                            "Включить ночную тему"
                        }
                    }
                    .testTag(AzkarReadingTestTags.ThemeToggle),
            ) {
                BasicText(
                    text = if (isDarkTheme) "☀" else "☾",
                    style = AzkarThemeValues.typography.translation.copy(
                        color = colors.foreground,
                        fontSize = 19.sp,
                    ),
                )
            }
            AzkarIconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .semantics { contentDescription = "Настройки чтения" }
                    .testTag(AzkarReadingTestTags.OpenSettingsTop),
            ) {
                BasicText(
                    text = "⚙",
                    style = AzkarThemeValues.typography.translation.copy(
                        color = colors.foreground,
                        fontSize = AzkarDimensions.settingsIconGlyphSp.sp,
                    ),
                )
            }
        }
    }
}

@Composable
internal fun AzkarSourceNote(onOpenSourceInfo: () -> Unit = {}) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.sourceNote)
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onOpenSourceInfo,
            )
            .semantics { contentDescription = "Источник списка. Подробнее" }
            .padding(
                horizontal = AzkarSpacing.sourceNoteHorizontal,
                vertical = AzkarSpacing.sourceNoteVertical,
            )
            .testTag(AzkarReadingTestTags.SourceNote),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "Источник списка · ат-Тарифи · Подробнее ›",
            maxLines = 1,
            style = AzkarThemeValues.typography.sourceNote.copy(color = colors.muted),
        )
    }
}

@Composable
internal fun AzkarPeriodTabs(
    period: AzkarPeriod,
    onPeriodChange: (AzkarPeriod) -> Unit = {},
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.periodTabs)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AzkarSpacing.periodTop, bottom = AzkarSpacing.periodBottom)
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(AzkarSpacing.periodInternal)
            .testTag(AzkarReadingTestTags.PeriodTabs),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tabWidth = (maxWidth - AzkarSpacing.periodGap) / 2
            val indicatorX by animateDpAsState(
                targetValue = if (period == AzkarPeriod.Morning) 0.dp
                    else tabWidth + AzkarSpacing.periodGap,
                animationSpec = tween(
                    durationMillis = AzkarMotion.stateTransitionDurationMillis,
                    easing = AzkarMotion.sheetEasing,
                ),
                label = "period-indicator",
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorX)
                    .width(tabWidth)
                    .defaultMinSize(minHeight = AzkarDimensions.periodButtonMinHeight)
                    .azkarShadow(AzkarElevation.ActivePeriod, AzkarRadius.periodButton)
                    .clip(RoundedCornerShape(AzkarRadius.periodButton))
                    .background(colors.card),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.periodGap),
            ) {
                AzkarPeriodButton(
                    text = "☀ Утро",
                    active = period == AzkarPeriod.Morning,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(AzkarReadingTestTags.PeriodMorning),
                    onClick = { onPeriodChange(AzkarPeriod.Morning) },
                )
                AzkarPeriodButton(
                    text = "☾ Вечер",
                    active = period == AzkarPeriod.Evening,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(AzkarReadingTestTags.PeriodEvening),
                    onClick = { onPeriodChange(AzkarPeriod.Evening) },
                )
            }
        }
    }
}

@Composable
private fun AzkarPeriodButton(
    text: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.periodButton)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = AzkarDimensions.periodButtonMinHeight)
            .clip(shape)
            .semantics {
                contentDescription = if (active) "$text, выбрано" else text
            }
            .clickable(enabled = !active, role = Role.Tab, onClick = onClick)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.periodButton.copy(
                color = if (active) colors.primary else colors.muted,
            ),
        )
    }
}

@Composable
internal fun AzkarProgressCard(
    state: AzkarGoldenReadingUiState,
    onResetProgress: () -> Unit = {},
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.progressCard)
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val roundedPercent = if (state.total == 0) 0
    else (state.completedItems.toFloat() / state.total.toFloat() * 100f).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AzkarSpacing.progressBottom)
            .azkarShadow(AzkarThemeValues.elevation.card, AzkarRadius.progressCard)
            .clip(shape)
            .background(colors.card)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(
                horizontal = AzkarSpacing.progressHorizontal,
                vertical = AzkarSpacing.progressVertical,
            )
            .testTag(AzkarReadingTestTags.Progress),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AzkarSpacing.progressHeaderBottom),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.progressHeaderGap),
        ) {
            BasicText(
                modifier = Modifier.weight(1f),
                text = buildProgressLabel(state),
                style = AzkarThemeValues.typography.progressLabel.copy(color = colors.muted),
            )
            BasicText(
                text = "Сбросить",
                modifier = Modifier
                    .clickable(role = Role.Button) { showResetDialog = true }
                    .semantics { contentDescription = "Сбросить прогресс за сегодня" }
                    .testTag(AzkarReadingTestTags.ResetProgress),
                style = AzkarThemeValues.typography.resetTextButton.copy(color = colors.muted),
            )
        }
        AzkarProgressBar(progress = roundedPercent / 100f, animate = true)
    }
    if (showResetDialog) {
        AzkarConfirmDialog(
            title = "Сбросить прогресс?",
            message = "Будет сброшен сегодняшний прогресс всех азкаров в этом списке.",
            confirmText = "Сбросить",
            onConfirm = onResetProgress,
            onDismiss = { showResetDialog = false },
        )
    }
}

@Composable
private fun buildProgressLabel(state: AzkarGoldenReadingUiState): AnnotatedString {
    val colors = AzkarThemeValues.colors
    val prefix = if (state.period == AzkarPeriod.Morning) "Утренние" else "Вечерние"
    return buildAnnotatedString {
        append("${prefix} азкары ")
        pushStyle(
            SpanStyle(
                color = colors.foreground,
                fontSize = AzkarThemeValues.typography.progressCount.fontSize,
                fontWeight = FontWeight.Medium,
            ),
        )
        append("${state.completedItems} из ${state.total}")
        pop()
    }
}

@Composable
internal fun AzkarReaderToolbar(
    state: AzkarGoldenReadingUiState,
    narrow: Boolean,
    onOpenContents: () -> Unit,
    viewMode: AzkarReaderViewMode,
    onViewModeChange: (AzkarReaderViewMode) -> Unit,
    positionText: String? = null,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.readingToolbar)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AzkarSpacing.readingToolbarBottom)
            .azkarShadow(AzkarThemeValues.elevation.card, AzkarRadius.readingToolbar)
            .clip(shape)
            .background(colors.stickyToolbarBackground)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(AzkarSpacing.readingToolbarPadding)
            .testTag(AzkarReadingTestTags.Toolbar),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.readingToolbarGap),
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicText(
                text = positionText ?: "${state.position} из ${state.total}",
                style = AzkarThemeValues.typography.toolbarPosition.copy(color = colors.muted),
            )
        }
        AzkarToolbarButton(
            text = when (viewMode) {
                AzkarReaderViewMode.Cards -> if (narrow) "Список" else "☷ Список"
                AzkarReaderViewMode.List -> if (narrow) "По одному" else "▣ По одному"
            },
            onClick = {
                onViewModeChange(
                    if (viewMode == AzkarReaderViewMode.Cards) {
                        AzkarReaderViewMode.List
                    } else {
                        AzkarReaderViewMode.Cards
                    },
                )
            },
            modifier = Modifier.testTag(AzkarReadingTestTags.ViewModeToggle),
        )
    }
}

@Composable
private fun AzkarToolbarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.toolbarButton)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = AzkarDimensions.toolbarButtonMinHeight)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(colors.card)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(horizontal = AzkarSpacing.toolbarButtonHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.toolbarButton.copy(color = colors.foreground),
        )
    }
}

@Composable
internal fun AzkarDhikrCard(
    state: AzkarGoldenReadingUiState,
    narrow: Boolean,
    scrollState: ScrollState?,
    maxHeight: Dp?,
    interactionModifier: Modifier,
    compactReader: Boolean,
    showTranslation: Boolean,
    showTransliteration: Boolean,
    transliterationSizeSp: Float,
    showSources: Boolean,
    showNotes: Boolean,
    onOpenExplanation: () -> Unit,
    onOpenActions: () -> Unit,
    onIncrementCount: () -> Unit,
) {
    val item = state.item
    val colors = AzkarThemeValues.colors
    val completed = state.currentCount >= item.count
    var transliterationExpanded by rememberSaveable(item.id, showTransliteration) {
        mutableStateOf(showTransliteration)
    }

    val cardModifier = Modifier
        .fillMaxWidth()
        .then(if (maxHeight != null) Modifier.heightIn(max = maxHeight) else Modifier)
        .testTag(AzkarReadingTestTags.Card)

    AzkarCardSurface(
        modifier = cardModifier,
        compact = compactReader,
        completed = completed,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(interactionModifier)
                .testTag(AzkarReadingTestTags.ReadingArea)
                .then(if (scrollState != null) Modifier.verticalScroll(scrollState) else Modifier),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.cardHeaderGap),
                verticalAlignment = Alignment.Top,
            ) {
                val numberShape = RoundedCornerShape(AzkarRadius.numberChip)
                Box(
                    modifier = Modifier
                        .size(AzkarDimensions.numberChip)
                        .clip(numberShape)
                        .background(colors.surface)
                        .border(AzkarBorders.thin, colors.border, numberShape),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = state.position.toString().padStart(2, '0'),
                        style = AzkarThemeValues.typography.cardNumber.copy(color = colors.muted),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = item.title,
                        modifier = Modifier.padding(top = AzkarSpacing.headingTop),
                        style = AzkarThemeValues.typography.cardHeading.copy(color = colors.foreground),
                    )
                    if (showNotes && item.disputed) {
                        val badgeShape = RoundedCornerShape(AzkarRadius.pill)
                        Box(
                            modifier = Modifier
                                .padding(top = AzkarSpacing.badgeTop)
                                .clip(badgeShape)
                                .border(AzkarBorders.thin, colors.warningBadgeBorder, badgeShape)
                                .padding(
                                    horizontal = AzkarSpacing.badgeHorizontal,
                                    vertical = AzkarSpacing.badgeVertical,
                                )
                                .testTag(AzkarReadingTestTags.Disputed),
                        ) {
                            BasicText(
                                text = "есть разногласие",
                                style = AzkarThemeValues.typography.disputeBadge.copy(
                                    color = colors.warning,
                                ),
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (completed) {
                        Box(
                            modifier = Modifier
                                .size(AzkarDimensions.doneMarker)
                                .clip(CircleShape)
                                .background(colors.doneMarkerBackground)
                                .border(AzkarBorders.thin, colors.doneMarkerBorder, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "✓",
                                style = AzkarThemeValues.typography.toolbarButton.copy(
                                    color = colors.success,
                                ),
                            )
                        }
                    }
                    AzkarIconButton(
                        onClick = onOpenActions,
                        modifier = Modifier.semantics { contentDescription = "Действия с азкаром" },
                        size = AzkarIconButtonSize.Compact,
                    ) {
                        BasicText(
                            text = "⋮",
                            style = AzkarThemeValues.typography.cardHeading.copy(
                                color = colors.muted,
                                fontSize = 20.sp,
                            ),
                        )
                    }
                }
            }

            if (item.id == "muawwidhat") {
                AzkarMuawwidhatText(
                    item = item,
                    showTranslation = showTranslation,
                )
            } else {
                BasicText(
                    text = azkarArabicDisplayText(
                        text = item.arabic,
                        bodySizeSp = AzkarThemeValues.typography.arabicBody.fontSize.value,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = AzkarSpacing.arabicTop,
                            bottom = AzkarSpacing.arabicBottom,
                        )
                        .testTag(AzkarReadingTestTags.Arabic),
                    style = AzkarThemeValues.typography.arabicBody.copy(color = colors.foreground),
                )
            }

            if (showTransliteration && item.transliteration.isNotBlank()) {
                val transliterationShape = RoundedCornerShape(AzkarRadius.noteBox)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(transliterationShape)
                        .background(colors.surface)
                        .clickable(
                            role = Role.Button,
                            onClick = { transliterationExpanded = !transliterationExpanded },
                        )
                        .semantics {
                            contentDescription = if (transliterationExpanded) {
                                "Транскрипция. Скрыть"
                            } else {
                                "Транскрипция. Показать"
                            }
                        }
                        .padding(
                            horizontal = AzkarSpacing.noteHorizontal,
                            vertical = 8.dp,
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = "Транскрипция",
                            modifier = Modifier.weight(1f),
                            style = AzkarThemeValues.typography.sourceRow.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        BasicText(
                            text = if (transliterationExpanded) "Скрыть ︿" else "Показать ﹀",
                            style = AzkarThemeValues.typography.sourceRow.copy(color = colors.muted),
                        )
                    }
                    AnimatedVisibility(
                        visible = transliterationExpanded,
                        enter = fadeIn(tween(AzkarMotion.toggleDurationMillis)) +
                            slideInVertically(
                                animationSpec = tween(
                                    durationMillis = AzkarMotion.toggleDurationMillis,
                                    easing = AzkarMotion.sheetEasing,
                                ),
                                initialOffsetY = { (-it * 0.02f).roundToInt() },
                            ),
                        exit = fadeOut(tween(AzkarMotion.toggleDurationMillis)),
                    ) {
                        BasicText(
                            text = item.transliteration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            style = AzkarThemeValues.typography.translation.copy(
                                color = colors.muted,
                                fontSize = transliterationSizeSp.sp,
                                lineHeight = (transliterationSizeSp * 1.45f).sp,
                            ),
                        )
                    }
                }
            }

            if (showTranslation && item.id != "muawwidhat") {
                BasicText(
                    text = item.translation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .topRule(colors.border, dashed = false)
                        .padding(top = AzkarSpacing.translationTop)
                        .testTag(AzkarReadingTestTags.Translation),
                    style = AzkarThemeValues.typography.translation.copy(color = colors.foreground),
                )
            }

            if (showSources) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AzkarSpacing.sourceTop)
                        .topRule(colors.border, dashed = true)
                        .padding(top = AzkarSpacing.sourcePaddingTop)
                        .testTag(AzkarReadingTestTags.Source),
                    horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.sourceGap),
                    verticalAlignment = Alignment.Top,
                ) {
                    BasicText(
                        text = "Источник",
                        style = AzkarThemeValues.typography.sourceRow.copy(color = colors.primary),
                    )
                    BasicText(
                        text = item.source,
                        modifier = Modifier.weight(1f),
                        style = AzkarThemeValues.typography.sourceRow.copy(color = colors.muted),
                    )
                }
            }

            if (showNotes) item.note?.let { note ->
                val noteShape = RoundedCornerShape(AzkarRadius.noteBox)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AzkarSpacing.noteTop)
                        .clip(noteShape)
                        .background(colors.noteBackground)
                        .border(AzkarBorders.thin, colors.noteBorder, noteShape)
                        .padding(
                            horizontal = AzkarSpacing.noteHorizontal,
                            vertical = AzkarSpacing.noteVertical,
                        )
                        .testTag(AzkarReadingTestTags.Note),
                    horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.noteGap),
                    verticalAlignment = Alignment.Top,
                ) {
                    BasicText(
                        text = "ⓘ",
                        style = AzkarThemeValues.typography.noteBox.copy(color = colors.muted),
                    )
                    BasicText(
                        text = note,
                        modifier = Modifier.weight(1f),
                        style = AzkarThemeValues.typography.noteBox.copy(color = colors.muted),
                    )
                }
            }

            if (item.hasInsight) {
                AzkarExplanationButton(
                    onClick = onOpenExplanation,
                    modifier = Modifier
                        .padding(top = AzkarSpacing.explainTop)
                        .testTag(AzkarReadingTestTags.Explain),
                )
            }

            AzkarCounterRow(
                state = state,
                completed = completed,
                narrow = narrow,
                onIncrementCount = onIncrementCount,
                modifier = Modifier
                    .padding(top = AzkarSpacing.counterTop)
                    .testTag(AzkarReadingTestTags.Counter),
            )
        }
    }
}

@Composable
private fun AzkarMuawwidhatText(
    item: AzkarReadingItem,
    showTranslation: Boolean,
) {
    val colors = AzkarThemeValues.colors
    val arabicParts = item.arabic
        .split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val translationParts = item.translation
        .split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val titles = listOf(
        "Аль-Ихляс · 112",
        "Аль-Фаляк · 113",
        "Ан-Нас · 114",
    )

    if (arabicParts.size != 3 || translationParts.size != 3) {
        BasicText(
            text = azkarArabicDisplayText(
                text = item.arabic,
                bodySizeSp = AzkarThemeValues.typography.arabicBody.fontSize.value,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = AzkarSpacing.arabicTop,
                    bottom = AzkarSpacing.arabicBottom,
                )
                .testTag(AzkarReadingTestTags.Arabic),
            style = AzkarThemeValues.typography.arabicBody.copy(color = colors.foreground),
        )
        if (showTranslation) {
            BasicText(
                text = item.translation,
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(colors.border, dashed = false)
                    .padding(top = AzkarSpacing.translationTop)
                    .testTag(AzkarReadingTestTags.Translation),
                style = AzkarThemeValues.typography.translation.copy(color = colors.foreground),
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = AzkarSpacing.arabicTop,
                bottom = AzkarSpacing.arabicBottom,
            ),
    ) {
        titles.indices.forEach { index ->
            val sectionHeaderModifier = if (index == 0) Modifier
                else Modifier.topRule(colors.border, dashed = false)
            BasicText(
                text = titles[index],
                modifier = sectionHeaderModifier
                    .fillMaxWidth()
                    .padding(
                        top = if (index == 0) 0.dp else 9.dp,
                        bottom = 3.dp,
                    ),
                style = AzkarThemeValues.typography.sourceRow.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            BasicText(
                text = azkarArabicDisplayText(
                    text = arabicParts[index],
                    bodySizeSp = AzkarThemeValues.typography.arabicBody.fontSize.value,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (showTranslation) 5.dp else 7.dp)
                    .testTag(
                        if (index == 0) AzkarReadingTestTags.Arabic
                        else "${AzkarReadingTestTags.Arabic}-${index + 1}",
                    ),
                style = AzkarThemeValues.typography.arabicBody.copy(color = colors.foreground),
            )
            if (showTranslation) {
                BasicText(
                    text = translationParts[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index == titles.lastIndex) 0.dp else 9.dp)
                        .testTag(
                            if (index == 0) AzkarReadingTestTags.Translation
                            else "${AzkarReadingTestTags.Translation}-${index + 1}",
                        ),
                    style = AzkarThemeValues.typography.translation.copy(
                        color = colors.foreground,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AzkarExplanationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.explainButton)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AzkarDimensions.explainButtonMinHeight)
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Открыть разъяснение" },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_explanation),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(colors.muted),
            )
            BasicText(
                text = "Разъяснение",
                style = AzkarThemeValues.typography.explainButton.copy(color = colors.foreground),
            )
        }
    }
}

@Composable
private fun AzkarCounterRow(
    state: AzkarGoldenReadingUiState,
    completed: Boolean,
    narrow: Boolean,
    onIncrementCount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (narrow) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AzkarSpacing.counterGap),
        ) {
            AzkarCounterText(state, completed)
            AzkarCountAction(state, completed, onIncrementCount, Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.counterGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AzkarCounterText(state, completed)
            }
            AzkarCountAction(state, completed, onIncrementCount)
        }
    }
}

@Composable
private fun AzkarCounterText(state: AzkarGoldenReadingUiState, completed: Boolean) {
    val colors = AzkarThemeValues.colors
    val countScale = remember { Animatable(1f) }
    LaunchedEffect(state.currentCount) {
        if (state.currentCount > 0) {
            countScale.snapTo(1.10f)
            countScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(AzkarMotion.toggleDurationMillis),
            )
        }
    }
    if (completed) {
        BasicText(
            text = "Выполнено",
            style = AzkarThemeValues.typography.counter.copy(
                color = colors.success,
                fontWeight = FontWeight.Bold,
            ),
        )
    } else {
        BasicText(
            modifier = Modifier.graphicsLayer {
                scaleX = countScale.value
                scaleY = countScale.value
            },
            text = buildAnnotatedString {
                pushStyle(
                    SpanStyle(
                        color = colors.foreground,
                        fontSize = AzkarThemeValues.typography.counterNumber.fontSize,
                    ),
                )
                append(state.currentCount.toString())
                pop()
                append(" / ${state.item.count}")
            },
            style = AzkarThemeValues.typography.counter.copy(color = colors.muted),
        )
    }
}

private fun azkarArabicDisplayText(
    text: String,
    bodySizeSp: Float,
): AnnotatedString = buildAnnotatedString {
    val compactText = text
        .replace(Regex("[\\t ]*\\n+[\\t ]*"), "  ")
        .replace(Regex(" {3,}"), "  ")
    compactText.forEach { character ->
        when (character) {
            '،' -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = AzkarFontFamilies.NotoNaskhArabic,
                        fontSize = (bodySizeSp * 1.24f).coerceAtLeast(20f).sp,
                    ),
                )
                append(character)
                pop()
            }
            '۝' -> {
                pushStyle(
                    SpanStyle(
                        fontSize = (bodySizeSp * 0.055f).coerceIn(2.2f, 2.8f).sp,
                    ),
                )
                append(character)
                pop()
            }
            else -> append(character)
        }
    }
}

@Composable
private fun AzkarCountAction(
    state: AzkarGoldenReadingUiState,
    completed: Boolean,
    onIncrementCount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when {
        completed -> "✓ Готово"
        state.item.count == 1 -> "Прочитано"
        else -> "+1 · осталось ${state.item.count - state.currentCount}"
    }
    AzkarPrimaryButton(
        text = label,
        onClick = onIncrementCount,
        modifier = modifier.testTag(AzkarReadingTestTags.CountActionPrefix + state.item.id),
        enabled = !completed,
    )
}

private fun Modifier.topRule(color: Color, dashed: Boolean): Modifier = drawBehind {
    val stroke = AzkarBorders.thin.toPx()
    val y = stroke / 2f
    val effect = if (dashed) {
        val segment = stroke * AzkarBorders.dashedSegmentMultiplier
        PathEffect.dashPathEffect(floatArrayOf(segment, segment))
    } else {
        null
    }
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(0f, y),
        end = androidx.compose.ui.geometry.Offset(size.width, y),
        strokeWidth = stroke,
        pathEffect = effect,
    )
}
