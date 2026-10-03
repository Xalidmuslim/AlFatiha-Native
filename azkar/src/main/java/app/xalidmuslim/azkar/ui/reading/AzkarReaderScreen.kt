package app.xalidmuslim.azkar.ui.reading

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    if (!readerUi.isHydrated) {
        AzkarTheme {
            AzkarSurface(modifier = modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.azkar_launcher_exact),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        )
                        BasicText(
                            text = "Азкар",
                            style = AzkarThemeValues.typography.brandTitle.copy(
                                color = AzkarThemeValues.colors.foreground,
                            ),
                        )
                        BasicText(
                            text = "УТРО · ВЕЧЕР",
                            style = AzkarThemeValues.typography.brandSubtitle.copy(
                                color = AzkarThemeValues.colors.muted,
                            ),
                        )
                    }
                }
            }
        }
        return
    }
    val settings = readerUi.settings
    val globalUiPreferences = remember(context) {
        context.getSharedPreferences("alfatiha_native", android.content.Context.MODE_PRIVATE)
    }

    // One day/night state for the whole integrated application.
    LaunchedEffect(readerUi.isHydrated) {
        if (readerUi.isHydrated) {
            val sharedDark = globalUiPreferences.getBoolean("dark", false)
            val sharedMode = if (sharedDark) AzkarThemeMode.Dark else AzkarThemeMode.Light
            if (settings.themeMode != sharedMode) {
                resolvedUiController.updateSettings { it.copy(themeMode = sharedMode) }
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
        val nextDark = !isDarkTheme
        globalUiPreferences.edit().putBoolean("dark", nextDark).apply()
        resolvedUiController.updateSettings {
            it.copy(
                themeMode = if (nextDark) AzkarThemeMode.Dark else AzkarThemeMode.Light,
            )
        }
    }

    val resolvedEntries = entries.map { entry ->
        entry.copy(
            currentCount = resolvedUiController
                .currentCount(entry.item.id, entry.currentCount)
                .coerceIn(0, entry.item.count),
        )
    }
    val activeIndex = navigation.activeIndex.coerceIn(resolvedEntries.indices)
    val active = resolvedEntries[activeIndex]

    var restoredLastItem by remember(period, controller) { mutableStateOf(false) }
    LaunchedEffect(readerUi.isHydrated, period, readerUi.lastItemByPeriod) {
        if (readerUi.isHydrated && !restoredLastItem) {
            val savedId = readerUi.lastItemByPeriod[period]
            val savedIndex = resolvedEntries.indexOfFirst { it.item.id == savedId }
            if (savedIndex >= 0 && savedIndex != controller.state.activeIndex) {
                controller.selectAnchor(savedIndex)
            }
            restoredLastItem = true
        }
    }
    LaunchedEffect(restoredLastItem, period, navigation.activeIndex) {
        if (restoredLastItem) {
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
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val listScrollMarginPx = with(density) {
        AzkarDimensions.dhikrScrollMarginTop.roundToPx()
    }

    LaunchedEffect(readingScrollState) {
        snapshotFlow { readingScrollState.value }.collect(controller::recordScrollY)
    }

    LaunchedEffect(readerUi.viewMode) {
        if (readerUi.viewMode == AzkarReaderViewMode.List) {
            listState.animateScrollToItem(
                index = AzkarListCardStartIndex + activeListIndex,
                scrollOffset = -listScrollMarginPx,
            )
        }
    }

    BackHandler(enabled = readerUi.activeSheet != AzkarReaderSheet.None) {
        resolvedUiController.closeSheet()
    }
    BackHandler(enabled = readerUi.activeSheet == AzkarReaderSheet.None) {
        navigateToHeartPrayer(context, "home")
    }

    val haptic = LocalHapticFeedback.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }
    var pageDirection by remember { mutableStateOf(1) }
    val pageSlidePx = with(density) { 22.dp.toPx() }
    val initialOffset = if (navigation.generation > 0L && animationsEnabled) {
        pageSlidePx * pageDirection
    } else {
        0f
    }
    val initialAlpha = if (navigation.generation > 0L && animationsEnabled) {
        0.82f
    } else {
        1f
    }
    val transitionOffset = remember(navigation.generation) { Animatable(initialOffset) }
    val transitionAlpha = remember(navigation.generation) { Animatable(initialAlpha) }

    LaunchedEffect(navigation.generation, animationsEnabled) {
        if (!animationsEnabled || navigation.generation == 0L) {
            transitionOffset.snapTo(0f)
            transitionAlpha.snapTo(1f)
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
                            .padding(bottom = 84.dp),
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
                            val finishing = resolvedUiController.currentCount(itemId) == target - 1
                            if (resolvedUiController.incrementProgress(itemId, target) && finishing) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            .padding(bottom = 84.dp),
                        onOpenSettings = resolvedUiController::openSettings,
                        onOpenSearch = { navigateToHeartPrayer(context, "search") },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = toggleTheme,
                        onOpenContents = resolvedUiController::openContents,
                        onViewModeChange = { mode -> resolvedUiController.setViewMode(mode) },
                        onOpenSourceInfo = resolvedUiController::openSourceInfo,
                        onIncrementCount = { itemId, target ->
                            val finishing = resolvedUiController.currentCount(itemId) == target - 1
                            if (resolvedUiController.incrementProgress(itemId, target) && finishing) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                onHome = {
                    navigateToHeartPrayer(context, "home")
                },
                onContents = {
                    navigateToHeartPrayer(context, "menu")
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
                            controller.selectAnchor(index)
                            resolvedUiController.closeSheet()
                            scope.launch {
                                listState.animateScrollToItem(
                                    index = AzkarListCardStartIndex +
                                        listVisibleIndices.indexOf(index).coerceAtLeast(0),
                                    scrollOffset = -listScrollMarginPx,
                                )
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
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(R.anim.azkar_enter, R.anim.azkar_exit)
    }
}

@Composable
private fun HeartPrayerBottomNav(
    modifier: Modifier = Modifier,
    onHome: () -> Unit,
    onContents: () -> Unit,
    onProgress: () -> Unit,
    onMenu: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .background(colors.card.copy(alpha = 0.98f), shape)
            .border(1.dp, colors.border.copy(alpha = 0.72f), shape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_home,
            label = "Главная",
            color = colors.primary,
            onClick = onHome,
            modifier = Modifier.weight(1f),
        )
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_contents,
            label = "Инструменты",
            color = colors.muted,
            onClick = onContents,
            modifier = Modifier.weight(1f),
        )
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_progress,
            label = "Прогресс",
            color = colors.muted,
            onClick = onProgress,
            modifier = Modifier.weight(1f),
        )
        HeartPrayerNavItem(
            iconRes = R.drawable.ic_nav_menu,
            label = "Меню",
            color = colors.muted,
            onClick = onMenu,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HeartPrayerNavItem(
    iconRes: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics { contentDescription = label }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color),
        )
        BasicText(
            text = label,
            style = AzkarThemeValues.typography.sourceNote.copy(
                color = color,
                fontSize = 11.sp,
            ),
        )
    }
}

