package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

/**
 * Prepares the exact original home artwork on Dispatchers.IO before Home appears.
 * BitmapPainter still draws native Compose images, but its bitmap is decoded
 * off the main thread, and every Image composable gets an independent Painter.
 *
 * This keeps original PNG/WebP bytes and visual appearance unchanged.
 * The map is published atomically so composition never sees partial state.
 */
internal object MedicineHomeArtwork {
    private val gate = Any()
    @Volatile private var bitmaps: Map<Int, ImageBitmap> = emptyMap()
    @Volatile private var cardFrameState: Drawable.ConstantState? = null
    @Volatile private var initialized = false

    private val homeIds = intArrayOf(
        R.drawable.antique_parchment,
        R.drawable.antique_hero_refined,
        R.drawable.antique_card_paper,
        R.drawable.medicine_launcher_book,
        R.drawable.antique_open_book_detail,
        R.drawable.antique_search_detail,
        R.drawable.antique_mortar_detail,
        R.drawable.antique_books_detail,
        R.drawable.antique_scroll_detail,
        R.drawable.antique_remedies_detail,
        R.drawable.antique_bookmark_detail,
        R.drawable.nav_home,
        R.drawable.nav_topics,
        R.drawable.nav_search,
        R.drawable.nav_bookmark,
        R.drawable.nav_more,
    )

    fun isReady(): Boolean = initialized

    fun preload(context: Context) {
        if (initialized) return
        synchronized(gate) {
            if (initialized) return
            val appContext = context.applicationContext
            val resources = appContext.resources
            val decoded = HashMap<Int, ImageBitmap>(homeIds.size)
            for (id in homeIds) {
                try {
                    val bitmap = BitmapFactory.decodeResource(resources, id)
                    if (bitmap != null) decoded[id] = bitmap.asImageBitmap()
                } catch (e: Exception) {
                    Log.w("MedicineHomeArt", "Artwork decode failed for resource $id", e)
                }
            }
            try {
                // Nine-patch must remain a real resizable Drawable; only its
                // shared immutable ConstantState is warmed on the IO thread.
                cardFrameState = appContext.getDrawable(R.drawable.antique_card_frame)?.constantState
            } catch (e: Exception) {
                Log.w("MedicineHomeArt", "Card frame warmup failed", e)
            }
            bitmaps = decoded
            initialized = true
        }
    }

    fun newCardFrame(context: Context): Drawable =
        cardFrameState?.newDrawable(context.resources)?.mutate()
            ?: requireNotNull(context.getDrawable(R.drawable.antique_card_frame))

    @Composable
    fun painter(@DrawableRes id: Int): Painter {
        val bitmap = bitmaps[id]
        return if (bitmap != null) {
            // BitmapPainter stores draw alpha/filter state: never share one
            // Painter between differently tinted/faded image composables.
            remember(bitmap) { BitmapPainter(bitmap) }
        } else {
            painterResource(id)
        }
    }
}
