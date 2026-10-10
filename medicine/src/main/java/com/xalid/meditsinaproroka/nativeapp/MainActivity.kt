package com.xalid.meditsinaproroka.nativeapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        configureActivityTransitions()

        val appContext = applicationContext
        val launchTheme = if (
            getSharedPreferences("alfatiha_native", MODE_PRIVATE).getBoolean("dark", false)
        ) "dark" else "light"

        // The standalone Activity's IO coroutine performs the single cold load.
        // Avoid racing it with separate preload threads on every cold launch.
        setContent {
            val warmedState = remember {
                val warmedStore = MedicineRuntimeWarmup.peekStore()
                val warmedBook = MedicineBookCache.peekOrNull()
                if (warmedStore != null && warmedBook != null && MedicineHomeArtwork.isReady()) {
                    warmedStore.syncSharedTheme()
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
                        store.syncSharedTheme()
                        val bookResult = MedicineBookCache.getOrLoad(appContext)
                        // Decode home artwork while the loading screen is still
                        // visible. The first Home frame should never synchronously
                        // decode the hero, paper tiles or bottom-navigation icons.
                        if (bookResult.isSuccess) {
                            MedicineHomeArtwork.preload(appContext)
                        }
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
                // Fade directly into ready content; no text-based loading page.
                Crossfade(
                    targetState = state == null,
                    animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing),
                    label = "medicineLaunch",
                ) { stillLoading ->
                    if (stillLoading) {
                        BookLoadingScreen()
                    } else {
                        val loaded = state
                        if (loaded != null) {
                            loaded.bookResult.fold(
                                onSuccess = { book -> MedicinaApp(book = book, store = loaded.store) },
                                onFailure = { BookLoadErrorScreen() },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MedicineRuntimeWarmup.peekStore()?.syncSharedTheme()
    }

    private fun configureActivityTransitions() {
        if (Build.VERSION.SDK_INT >= 34) {
            // Opening is already driven by ActivityOptions.makeCustomAnimation()
            // in the host activity. Do not stack a second OPEN transition here.
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE,
                R.anim.section_return_enter,
                R.anim.section_return_exit,
            )
        }
    }

    private companion object {
        const val TAG = "MedicineProphet"
    }
}

@Composable
private fun BookLoadingScreen() {
    // Cold starts may require one asynchronous JSON parse. Keep it quiet
    // and on-brand rather than showing a conspicuous loading sentence.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.medicine_launcher_book),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                alpha = 0.82f,
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
