package com.xalid.meditsinaproroka.nativeapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
}

fun shareText(context: Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться"))
}

fun chapterPlainText(book: BookData, chapter: Chapter, includeSource: Boolean = false): String = buildString {
    if (includeSource) {
        append(book.title).append("\n")
        append(chapter.title).append("\n\n")
    }
    chapter.blocks.forEach { block ->
        if (block.type == "subheading") append("\n").append(block.text).append("\n\n")
        else {
            append(block.text)
            if (block.type == "quran") append(block.quranReference.orEmpty())
            append("\n\n")
        }
    }
}.trim()

fun formatDate(time: Long): String =
    SimpleDateFormat("dd.MM.yyyy · HH:mm", Locale.getDefault()).format(Date(time))

fun searchBook(book: BookData, query: String, filter: SearchFilter): List<SearchHit> {
    val q = query.trim().lowercase(Locale.getDefault())
    if (q.length < 2) return emptyList()
    val chaptersById = book.chapters.associateBy { it.id }

    if (filter == SearchFilter.REMEDIES) {
        return book.remedies
            .filter { r ->
                r.title.lowercase(Locale.getDefault()).contains(q) ||
                    r.aliases.any { it.lowercase(Locale.getDefault()).contains(q) }
            }
            .flatMap { remedy ->
                remedy.chapterIds.mapIndexedNotNull { index, id ->
                    chaptersById[id]?.let {
                        SearchHit(it.id, remedy.anchors.getOrNull(index), it.title, remedy.title, "remedy")
                    }
                }
            }
            .distinctBy { it.chapterId to it.anchor }
            .take(250)
    }

    return buildList {
        book.chapters.forEach { chapter ->
            if (filter == SearchFilter.TITLES && chapter.title.lowercase(Locale.getDefault()).contains(q)) {
                add(SearchHit(chapter.id, null, chapter.title, chapter.title, "title"))
            }
            if (filter != SearchFilter.TITLES) {
                chapter.blocks.forEach { block ->
                    val allowed = when (filter) {
                        SearchFilter.ALL -> true
                        SearchFilter.HADITH -> block.type == "hadith"
                        SearchFilter.AUTHOR -> block.type == "text" || block.type == "historical_note"
                        else -> false
                    }
                    if (allowed) {
                        val lower = block.text.lowercase(Locale.getDefault())
                        val at = lower.indexOf(q)
                        if (at >= 0) {
                            val from = (at - 75).coerceAtLeast(0)
                            val to = (at + q.length + 120).coerceAtMost(block.text.length)
                            val snippet = (if (from > 0) "…" else "") +
                                block.text.substring(from, to) +
                                (if (to < block.text.length) "…" else "")
                            add(SearchHit(chapter.id, block.anchor, chapter.title, snippet, block.type))
                        }
                    }
                }
            }
        }
    }.take(400)
}
