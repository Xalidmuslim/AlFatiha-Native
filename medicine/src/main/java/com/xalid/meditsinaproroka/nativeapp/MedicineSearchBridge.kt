package com.xalid.meditsinaproroka.nativeapp

import android.content.Context

data class MedicineSearchDocument(
    val chapterId: String,
    val title: String,
    val body: String,
)

object MedicineSearchBridge {
    @JvmStatic
    fun load(context: Context): List<MedicineSearchDocument> {
        val book = MedicineBookCache.getOrLoad(context.applicationContext).getOrNull()
            ?: return emptyList()

        return book.chapters.map { chapter ->
            MedicineSearchDocument(
                chapterId = chapter.id,
                title = chapter.title,
                body = buildString {
                    chapter.blocks.forEachIndexed { index, block ->
                        if (index > 0) append('\n')
                        append(block.text)
                    }
                },
            )
        }
    }
}
