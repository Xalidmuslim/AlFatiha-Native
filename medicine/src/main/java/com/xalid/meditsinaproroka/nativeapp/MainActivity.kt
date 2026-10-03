package com.xalid.meditsinaproroka.nativeapp

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContext = applicationContext
        val launchTheme = if (
            getSharedPreferences("alfatiha_native", MODE_PRIVATE).getBoolean("dark", false)
        ) "dark" else "light"

        setContent {
            var store by remember { mutableStateOf<AppStore?>(null) }
            var bookResult by remember { mutableStateOf<Result<BookData>?>(null) }

            LaunchedEffect(Unit) {
                coroutineScope {
                    val storeDeferred = async(Dispatchers.IO) { AppStore(appContext) }
                    val bookDeferred = async(Dispatchers.IO) {
                        MedicineBookCache.getOrLoad(appContext)
                    }
                    store = storeDeferred.await()
                    bookResult = bookDeferred.await()
                    bookResult?.exceptionOrNull()?.let { error ->
                        Log.e(TAG, "Failed to load bundled book.json", error)
                    }
                }
            }

            val loadedStore = store
            val loadedBook = bookResult
            val ready = loadedStore != null && loadedBook != null

            Crossfade(
                targetState = ready,
                animationSpec = tween(durationMillis = 140),
                label = "medicine-bootstrap",
            ) { isReady ->
                if (!isReady) {
                    MedicinaTheme(launchTheme) {
                        BookLoadingScreen()
                    }
                } else {
                    val actualStore = loadedStore
                    val actualBook = loadedBook
                    if (actualStore == null || actualBook == null) {
                        MedicinaTheme(launchTheme) { BookLoadingScreen() }
                    } else {
                        MedicinaTheme(actualStore.settings.theme) {
                            actualBook.fold(
                                onSuccess = { book ->
                                    MedicinaApp(book = book, store = actualStore)
                                },
                                onFailure = { BookLoadErrorScreen() },
                            )
                        }
                    }
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
