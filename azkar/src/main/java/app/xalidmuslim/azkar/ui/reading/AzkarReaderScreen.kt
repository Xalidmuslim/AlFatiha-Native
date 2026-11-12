package app.xalidmuslim.azkar.ui.reading

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.xalidmuslim.azkar.R
import app.xalidmuslim.azkar.ui.designsystem.AzkarDimensions
import app.xalidmuslim.azkar.ui.designsystem.AzkarMotion
import app.xalidmuslim.azkar.persistence.AzkarDateProvider
import app.xalidmuslim.azkar.persistence.AzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.SystemAzkarDateProvider
import app.xalidmuslim.azkar.ui.designsystem.AzkarSurface
import app.xalidmuslim.azkar.ui.designsystem.AzkarTheme
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeMode
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeValues
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AzkarReaderEntry(
    val item: AzkarReadingItem,
    val currentCount: Int = 0,
)

@Composable
fun AzkarReaderScreen(
    entries: List<AzkarReaderEntry>,
    period: AzkarPeriod,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    onPeriodChange: (AzkarPeriod) -> Unit = {},
) {
    val navigationController = remember(entries.size, initialIndex) {
        AzkarReaderNavigationController(entries.size, initialIndex)
    }
    val uiController = remember { AzkarReaderUiController() }
    AzkarReaderScreen(
        entries = entries,
        period = period,
        controller = navigationController,
        uiController = uiController,
        modifier = modifier,
        onPeriodChange = onPeriodChange,
    )
}

@Composable
fun AzkarReaderScreen(
    entries: List<AzkarReaderEntry>,
    period: AzkarPeriod,
    preferencesRepository: AzkarPreferencesRepository,
    dateProvider: AzkarDateProvider = SystemAzkarDateProvider,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    onPeriodChange: (AzkarPeriod) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val visibleItemIds = remember(entries) { entries.map { it.item.id }.toSet() }
    val navigationController = remember(entries.size, initialIndex) {
        AzkarReaderNavigationController(entries.size, initialIndex)
    }
    val uiController = remember(preferencesRepository, dateProvider, visibleItemIds) {
        AzkarReaderUiController(
            repository = preferencesRepository,
            dateProvider = dateProvider,
            persistenceScope = scope,
            visibleItemIds = visibleItemIds,
        )
    }
    AzkarReaderScreen(
        entries = entries,
        period = period,
        controller = navigationController,
        uiController = uiController,
        modifier = modifier,
        onPeriodChange = onPeriodChange,
    )
}

@Composable
fun AzkarReaderScreen(
    entries: List<AzkarReaderEntry>,
    period: AzkarPeriod,
    controller: AzkarReaderNavigationController,
    modifier: Modifier = Modifier,
    uiController: AzkarReaderUiController? = null,
    onPeriodChange: (AzkarPeriod) -> Unit = {},
) {
    require(entries.isNotEmpty()) { "Reader requires at least one entry" }

    val context = LocalContext.current
    val resolvedUiController = uiController ?: remember { AzkarReaderUiController() }
    val navigation = controller.state
    val readerUi = resolvedUiController.state
    // Render the real reader immediately. Persistence hydration updates the same
    // state tree in place instead of showing a separate splash and replacing it.
    val settings = readerUi.settings
    val globalUiPreferences = remember(context) {
        context.getSharedPreferences(
            "alfatiha_native",
            android.content.Context.MODE_PRIVATE,
        )
    }
    var sharedDark by remember(globalUiPreferences) {
        mutableStateOf(globalUiPreferences.getBoolean("dark", false))
    }

    // Keep the Azkar module attached to the same theme switch as the host app
    // even when this Activity stays alive while another section changes theme.
    DisposableEffect(globalUiPreferences) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == "dark") {
                    sharedDark = prefs.getBoolean("dark", false)
                }
            }
        globalUiPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            globalUiPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(readerUi.isHydrated, sharedDark) {
        if (readerUi.isHydrated) {
            val sharedMode =
                if (sharedDark) AzkarThemeMode.Dark else AzkarThemeMode.Light
            if (settings.themeMode != sharedMode) {
                resolvedUiController.updateSettings {
                    it.copy(themeMode = sharedMode)
                }
            }
        }
    }

    val systemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (settings.themeMode) {
        AzkarThemeMode.Dark -> true
        AzkarThemeMode.Light -> false
        AzkarThemeMode.System -> systemDarkTheme
    }
    val toggleTheme: () -> Unit = {
        val nextDark = !sharedDark
        sharedDark = nextDark
        globalUiPreferences.edit().putBoolean("dark", nextDark).apply()
        resolvedUiController.updateSettings {
            it.copy(
                themeMode =
                    if (nextDark) AzkarThemeMode.Dark else AzkarThemeMode.Light,
            )
        }
    }

    val resolvedEntries = remember(entries, readerUi.progressById) {
        entries.map { entry ->
            entry.copy(
                currentCount = readerUi.progressById[entry.item.id]
                    ?.coerceIn(0, entry.item.count)
                    ?: entry.currentCount.coerceIn(0, entry.item.count),
            )
        }
    }
    val activeIndex = navigation.activeIndex.coerceIn(resolvedEntries.indices)
    val active = resolvedEntries[activeIndex]

    var restoredLastItem by remember(period, controller) { mutableStateOf(false) }
    LaunchedEffect(readerUi.hasLiveSnapshot, readerUi.lastPeriod, period, readerUi.lastItemByPeriod) {
        if (readerUi.hasLiveSnapshot && readerUi.lastPeriod == period && !restoredLastItem) {
            val savedId = readerUi.lastItemByPeriod[period]
            val savedIndex = resolvedEntries.indexOfFirst { it.item.id == savedId }
            if (savedIndex >= 0 && savedIndex != controller.state.activeIndex) {
                controller.selectAnchor(savedIndex)
            }
            restoredLastItem = true
        }
    }
    LaunchedEffect(restoredLastItem, period, navigation.activeIndex) {
        if (restoredLastItem && readerUi.hasLiveSnapshot && readerUi.lastPeriod == period) {
            resolvedEntries.getOrNull(navigation.activeIndex)?.let { entry ->
                resolvedUiController.saveLastItem(period, entry.item.id)
            }
        }
    }

    val previousTarget = if (settings.hideCompleted) {
        (activeIndex - 1 downTo 0).firstOrNull { index ->
            resolvedEntries[index].currentCount < resolvedEntries[index].item.count
        }
    } else {
        (activeIndex - 1).takeIf { it >= 0 }
    }
    val nextTarget = if (settings.hideCompleted) {
        (activeIndex + 1 until resolvedEntries.size).firstOrNull { index ->
            resolvedEntries[index].currentCount < resolvedEntries[index].item.count
        }
    } else {
        (activeIndex + 1).takeIf { it < resolvedEntries.size }
    }

    LaunchedEffect(
        settings.hideCompleted,
        active.item.id,
        active.currentCount,
        readerUi.activeSheet,
    ) {
        if (
            settings.hideCompleted &&
            active.currentCount >= active.item.count &&
            readerUi.activeSheet == AzkarReaderSheet.None
        ) {
            delay(600)
            (nextTarget ?: previousTarget)?.let(controller::navigateTo)
        }
    }

    val listVisibleIndices = resolvedEntries.indices.filter { index ->
        !settings.hideCompleted ||
            resolvedEntries[index].currentCount < resolvedEntries[index].item.count ||
            index == activeIndex
    }
    val activeListIndex = listVisibleIndices.indexOf(activeIndex).coerceAtLeast(0)

    val shellScrollState = rememberScrollState()
    val readingScrollState = remember(navigation.generation, readerUi.viewMode) { ScrollState(0) }
    val listState = rememberLazyListState()
    val positionPrefs = remember(context) {
        context.getSharedPreferences("azkar_reader_position", android.content.Context.MODE_PRIVATE)
    }
    var restoredFirstCardScroll by remember(period, controller) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val listScrollMarginPx = with(density) {
        AzkarDimensions.dhikrScrollMarginTop.roundToPx()
    }

    LaunchedEffect(readingScrollState) {
        snapshotFlow { readingScrollState.value }.collect(controller::recordScrollY)
    }

    // Exact in-card scroll offset is stored separately from the active item:
    // keep the existing DataStore IDs/progress untouched. The position is
    // restored only at entry, not during normal next/previous paging.
    LaunchedEffect(readerUi.viewMode) {
        if (readerUi.viewMode == AzkarReaderViewMode.List) restoredFirstCardScroll = false
    }
    LaunchedEffect(
        readerUi.viewMode, readerUi.hasLiveSnapshot, restoredLastItem,
        active.item.id, readingScrollState,
    ) {
        if (readerUi.viewMode == AzkarReaderViewMode.Cards &&
            readerUi.hasLiveSnapshot && restoredLastItem && !restoredFirstCardScroll
        ) {
            withFrameNanos { }
            val savedY = positionPrefs.getInt(
                "card_${period.name}_${active.item.id}", 0,
            ).coerceAtLeast(0)
            readingScrollState.scrollTo(savedY)
            restoredFirstCardScroll = true
        }
    }
    LaunchedEffect(readingScrollState, readerUi.viewMode, active.item.id, restoredFirstCardScroll) {
        if (readerUi.viewMode == AzkarReaderViewMode.Cards && restoredFirstCardScroll) {
            snapshotFlow { readingScrollState.value }.collectLatest { scrollY ->
                delay(180)
                positionPrefs.edit().putInt(
                    "card_${period.name}_${active.item.id}", scrollY,
                ).apply()
            }
        }
    }
    DisposableEffect(readingScrollState, active.item.id, readerUi.viewMode, restoredFirstCardScroll) {
        onDispose {
            if (readerUi.viewMode == AzkarReaderViewMode.Cards && restoredFirstCardScroll) {
                positionPrefs.edit().putInt(
                    "card_${period.name}_${active.item.id}", readingScrollState.value,
                ).apply()
            }
        }
    }

    // Restore the list only after the real DataStore snapshot has supplied the
    // active item. Do not animate the initial scroll: it should open in place.
    var listPositionReady by remember(period) { mutableStateOf(false) }
    var programmaticListScroll by remember(period) { mutableStateOf(false) }
    LaunchedEffect(readerUi.viewMode, restoredLastItem, period) {
        listPositionReady = false
        if (readerUi.viewMode == AzkarReaderViewMode.List &&
            restoredLastItem && readerUi.hasLiveSnapshot
        ) {
            val oldListIndex = positionPrefs.getInt("list_index_${period.name}", -1)
            val destination = if (oldListIndex in 0 until AzkarListCardStartIndex) {
                oldListIndex
            } else {
                AzkarListCardStartIndex + activeListIndex
            }
            listState.scrollToItem(
                index = destination,
                scrollOffset = positionPrefs.getInt("list_offset_${period.name}", 0)
                    .coerceAtLeast(0),
            )
            listPositionReady = true
        }
    }

    // In list mode the reader's active index did not change as the user scrolled.
    // Persist the first visible card (not the header), so opening Azkar again
    // returns to the item actually being read.
    LaunchedEffect(readerUi.viewMode, listPositionReady, period, settings.hideCompleted) {
        if (readerUi.viewMode == AzkarReaderViewMode.List && listPositionReady) {
            snapshotFlow { listState.firstVisibleItemIndex }.collect { firstVisible ->
                if (!programmaticListScroll && firstVisible >= AzkarListCardStartIndex) {
                    readerIndexForVisibleListPosition(
                        firstVisible,
                        listVisibleIndices,
                    )?.let { itemIndex ->
                        controller.selectAnchor(itemIndex)
                    }
                }
            }
        }
    }
    // Save pixel offset only after a brief idle period, not on every scroll
    // frame. A dispose flush also keeps the final position on Activity exit.
    LaunchedEffect(listState, readerUi.viewMode, listPositionReady, period) {
        if (readerUi.viewMode == AzkarReaderViewMode.List && listPositionReady) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collectLatest { (index, offset) ->
                delay(160)
                if (!programmaticListScroll) {
                    positionPrefs.edit()
                        .putInt("list_index_${period.name}", index)
                        .putInt("list_offset_${period.name}", offset)
                        .apply()
                }
            }
        }
    }
    DisposableEffect(listState, readerUi.viewMode, listPositionReady, period) {
        onDispose {
            if (readerUi.viewMode == AzkarReaderViewMode.List && listPositionReady) {
                positionPrefs.edit()
                    .putInt("list_index_${period.name}", listState.firstVisibleItemIndex)
                    .putInt("list_offset_${period.name}", listState.firstVisibleItemScrollOffset)
                    .apply()
            }
        }
    }

    BackHandler(enabled = readerUi.activeSheet != AzkarReaderSheet.None) {
        resolvedUiController.closeSheet()
    }
    BackHandler(enabled = readerUi.activeSheet == AzkarReaderSheet.None) {
        navigateToHeartPrayer(context, "home")
    }

    val rootView = LocalView.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }
    var pageDirection by remember { mutableStateOf(1) }
    val pageSlidePx = with(density) { 18.dp.toPx() }
    val initialOffset = if (navigation.generation > 0L && animationsEnabled) {
        pageSlidePx * pageDirection
    } else {
        0f
    }
    val initialAlpha = if (navigation.generation > 0L && animationsEnabled) {
        0.94f
    } else {
        1f
    }
    val transitionOffset = remember(navigation.generation) { Animatable(initialOffset) }
    val transitionAlpha = remember(navigation.generation) { Animatable(initialAlpha) }
    val transitionScale = remember(navigation.generation) {
        Animatable(if (navigation.generation > 0L && animationsEnabled) 0.992f else 1f)
    }

    LaunchedEffect(navigation.generation, animationsEnabled) {
        if (!animationsEnabled || navigation.generation == 0L) {
            transitionOffset.snapTo(0f)
            transitionAlpha.snapTo(1f)
            transitionScale.snapTo(1f)
        } else {
            coroutineScope {
                launch {
                    transitionOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = AzkarMotion.dhikrPageDurationMillis,
                            easing = AzkarMotion.dhikrPageEasing,
                        ),
                    )
                }
                launch {
                    transitionAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = AzkarMotion.dhikrPageDurationMillis,
                            easing = AzkarMotion.dhikrPageEasing,
                        ),
                    )
                }
                launch {
                    transitionScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = AzkarMotion.dhikrPageDurationMillis,
                            easing = AzkarMotion.dhikrPageEasing,
                        ),
                    )
                }
            }
        }
    }

    val completedItems = countCompletedItems(resolvedEntries)
    val uiState = AzkarGoldenReadingUiState(
        item = active.item,
        period = period,
        position = activeIndex + 1,
        total = entries.size,
        completedItems = completedItems,
        currentCount = active.currentCount,
    )

    val previous: () -> Unit = {
        if (
            readerUi.viewMode == AzkarReaderViewMode.Cards &&
            readerUi.activeSheet == AzkarReaderSheet.None
        ) {
            previousTarget?.let {
                pageDirection = -1
                controller.navigateTo(it)
            }
        }
        Unit
    }
    val next: () -> Unit = {
        if (
            readerUi.viewMode == AzkarReaderViewMode.Cards &&
            readerUi.activeSheet == AzkarReaderSheet.None
        ) {
            nextTarget?.let {
                pageDirection = 1
                controller.navigateTo(it)
            }
        }
        Unit
    }
    val pagingModifier = Modifier.azkarHorizontalPaging(
        enabled = readerUi.allowsHorizontalPaging(resolvedEntries.size),
        onPrevious = previous,
        onNext = next,
    )
    val transitionModifier = Modifier.graphicsLayer {
        translationX = transitionOffset.value
        alpha = transitionAlpha.value
        scaleX = transitionScale.value
        scaleY = transitionScale.value
    }

    AzkarTheme(
        themeMode = settings.themeMode,
        russianFontFamily = settings.russianFontFamily,
        arabicFontFamily = settings.arabicFontFamily,
        arabicSizeSp = settings.arabicSizeSp,
        russianSizeSp = settings.russianSizeSp,
        readerLineHeight = settings.lineHeight,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AzkarThemeValues.colors.background)
                .then(
                    if (readerUi.viewMode == AzkarReaderViewMode.Cards) pagingModifier
                    else Modifier,
                ),
        ) {
            // Do not reserve either screen edge for Azkar paging.
            // Horizontal paging still works from the content area via draggable(),
            // while Android always owns the edge swipe so Back reacts immediately
            // in cards, settings, contents, explanations, and other sheets.

            when (readerUi.viewMode) {
                AzkarReaderViewMode.Cards -> {
                    AzkarGoldenReadingScreen(
                        state = uiState,
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                            )
                            .padding(bottom = 72.dp),
                        onPrevious = previous,
                        onNext = next,
                        shellScrollState = shellScrollState,
                        readingScrollState = readingScrollState,
                        readingAreaModifier = transitionModifier,
                        onOpenSettings = resolvedUiController::openSettings,
                        onOpenSearch = { navigateToHeartPrayer(context, "search") },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = toggleTheme,
                        onOpenContents = resolvedUiController::openContents,
                        viewMode = readerUi.viewMode,
                        onViewModeChange = { mode -> resolvedUiController.setViewMode(mode) },
                        onOpenSourceInfo = resolvedUiController::openSourceInfo,
                        onOpenExplanation = resolvedUiController::openExplanation,
                        onOpenActions = resolvedUiController::openActions,
                        onIncrementCount = { itemId, target ->
                            if (resolvedUiController.incrementProgress(itemId, target)) {
                                rootView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        },
                        onResetProgress = {
                            resolvedUiController.resetProgress(resolvedEntries.map { it.item.id })
                        },
                        onPeriodChange = onPeriodChange,
                        compactReader = settings.readerStyle == AzkarReaderStyle.Compact,
                        showTranslation = settings.showTranslation,
                        showTransliteration = settings.showTransliteration,
                        transliterationSizeSp = settings.transliterationSizeSp,
                        showSources = settings.showSources,
                        showNotes = settings.showNotes,
                        canPrevious = previousTarget != null,
                        canNext = nextTarget != null,
                    )
                }

                AzkarReaderViewMode.List -> {
                    AzkarListReadingScreen(
                        entries = resolvedEntries,
                        period = period,
                        activeIndex = activeIndex,
                        listState = listState,
                        settings = settings,
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                            )
                            .padding(bottom = 72.dp),
                        onOpenSettings = resolvedUiController::openSettings,
                        onOpenSearch = { navigateToHeartPrayer(context, "search") },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = toggleTheme,
                        onOpenContents = resolvedUiController::openContents,
                        onViewModeChange = { mode -> resolvedUiController.setViewMode(mode) },
                        onOpenSourceInfo = resolvedUiController::openSourceInfo,
                        onIncrementCount = { itemId, target ->
                            if (resolvedUiController.incrementProgress(itemId, target)) {
                                rootView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        },
                        onResetProgress = {
                            resolvedUiController.resetProgress(resolvedEntries.map { it.item.id })
                        },
                        onPeriodChange = onPeriodChange,
                        hideCompleted = settings.hideCompleted,
                        onOpenActions = { index, itemId ->
                            controller.selectAnchor(index)
                            resolvedUiController.openActions(itemId)
                        },
                        onOpenExplanation = { index, itemId ->
                            controller.selectAnchor(index)
                            resolvedUiController.openExplanation(itemId)
                        },
                    )
                }
            }

            HeartPrayerBottomNav(
                modifier = Modifier.align(Alignment.BottomCenter),
                contentsSelected = readerUi.activeSheet == AzkarReaderSheet.Contents,
                onHome = {
                    navigateToHeartPrayer(context, "home")
                },
                onContents = {
                    resolvedUiController.openContents()
                },
                onProgress = {
                    navigateToHeartPrayer(context, "progress")
                },
                onMenu = {
                    navigateToHeartPrayer(context, "menu")
                },
            )

            AzkarReaderSheetHost(
                activeSheet = readerUi.activeSheet,
                entries = resolvedEntries,
                activeIndex = activeIndex,
                selectedExplanationId = readerUi.selectedExplanationId,
                selectedActionId = readerUi.selectedActionId,
                settings = settings,
                viewMode = readerUi.viewMode,
                onDismiss = { resolvedUiController.closeSheet() },
                onResetSettings = {
                    resolvedUiController.updateSettings { AzkarReaderSettings() }
                },
                onDecrementProgress = { itemId, target ->
                    resolvedUiController.decrementProgress(itemId, target)
                },
                onResetItemProgress = { itemId ->
                    resolvedUiController.resetSingleProgress(itemId)
                },
                onResetPeriodProgress = {
                    resolvedUiController.resetProgress(resolvedEntries.map { it.item.id })
                },
                onSelectContents = { index ->
                    when (readerUi.viewMode) {
                        AzkarReaderViewMode.Cards -> {
                            if (index == controller.state.activeIndex) {
                                controller.reopenCurrentAtTop()
                            } else {
                                controller.navigateTo(index)
                            }
                            resolvedUiController.closeSheet()
                        }

                        AzkarReaderViewMode.List -> {
                            programmaticListScroll = true
                            controller.selectAnchor(index)
                            resolvedUiController.closeSheet()
                            scope.launch {
                                try {
                                    listState.animateScrollToItem(
                                        index = AzkarListCardStartIndex +
                                            listVisibleIndices.indexOf(index).coerceAtLeast(0),
                                        scrollOffset = -listScrollMarginPx,
                                    )
                                } finally {
                                    programmaticListScroll = false
                                }
                            }
                        }
                    }
                },
                onViewModeChange = { mode ->
                    resolvedUiController.setViewMode(mode)
                    resolvedUiController.closeSheet()
                },
                onUpdateSettings = resolvedUiController::updateSettings,
            )
        }
    }
}

private fun navigateToHeartPrayer(context: android.content.Context, destination: String) {
    val intent = Intent()
        .setClassName(context.packageName, "app.alfatiha.tafsir.MainActivity")
        .putExtra("heart_nav", destination)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    context.startActivity(intent)
    (context as? Activity)?.let { activity ->
        activity.finish()
        if (Build.VERSION.SDK_INT < 34) {
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(
                R.anim.azkar_return_enter,
                R.anim.azkar_return_exit,
            )
        }
    }
}

@Composable
private fun HeartPrayerBottomNav(
    modifier: Modifier = Modifier,
    contentsSelected: Boolean = false,
    onHome: () -> Unit,
    onContents: () -> Unit,
    onProgress: () -> Unit,
    onMenu: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    // Exact visual contract of MainActivity.buildShell()/navBtn().
    val dark = colors.background.red < 0.25f
    val navStart = if (dark) Color(0xFF1F2522) else Color(0xFFFBF7F0)
    val navEnd = if (dark) Color(0xFF1D2220) else Color(0xFFF8F4EC)
    val navLine = if (dark) Color(0xFF3D4641) else Color(0xFFE6DDD0)
    val homeColor = Color(0xFF356D57)
    val mutedColor = if (dark) Color(0xFFAAB3AD) else Color(0xFF6E6A63)
    val shape = RoundedCornerShape(18.dp)
    val scope = rememberCoroutineScope()
    // Azkar reading itself belongs to the Contents context.
    // Outside destinations remain transient until the native Activity opens.
    var activeNavIndex by remember { mutableIntStateOf(1) }
    var leaving by remember { mutableStateOf(false) }
    LaunchedEffect(contentsSelected) {
        if(contentsSelected) activeNavIndex = 1
    }
    fun exitTo(index: Int, target: () -> Unit) {
        if(leaving)return
        leaving = true
        activeNavIndex = index
        scope.launch {
            kotlinx.coroutines.delay(145L)
            target()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .height(64.dp)
            .shadow(3.dp, shape, clip = false)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(navStart, navEnd),
                ),
                shape = shape,
            )
            .border(1.dp, navLine, shape)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        val cellWidth = maxWidth / 4
        val capsuleX by animateDpAsState(
            targetValue = cellWidth * activeNavIndex,
            animationSpec = tween(265, easing = FastOutSlowInEasing),
            label = "azkarMorphPill",
        )
        Box(
            Modifier.offset(x = capsuleX, y = 3.dp)
                .width(cellWidth)
                .height(48.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(homeColor.copy(alpha = if (dark) 0.20f else 0.10f)),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_home,
            label = "Главная",
            color = if (activeNavIndex == 0) homeColor else mutedColor,
            selected = activeNavIndex == 0,
            onClick = { exitTo(0, onHome) },
            modifier = Modifier.weight(1f),
        )
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_contents,
            label = "Содержание",
            color = if (activeNavIndex == 1) homeColor else mutedColor,
            selected = activeNavIndex == 1,
            onClick = { if(!leaving) { activeNavIndex = 1; onContents() } },
            modifier = Modifier.weight(1f),
        )
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_progress,
            label = "Прогресс",
            color = if (activeNavIndex == 2) homeColor else mutedColor,
            selected = activeNavIndex == 2,
            onClick = { exitTo(2, onProgress) },
            modifier = Modifier.weight(1f),
        )
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_menu,
            label = "Меню",
            color = if (activeNavIndex == 3) homeColor else mutedColor,
            selected = activeNavIndex == 3,
            onClick = { exitTo(3, onMenu) },
            modifier = Modifier.weight(1f),
        )
        }
    }
}

@Composable
private fun HeartPrayerNavItem(
    iconRes: Int,
    label: String,
    color: Color,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else if (selected) 1.045f else 1f,
        animationSpec = tween(
            durationMillis = AzkarMotion.navigationPressDurationMillis,
            easing = AzkarMotion.dhikrPageEasing,
        ),
        label = "azkar-bottom-nav-press",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = label; this.selected = selected }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color),
        )
        Box(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = label,
                style = TextStyle(
                    color = color,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.2.sp,
                    lineHeight = 13.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false,
                    ),
                ),
            )
        }
    }
}

