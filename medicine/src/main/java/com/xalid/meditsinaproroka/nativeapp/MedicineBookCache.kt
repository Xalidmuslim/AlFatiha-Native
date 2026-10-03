package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object MedicineBookCache {
    @Volatile
    private var cached: BookData? = null

    private val preloadStarted = AtomicBoolean(false)
    private val loadLock = Any()

    @JvmStatic
    fun peekOrNull(): BookData? = cached

    @JvmStatic
    fun preload(context: Context) {
        if (cached != null || !preloadStarted.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        thread(
            start = true,
            isDaemon = true,
            name = "medicine-book-warmup",
        ) {
            runCatching { getOrLoad(appContext) }
        }
    }

    @JvmStatic
    fun getOrLoad(context: Context): Result<BookData> {
        cached?.let { return Result.success(it) }
        return synchronized(loadLock) {
            cached?.let { return@synchronized Result.success(it) }
            runCatching {
                context.assets.open("book.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { parseBook(it.readText()) }
                    .also { cached = it }
            }
        }
    }
}
