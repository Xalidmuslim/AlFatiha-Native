package app.xalidmuslim.azkar

import android.content.Context
import app.xalidmuslim.azkar.content.AzkarCatalog
import app.xalidmuslim.azkar.persistence.DataStoreAzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.SystemAzkarDateProvider
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Warms the Azkar DataStore and catalog without touching UI state.
 * Safe to call from the host app; it never blocks the caller.
 */
object AzkarWarmup {
    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @JvmStatic
    fun preload(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        scope.launch {
            runCatching {
                val repository = DataStoreAzkarPreferencesRepository(
                    appContext.azkarPreferencesDataStore,
                )
                repository.observeSnapshot(
                    date = SystemAzkarDateProvider.currentDate(),
                    visibleItemIds = AzkarCatalog.stableIds,
                ).first()
            }
        }
    }
}
