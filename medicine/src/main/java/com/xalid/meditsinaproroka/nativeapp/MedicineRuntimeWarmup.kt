package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Prepares the Medicine runtime while the user is still on the host dashboard.
 * The Activity only reads the warmed objects; no heavy preferences/book parsing
 * is required before its first frame in the normal path.
 */
object MedicineRuntimeWarmup {
    @Volatile
    private var store: AppStore? = null

    private val started = AtomicBoolean(false)
    private val storeLock = Any()

    @JvmStatic
    fun preload(context: Context) {
        val appContext = context.applicationContext
        MedicineBookCache.preload(appContext)
        if (!started.compareAndSet(false, true)) return

        thread(
            start = true,
            isDaemon = true,
            name = "medicine-runtime-warmup",
        ) {
            runCatching { getOrCreateStore(appContext) }
        }
    }

    @JvmStatic
    fun peekStore(): AppStore? = store

    @JvmStatic
    fun getOrCreateStore(context: Context): AppStore {
        store?.let { return it }
        val appContext = context.applicationContext
        return synchronized(storeLock) {
            store ?: AppStore(appContext).also { store = it }
        }
    }
}
