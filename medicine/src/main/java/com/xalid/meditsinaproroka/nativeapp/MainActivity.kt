package com.xalid.meditsinaproroka.nativeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class MedicineLaunchState(
    val store: AppStore,
    val bookResult: Result<BookData>,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContext = applicationContext
        val launchTheme = if (
            getSharedPreferences("alfatiha_native", MODE_PRIVATE).getBoolean("dark", false)
        ) "dark" else "light"

        MedicineRuntimeWarmup.preload(appContext)

        setContent {
            val warmedState = remember {
                val warmedStore = MedicineRuntimeWarmup.peekStore()
                val warmedBook = MedicineBookCache.peekOrNull()
                if (warmedStore != null && warmedBook != null) {
                    MedicineLaunchState(
                        store = warmedStore,
                        bookResult = Result.success(warmedBook),
                    )
                } else {
                    null
                }
            }
            var launchState by remember { mutableStateOf(warmedState) }

            LaunchedEffect(Unit) {
                if (launchState == null) {
                    launchState = withContext(Dispatchers.IO) {
                        val store = MedicineRuntimeWarmup.getOrCreateStore(appContext)
                        val bookResult = MedicineBookCache.getOrLoad(appContext)
                        bookResult.exceptionOrNull()?.let { error ->
                            Log.e(TAG, "Failed to load bundled book.json", error)
                        }
                        MedicineLaunchState(
                            store = store,
                            bookResult = bookResult,
                        )
                    }
                }
            }

            val state = launchState
            MedicinaTheme(state?.store?.settings?.theme ?: launchTheme) {
                if (state == null) {
                    BookLoadingScreen()
                } else {
                    state.bookResult.fold(
                        onSuccess = { book ->
                            MedicinaApp(book = book, store = state.store)
                        },
                        onFailure = { BookLoadErrorScreen() },
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "MedicineProphet"
    }
}

@Composable
private fun BookLoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Открываем книгу…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun BookLoadErrorScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "Не удалось открыть книгу",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Не удалось прочитать данные книги. Закройте раздел и попробуйте открыть его снова.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
