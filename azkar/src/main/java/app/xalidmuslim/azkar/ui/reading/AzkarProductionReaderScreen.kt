package app.xalidmuslim.azkar.ui.reading

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
import androidx.compose.ui.platform.LocalContext
import app.xalidmuslim.azkar.content.AzkarCatalog
import app.xalidmuslim.azkar.persistence.AzkarDateProvider
import app.xalidmuslim.azkar.persistence.AzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.AzkarPreferencesSnapshot
import app.xalidmuslim.azkar.persistence.SystemAzkarDateProvider

class AzkarPeriodReaderController(
    initialPeriod: AzkarPeriod = AzkarPeriod.Morning,
    initialItemId: String? = null,
) {
    private fun initialIndex(period: AzkarPeriod, itemId: String?): Int {
        if (itemId.isNullOrBlank()) return 0
        return AzkarCatalog.itemsFor(period)
            .indexOfFirst { it.id == itemId }
            .coerceAtLeast(0)
    }

    var period by mutableStateOf(initialPeriod)
        private set

    var navigation by mutableStateOf(
        AzkarReaderNavigationController(
            itemCount = AzkarCatalog.itemsFor(initialPeriod).size,
            initialIndex = initialIndex(initialPeriod, initialItemId),
        ),
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
    val seededPeriod = initialSnapshot?.lastPeriod ?: initialPeriod
    val seededItemId = initialSnapshot?.lastItemByPeriod?.get(seededPeriod)
    val periodController = remember(initialPeriod, initialSnapshot) {
        AzkarPeriodReaderController(
            initialPeriod = seededPeriod,
            initialItemId = seededItemId,
        )
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
    val readerUi = uiController.state
    var restoredPeriod by remember(uiController) {
        mutableStateOf(
            readerUi.isHydrated && readerUi.lastPeriod == periodController.period,
        )
    }

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
    var restoredInitialItem by remember(uiController, period) {
        val savedId = readerUi.lastItemByPeriod[period]
        val activeId = entries
            .getOrNull(periodController.navigation.state.activeIndex)
            ?.item
            ?.id
        mutableStateOf(
            readerUi.isHydrated && (savedId == null || savedId == activeId),
        )
    }

    // Restore only on a true cold start. Warm starts are already positioned
    // before the first composition, so there is no visible second jump. Previously the activity
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
                modifier = Modifier.fillMaxSize(),
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
