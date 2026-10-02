package app.xalidmuslim.azkar.ui.reading

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
import app.xalidmuslim.azkar.content.AzkarCatalog
import app.xalidmuslim.azkar.persistence.AzkarDateProvider
import app.xalidmuslim.azkar.persistence.AzkarPreferencesRepository
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
) {
    val scope = rememberCoroutineScope()
    val periodController = remember(initialPeriod) {
        AzkarPeriodReaderController(initialPeriod)
    }
    val uiController = remember(preferencesRepository, dateProvider) {
        AzkarReaderUiController(
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

    key(period) {
        AzkarReaderScreen(
            entries = entries,
            period = period,
            controller = periodController.navigation,
            uiController = uiController,
            modifier = modifier.fillMaxSize(),
            onPeriodChange = { newPeriod ->
                if (periodController.switchTo(newPeriod)) {
                    uiController.setLastPeriod(newPeriod)
                    uiController.closeSheet()
                }
            },
        )
    }
}
