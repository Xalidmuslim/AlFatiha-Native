package app.xalidmuslim.azkar

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.preferencesDataStore
import app.xalidmuslim.azkar.persistence.AzkarDateProvider
import app.xalidmuslim.azkar.persistence.DataStoreAzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.SystemAzkarDateProvider
import app.xalidmuslim.azkar.ui.reading.AzkarProductionReaderScreen

val Context.azkarPreferencesDataStore by preferencesDataStore(
    name = "azkar_preferences",
)

@Composable
fun AzkarAppRoot(
    dateProvider: AzkarDateProvider = SystemAzkarDateProvider,
) {
    val applicationContext = LocalContext.current.applicationContext
    val repository = remember(applicationContext) {
        DataStoreAzkarPreferencesRepository(
            applicationContext.azkarPreferencesDataStore,
        )
    }

    AzkarProductionReaderScreen(
        preferencesRepository = repository,
        dateProvider = dateProvider,
        modifier = Modifier.fillMaxSize(),
    )
}
