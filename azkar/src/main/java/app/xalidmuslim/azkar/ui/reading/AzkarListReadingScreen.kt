package app.xalidmuslim.azkar.ui.reading

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.xalidmuslim.azkar.ui.designsystem.AzkarDimensions
import app.xalidmuslim.azkar.ui.designsystem.AzkarSpacing
import app.xalidmuslim.azkar.ui.designsystem.AzkarSurface

internal const val AzkarListCardStartIndex = 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AzkarListReadingScreen(
    entries: List<AzkarReaderEntry>,
    period: AzkarPeriod,
    activeIndex: Int,
    listState: LazyListState,
    settings: AzkarReaderSettings,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onOpenContents: () -> Unit,
    onViewModeChange: (AzkarReaderViewMode) -> Unit,
    onOpenSourceInfo: () -> Unit,
    onIncrementCount: (String, Int) -> Unit,
    onResetProgress: () -> Unit,
    onPeriodChange: (AzkarPeriod) -> Unit = {},
    hideCompleted: Boolean = false,
    onOpenActions: (Int, String) -> Unit,
    onOpenExplanation: (Int, String) -> Unit,
) {
    val selected = entries[activeIndex.coerceIn(entries.indices)]
    val completedItems = countCompletedItems(entries)
    val visibleEntries = entries.filterIndexed { index, entry ->
        !hideCompleted || entry.currentCount < entry.item.count || index == activeIndex
    }
    val summaryState = AzkarGoldenReadingUiState(
        item = selected.item,
        period = period,
        position = activeIndex + 1,
        total = entries.size,
        completedItems = completedItems,
        currentCount = selected.currentCount,
    )

    AzkarSurface(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val narrow = maxWidth <= AzkarDimensions.responsiveBreakpoint
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .widthIn(max = AzkarDimensions.shellMaxWidth)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    )
                    .padding(horizontal = AzkarSpacing.shellHorizontal),
            ) {
                // Keep the brand/header stationary in list mode too.
                AzkarHeader(
                    onOpenSettings = onOpenSettings,
                    onOpenSearch = onOpenSearch,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(bottom = AzkarSpacing.shellBottomBase)
                        .testTag(AzkarReadingTestTags.List),
                ) {
                    item(key = "reader-header") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AzkarSourceNote(onOpenSourceInfo)
                            AzkarPeriodTabs(period, onPeriodChange)
                            AzkarProgressCard(summaryState, onResetProgress)
                        }
                    }

                    stickyHeader(key = "reader-toolbar") {
                    AzkarReaderToolbar(
                        state = summaryState,
                        narrow = narrow,
                        onOpenContents = onOpenContents,
                        viewMode = AzkarReaderViewMode.List,
                        onViewModeChange = onViewModeChange,
                        positionText = "${summaryState.position} из ${summaryState.total}",
                    )
                }

                itemsIndexed(
                    items = visibleEntries,
                    key = { _, entry -> entry.item.id },
                ) { index, entry ->
                    val originalIndex = entries.indexOfFirst { it.item.id == entry.item.id }
                    val state = AzkarGoldenReadingUiState(
                        item = entry.item,
                        period = period,
                        position = originalIndex + 1,
                        total = entries.size,
                        completedItems = completedItems,
                        currentCount = entry.currentCount,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(AzkarReadingTestTags.ListCardPrefix + entry.item.id),
                    ) {
                        AzkarDhikrCard(
                            state = state,
                            narrow = narrow,
                            scrollState = null,
                            maxHeight = null,
                            interactionModifier = Modifier,
                            compactReader = settings.readerStyle == AzkarReaderStyle.Compact,
                            showTranslation = settings.showTranslation,
                            showTransliteration = settings.showTransliteration,
                            transliterationSizeSp = settings.transliterationSizeSp,
                            showSources = settings.showSources,
                            showNotes = settings.showNotes,
                            onOpenExplanation = {
                                onOpenExplanation(originalIndex, entry.item.id)
                            },
                            onOpenActions = {
                                onOpenActions(originalIndex, entry.item.id)
                            },
                            onIncrementCount = {
                                onIncrementCount(entry.item.id, entry.item.count)
                            },
                        )
                    }
                    if (index != visibleEntries.lastIndex) {
                        Spacer(modifier = Modifier.height(AzkarSpacing.pagerTop))
                    }
                    }
                }
            }
        }
    }
}
