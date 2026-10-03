package app.xalidmuslim.azkar.ui.reading

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import app.xalidmuslim.azkar.content.AzkarCatalog
import app.xalidmuslim.azkar.persistence.AzkarDateProvider
import app.xalidmuslim.azkar.persistence.AzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.AzkarPreferencesSnapshot
import app.xalidmuslim.azkar.persistence.SystemAzkarDateProvider

class AzkarPeriodReaderController(
    initialPeriod: AzkarPeriod = AzkarPeriod.Morning,
) {
    var period by mutableStateOf(initialPeriod)
        private set

    var navigation by mutableStateOf(
        AzkarReaderNavigationController(AzkarCatalog.itemsFor(initialPeriod).size),
    )
        private set

    fun switchTo(newPeriod: AzkarPeriod): Boolean {
        if (newPeriod == period) return false
        period = newPeriod
        navigation = AzkarReaderNavigationController(
            itemCount = AzkarCatalog.itemsFor(newPeriod).size,
            initialIndex = 0,
        )
        return true
    }
}

@Composable
fun AzkarProductionReaderScreen(
    modifier: Modifier = Modifier,
    initialPeriod: AzkarPeriod = AzkarPeriod.Morning,
) {
    val periodController = remember(initialPeriod) {
        AzkarPeriodReaderController(initialPeriod)
    }
    val uiController = remember { AzkarReaderUiController() }
    AzkarProductionReaderScreen(
        periodController = periodController,
        uiController = uiController,
        modifier = modifier,
    )
}

@Composable
fun AzkarProductionReaderScreen(
    preferencesRepository: AzkarPreferencesRepository,
    dateProvider: AzkarDateProvider = SystemAzkarDateProvider,
    modifier: Modifier = Modifier,
    initialPeriod: AzkarPeriod = AzkarPeriod.Morning,
    initialSnapshot: AzkarPreferencesSnapshot? = null,
) {
    val scope = rememberCoroutineScope()
    val periodController = remember(initialPeriod, initialSnapshot) {
        AzkarPeriodReaderController(initialSnapshot?.lastPeriod ?: initialPeriod)
    }
    val uiController = remember(preferencesRepository, dateProvider, initialSnapshot) {
        AzkarReaderUiController(
            initialSnapshot = initialSnapshot,
            repository = preferencesRepository,
            dateProvider = dateProvider,
            persistenceScope = scope,
            visibleItemIds = AzkarCatalog.stableIds,
        )
    }
    AzkarProductionReaderScreen(
        periodController = periodController,
        uiController = uiController,
        modifier = modifier,
    )
}

@Composable
internal fun AzkarProductionReaderScreen(
    periodController: AzkarPeriodReaderController,
    uiController: AzkarReaderUiController,
    modifier: Modifier = Modifier,
) {
    var restoredPeriod by remember(uiController) { mutableStateOf(false) }
    var restoredInitialItem by remember(uiController) { mutableStateOf(false) }
    val readerUi = uiController.state

    LaunchedEffect(readerUi.isHydrated, readerUi.lastPeriod) {
        if (readerUi.isHydrated && !restoredPeriod) {
            periodController.switchTo(readerUi.lastPeriod)
            restoredPeriod = true
        }
    }

    val period = periodController.period
    val entries = remember(period) {
        AzkarCatalog.readingItemsFor(period).map(::AzkarReaderEntry)
    }

    // Restore the last opened azkar before the first visible frame. Previously the activity
    // could briefly draw the default morning/first item and then replace it after DataStore
    // hydration, which looked like a delayed page refresh.
    LaunchedEffect(
        readerUi.isHydrated,
        restoredPeriod,
        period,
        readerUi.lastItemByPeriod,
    ) {
        if (readerUi.isHydrated && restoredPeriod && !restoredInitialItem) {
            val savedId = readerUi.lastItemByPeriod[period]
            val savedIndex = entries.indexOfFirst { it.item.id == savedId }
            if (savedIndex >= 0 && savedIndex != periodController.navigation.state.activeIndex) {
                periodController.navigation.selectAnchor(savedIndex)
            }
            restoredInitialItem = true
        }
    }

    val readyForFirstFrame =
        readerUi.isHydrated && restoredPeriod && restoredInitialItem
    val contentAlpha by animateFloatAsState(
        // Keep the destination visible while persistent state catches up.
        targetValue = if (readyForFirstFrame) 1f else 0.90f,
        animationSpec = tween(durationMillis = 110),
        label = "azkar-entry-fade",
    )

    val context = LocalContext.current
    val sharedDark = remember(context) {
        context
            .getSharedPreferences("alfatiha_native", android.content.Context.MODE_PRIVATE)
            .getBoolean("dark", false)
    }
    val launchBackground = if (sharedDark) {
        Color(0xFF171C1A)
    } else {
        Color(0xFFF3EEE4)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(launchBackground),
    ) {
        key(period) {
            AzkarReaderScreen(
                entries = entries,
                period = period,
                controller = periodController.navigation,
                uiController = uiController,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentAlpha },
                onPeriodChange = { newPeriod ->
                    if (periodController.switchTo(newPeriod)) {
                        uiController.setLastPeriod(newPeriod)
                        uiController.closeSheet()
                    }
                },
            )
        }
    }
}
