package app.alfatiha.tafsir.azkar.ui.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarBorders
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarCardSurface
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarDimensions
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarElevation
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarIconButton
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarIconButtonSize
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarPrimaryButton
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarProgressBar
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarRadius
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarSpacing
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarSurface
import app.alfatiha.tafsir.azkar.ui.designsystem.AzkarThemeValues
import app.alfatiha.tafsir.azkar.ui.designsystem.azkarShadow
import kotlin.math.roundToInt

internal object AzkarReadingTestTags {
    const val Screen = "azkar-reading-screen"
    const val Header = "azkar-header"
    const val SourceNote = "azkar-source-note"
    const val PeriodTabs = "azkar-period-tabs"
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
    const val Pager = "azkar-pager"
    const val Footer = "azkar-footer"
}

private const val SourceNoteText =
    "Основа списка — Абдуль-Азиз ат-Тарифи, «Утренние и вечерние азкары: передача и исследование». Спорные оценки отмечены отдельно."

private const val FooterText =
    "Русский текст — смысловой перевод. Разногласия и дополнительные оценки вынесены в примечания и разъяснения."

@Composable
fun AzkarGoldenReadingScreen(
    state: AzkarGoldenReadingUiState = AzkarGoldenReadingFixtures.GoldenMorning,
    modifier: Modifier = Modifier,
) {
    AzkarSurface(modifier = modifier.fillMaxWidth().testTag(AzkarReadingTestTags.Screen)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val narrow = maxWidth <= AzkarDimensions.responsiveBreakpoint
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
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
                AzkarHeader()
                AzkarSourceNote()
                AzkarPeriodTabs(state.period)
                AzkarProgressCard(state)
                AzkarReaderToolbar(state, narrow)
                AzkarDhikrCard(state, narrow)
                AzkarPager(state, narrow)
                AzkarFooter()
            }
        }
    }
}

@Composable
private fun AzkarHeader() {
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
                    .background(colors.accent)
                    .border(AzkarBorders.thin, colors.border, brandShape),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "✦",
                    style = AzkarThemeValues.typography.brandTitle.copy(
                        color = colors.primary,
                        fontSize = AzkarDimensions.brandIconGlyphSp.sp,
                    ),
                )
            }
            Column {
                BasicText(
                    text = "Азкар",
                    style = AzkarThemeValues.typography.brandTitle.copy(color = colors.foreground),
                )
                BasicText(
                    text = "УТРО · ВЕЧЕР",
                    modifier = Modifier.padding(top = AzkarSpacing.brandSubtitleTop),
                    style = AzkarThemeValues.typography.brandSubtitle.copy(color = colors.muted),
                )
            }
        }
        AzkarIconButton(onClick = {}) {
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

@Composable
private fun AzkarSourceNote() {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.sourceNote)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(
                horizontal = AzkarSpacing.sourceNoteHorizontal,
                vertical = AzkarSpacing.sourceNoteVertical,
            )
            .testTag(AzkarReadingTestTags.SourceNote),
    ) {
        BasicText(
            text = SourceNoteText,
            style = AzkarThemeValues.typography.sourceNote.copy(color = colors.muted),
        )
    }
}

@Composable
private fun AzkarPeriodTabs(period: AzkarPeriod) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.periodTabs)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AzkarSpacing.periodTop, bottom = AzkarSpacing.periodBottom)
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(AzkarSpacing.periodInternal)
            .testTag(AzkarReadingTestTags.PeriodTabs),
        horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.periodGap),
    ) {
        AzkarPeriodButton(
            text = "☀ Утро",
            active = period == AzkarPeriod.Morning,
            modifier = Modifier.weight(1f),
        )
        AzkarPeriodButton(
            text = "☾ Вечер",
            active = period == AzkarPeriod.Evening,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AzkarPeriodButton(text: String, active: Boolean, modifier: Modifier) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.periodButton)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = AzkarDimensions.periodButtonMinHeight)
            .then(
                if (active) Modifier.azkarShadow(AzkarElevation.ActivePeriod, AzkarRadius.periodButton)
                else Modifier,
            )
            .clip(shape)
            .background(if (active) colors.card else Color.Transparent),
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
private fun AzkarProgressCard(state: AzkarGoldenReadingUiState) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.progressCard)
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
                text = "↻ Сбросить",
                style = AzkarThemeValues.typography.resetTextButton.copy(color = colors.muted),
            )
        }
        AzkarProgressBar(progress = roundedPercent / 100f, animate = false)
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
private fun AzkarReaderToolbar(state: AzkarGoldenReadingUiState, narrow: Boolean) {
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
        AzkarToolbarButton(if (narrow) "☷" else "☷ Содержание")
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            BasicText(
                text = "${state.position} из ${state.total}",
                style = AzkarThemeValues.typography.toolbarPosition.copy(color = colors.muted),
            )
        }
        AzkarIconButton(onClick = {}, size = AzkarIconButtonSize.Compact) {
            BasicText(
                text = "⚙",
                style = AzkarThemeValues.typography.translation.copy(
                    color = colors.foreground,
                    fontSize = AzkarDimensions.compactIconGlyphSp.sp,
                ),
            )
        }
    }
}

@Composable
private fun AzkarToolbarButton(text: String) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.toolbarButton)
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = AzkarDimensions.toolbarButtonMinHeight)
            .clip(shape)
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
private fun AzkarDhikrCard(state: AzkarGoldenReadingUiState, narrow: Boolean) {
    val item = state.item
    val colors = AzkarThemeValues.colors
    val completed = state.currentCount >= item.count

    AzkarCardSurface(
        modifier = Modifier.fillMaxWidth().testTag(AzkarReadingTestTags.Card),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    if (item.disputed) {
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
            }

            BasicText(
                text = item.arabic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = AzkarSpacing.arabicTop,
                        bottom = AzkarSpacing.arabicBottom,
                    )
                    .testTag(AzkarReadingTestTags.Arabic),
                style = AzkarThemeValues.typography.arabicBody.copy(color = colors.foreground),
            )

            BasicText(
                text = item.translation,
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(colors.border, dashed = false)
                    .padding(top = AzkarSpacing.translationTop)
                    .testTag(AzkarReadingTestTags.Translation),
                style = AzkarThemeValues.typography.translation.copy(color = colors.foreground),
            )

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

            item.note?.let { note ->
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
                    modifier = Modifier
                        .padding(top = AzkarSpacing.explainTop)
                        .testTag(AzkarReadingTestTags.Explain),
                )
            }

            AzkarCounterRow(
                state = state,
                completed = completed,
                narrow = narrow,
                modifier = Modifier
                    .padding(top = AzkarSpacing.counterTop)
                    .testTag(AzkarReadingTestTags.Counter),
            )
        }
    }
}

@Composable
private fun AzkarExplanationButton(modifier: Modifier = Modifier) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.explainButton)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AzkarDimensions.explainButtonMinHeight)
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = "▣ Разъяснение · история · слова учёных",
            style = AzkarThemeValues.typography.explainButton.copy(color = colors.foreground),
        )
    }
}

@Composable
private fun AzkarCounterRow(
    state: AzkarGoldenReadingUiState,
    completed: Boolean,
    narrow: Boolean,
    modifier: Modifier = Modifier,
) {
    if (narrow) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AzkarSpacing.counterGap),
        ) {
            AzkarCounterText(state, completed)
            AzkarCountAction(state, completed, Modifier.fillMaxWidth())
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
            AzkarCountAction(state, completed)
        }
    }
}

@Composable
private fun AzkarCounterText(state: AzkarGoldenReadingUiState, completed: Boolean) {
    val colors = AzkarThemeValues.colors
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

@Composable
private fun AzkarCountAction(
    state: AzkarGoldenReadingUiState,
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    val label = when {
        completed -> "✓ Готово"
        state.item.count == 1 -> "Прочитано"
        else -> "+1 · осталось ${state.item.count - state.currentCount}"
    }
    AzkarPrimaryButton(
        text = label,
        onClick = {},
        modifier = modifier,
        enabled = !completed,
    )
}

@Composable
private fun AzkarPager(state: AzkarGoldenReadingUiState, narrow: Boolean) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.pager)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AzkarSpacing.pagerTop)
            .clip(shape)
            .background(colors.card)
            .border(AzkarBorders.thin, colors.border, shape)
            .padding(AzkarSpacing.pagerPadding)
            .testTag(AzkarReadingTestTags.Pager),
        horizontalArrangement = Arrangement.spacedBy(AzkarSpacing.pagerGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AzkarPagerButton(
            text = "‹ Назад",
            primary = false,
            enabled = state.position > 1,
            modifier = Modifier.weight(1f),
        )

        Column(
            modifier = Modifier.widthIn(min = AzkarDimensions.pagerCenterMinWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    AzkarSpacing.pagerCenterHorizontalGap,
                ),
                verticalAlignment = Alignment.Bottom,
            ) {
                BasicText(
                    text = state.position.toString(),
                    style = AzkarThemeValues.typography.pagerActiveNumber.copy(
                        color = colors.foreground,
                    ),
                )
                BasicText(
                    text = "из ${state.total}",
                    style = AzkarThemeValues.typography.pagerCenter.copy(color = colors.muted),
                )
            }
            if (!narrow) {
                BasicText(
                    text = "свайп влево/вправо",
                    modifier = Modifier.padding(top = AzkarSpacing.pagerHelperTop),
                    style = AzkarThemeValues.typography.pagerHelper.copy(color = colors.muted),
                )
            }
        }

        AzkarPagerButton(
            text = "Далее ›",
            primary = true,
            enabled = state.position < state.total,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AzkarPagerButton(
    text: String,
    primary: Boolean,
    enabled: Boolean,
    modifier: Modifier,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.pagerButton)
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else AzkarDimensions.disabledControlAlpha)
            .defaultMinSize(minHeight = AzkarDimensions.pagerButtonMinHeight)
            .clip(shape)
            .background(if (primary) colors.primary else colors.card)
            .then(
                if (primary) Modifier
                else Modifier.border(AzkarBorders.thin, colors.border, shape),
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.pagerButton.copy(
                color = if (primary) colors.primaryActionText else colors.foreground,
            ),
        )
    }
}

@Composable
private fun AzkarFooter() {
    val colors = AzkarThemeValues.colors
    BasicText(
        text = FooterText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = AzkarSpacing.footerHorizontal,
                end = AzkarSpacing.footerHorizontal,
                top = AzkarSpacing.footerTop,
                bottom = AzkarSpacing.footerBottom,
            )
            .testTag(AzkarReadingTestTags.Footer),
        style = AzkarThemeValues.typography.footer.copy(
            color = colors.muted,
            textAlign = TextAlign.Center,
        ),
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
