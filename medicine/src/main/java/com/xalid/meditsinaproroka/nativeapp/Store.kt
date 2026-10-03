package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal const val DEFAULT_BOOKMARK_FOLDER = "Без папки"
internal val DEFAULT_BOOKMARK_FOLDERS = listOf(DEFAULT_BOOKMARK_FOLDER, "Изучить", "Важное")

internal fun canonicalBookmarkFolder(folder: String): String {
    val clean = folder.trim()
    return when (clean) {
        "", "Общее" -> DEFAULT_BOOKMARK_FOLDER
        "Вернуться" -> "Важное"
        else -> clean
    }
}

data class ReaderSettings(
    val theme: String = "system",
    val fontFamily: String = "system",
    val fontSizeSp: Float = 17f,
    val lineSpacing: Float = 1.20f,
    val showHistoricalLabels: Boolean = true,
)

data class Bookmark(val chapterId: String, val anchor: String? = null, val folder: String = DEFAULT_BOOKMARK_FOLDER)
data class Note(
    val id: String,
    val chapterId: String,
    val blockId: String,
    val start: Int,
    val end: Int,
    val selectedText: String,
    val note: String,
    val createdAt: Long,
)
data class Highlight(
    val id: String,
    val chapterId: String,
    val blockId: String,
    val start: Int,
    val end: Int,
    val createdAt: Long,
)
data class HistoryEntry(val chapterId: String, val anchor: String?, val openedAt: Long)

class AppStore(context: Context) {
    private val prefs = context.getSharedPreferences("medicina_native", Context.MODE_PRIVATE)
    private val sharedPrefs = context.getSharedPreferences("alfatiha_native", Context.MODE_PRIVATE)

    var settings by mutableStateOf(loadSettings())
        private set

    val bookmarks = mutableStateListOf<Bookmark>()
    val notes = mutableStateListOf<Note>()
    val highlights = mutableStateListOf<Highlight>()
    val history = mutableStateListOf<HistoryEntry>()
    val progress = mutableStateMapOf<String, Int>()
    val bookmarkFolders = mutableStateListOf(*DEFAULT_BOOKMARK_FOLDERS.toTypedArray())

    var lastChapterId by mutableStateOf(prefs.getString("lastChapterId", null))
        private set
    var lastBlockIndex by mutableStateOf(prefs.getInt("lastBlockIndex", 0))
        private set

    init {
        bookmarks.addAll(loadBookmarks())
        notes.addAll(loadNotes())
        highlights.addAll(loadHighlights())
        history.addAll(loadHistory())
        loadProgress()
        loadFolders()
        migrateWebReaderDefaultsIfNeeded()
        migrateCompactLineSpacingIfNeeded()
        if (!prefs.getBoolean("bookmarkFoldersV2Migrated", false)) {
            persistBookmarks()
            persistFolders()
            prefs.edit().putBoolean("bookmarkFoldersV2Migrated", true).apply()
        }
    }

    fun updateSettings(transform: (ReaderSettings) -> ReaderSettings) {
        val next = transform(settings)
        settings = next
        persistSettings()
        if (next.theme == "dark" || next.theme == "light") {
            sharedPrefs.edit().putBoolean("dark", next.theme == "dark").apply()
        }
    }

    fun syncSharedTheme() {
        val sharedTheme = if (sharedPrefs.getBoolean("dark", false)) "dark" else "light"
        if (settings.theme != sharedTheme) {
            settings = settings.copy(theme = sharedTheme)
            persistSettings()
        }
    }

    fun toggleSharedTheme() {
        val next = if (sharedPrefs.getBoolean("dark", false)) "light" else "dark"
        settings = settings.copy(theme = next)
        persistSettings()
        sharedPrefs.edit().putBoolean("dark", next == "dark").apply()
    }

    fun setLastPosition(chapterId: String, blockIndex: Int) {
        lastChapterId = chapterId
        lastBlockIndex = blockIndex.coerceAtLeast(0)
        progress[chapterId] = lastBlockIndex
        prefs.edit()
            .putString("lastChapterId", chapterId)
            .putInt("lastBlockIndex", lastBlockIndex)
            .putString("progress", progressToJson().toString())
            .apply()
    }

    fun addHistory(chapterId: String, anchor: String?) {
        history.removeAll { it.chapterId == chapterId && it.anchor == anchor }
        history.add(0, HistoryEntry(chapterId, anchor, System.currentTimeMillis()))
        while (history.size > 60) history.removeAt(history.lastIndex)
        persistHistory()
    }

    fun toggleChapterBookmark(chapterId: String, folder: String = DEFAULT_BOOKMARK_FOLDER) {
        val index = bookmarks.indexOfFirst { it.chapterId == chapterId && it.anchor == null }
        if (index >= 0) bookmarks.removeAt(index) else bookmarks.add(Bookmark(chapterId, null, canonicalBookmarkFolder(folder)))
        persistBookmarks()
    }

    fun toggleAnchorBookmark(chapterId: String, anchor: String, folder: String = DEFAULT_BOOKMARK_FOLDER) {
        val index = bookmarks.indexOfFirst { it.chapterId == chapterId && it.anchor == anchor }
        if (index >= 0) bookmarks.removeAt(index) else bookmarks.add(Bookmark(chapterId, anchor, canonicalBookmarkFolder(folder)))
        persistBookmarks()
    }

    fun addFolder(name: String) {
        val clean = canonicalBookmarkFolder(name)
        if (clean.isNotEmpty() && clean !in bookmarkFolders) {
            bookmarkFolders.add(clean)
            persistFolders()
        }
    }

    fun addHighlight(chapterId: String, blockId: String, start: Int, end: Int) {
        if (start < 0 || end <= start) return
        highlights.add(Highlight(UUID.randomUUID().toString(), chapterId, blockId, start, end, System.currentTimeMillis()))
        persistHighlights()
    }

    fun removeHighlights(chapterId: String, blockId: String, start: Int, end: Int) {
        highlights.removeAll { it.chapterId == chapterId && it.blockId == blockId && it.start < end && it.end > start }
        persistHighlights()
    }

    fun addNote(chapterId: String, blockId: String, start: Int, end: Int, selectedText: String, note: String) {
        if (note.isBlank()) return
        notes.add(0, Note(UUID.randomUUID().toString(), chapterId, blockId, start, end, selectedText, note.trim(), System.currentTimeMillis()))
        persistNotes()
    }

    fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        persistNotes()
    }

    fun isChapterBookmarked(chapterId: String) = bookmarks.any { it.chapterId == chapterId && it.anchor == null }
    fun highlightsFor(chapterId: String, blockId: String) = highlights.filter { it.chapterId == chapterId && it.blockId == blockId }
    fun notesFor(chapterId: String, blockId: String) = notes.filter { it.chapterId == chapterId && it.blockId == blockId }

    private fun migrateWebReaderDefaultsIfNeeded() {
        if (prefs.getBoolean("webReaderDefaultsV1", false)) return
        if (kotlin.math.abs(settings.fontSizeSp - 18f) < 0.01f &&
            kotlin.math.abs(settings.lineSpacing - 1.62f) < 0.01f
        ) {
            settings = settings.copy(fontSizeSp = 17f, lineSpacing = 1.48f)
            persistSettings()
        }
        prefs.edit().putBoolean("webReaderDefaultsV1", true).apply()
    }

    private fun migrateCompactLineSpacingIfNeeded() {
        if (prefs.getBoolean("compactLineSpacingV2", false)) return
        val compact = when {
            settings.lineSpacing <= 1.30f -> settings.lineSpacing
            settings.lineSpacing <= 1.66f -> 1.28f
            settings.lineSpacing <= 1.90f -> 1.38f
            else -> 1.48f
        }
        if (kotlin.math.abs(settings.lineSpacing - compact) > 0.01f) {
            settings = settings.copy(lineSpacing = compact)
            persistSettings()
        }
        prefs.edit().putBoolean("compactLineSpacingV2", true).apply()
    }

    fun clearProgress() {
        progress.clear()
        lastChapterId = null
        lastBlockIndex = 0
        history.clear()
        prefs.edit().remove("progress").remove("lastChapterId").remove("lastBlockIndex").remove("history").apply()
    }

    private fun loadSettings(): ReaderSettings {
        val sharedTheme = if (sharedPrefs.getBoolean("dark", false)) "dark" else "light"
        val raw = prefs.getString("settings", null) ?: return ReaderSettings(theme = sharedTheme)
        return runCatching {
            JSONObject(raw).let {
                ReaderSettings(
                    theme = sharedTheme,
                    fontFamily = it.optString("fontFamily", "system"),
                    fontSizeSp = it.optDouble("fontSizeSp", 17.0).toFloat(),
                    lineSpacing = it.optDouble("lineSpacing", 1.20).toFloat(),
                    showHistoricalLabels = it.optBoolean("showHistoricalLabels", true),
                )
            }
        }.getOrDefault(ReaderSettings(theme = sharedTheme))
    }

    private fun persistSettings() {
        val s = settings
        prefs.edit().putString("settings", JSONObject()
            .put("theme", s.theme)
            .put("fontFamily", s.fontFamily)
            .put("fontSizeSp", s.fontSizeSp)
            .put("lineSpacing", s.lineSpacing)
            .put("showHistoricalLabels", s.showHistoricalLabels)
            .toString()).apply()
    }

    private fun loadBookmarks(): List<Bookmark> = parseArray("bookmarks") {
        Bookmark(
            it.optString("chapterId"),
            it.optString("anchor").takeIf(String::isNotBlank),
            canonicalBookmarkFolder(it.optString("folder", DEFAULT_BOOKMARK_FOLDER))
        )
    }
    private fun persistBookmarks() = putArray("bookmarks", bookmarks.map {
        JSONObject().put("chapterId", it.chapterId).put("anchor", it.anchor ?: "").put("folder", it.folder)
    })

    private fun loadNotes(): List<Note> = parseArray("notes") {
        Note(
            it.optString("id"), it.optString("chapterId"), it.optString("blockId"),
            it.optInt("start"), it.optInt("end"), it.optString("selectedText"),
            it.optString("note"), it.optLong("createdAt")
        )
    }
    private fun persistNotes() = putArray("notes", notes.map {
        JSONObject().put("id", it.id).put("chapterId", it.chapterId).put("blockId", it.blockId)
            .put("start", it.start).put("end", it.end).put("selectedText", it.selectedText)
            .put("note", it.note).put("createdAt", it.createdAt)
    })

    private fun loadHighlights(): List<Highlight> = parseArray("highlights") {
        Highlight(it.optString("id"), it.optString("chapterId"), it.optString("blockId"), it.optInt("start"), it.optInt("end"), it.optLong("createdAt"))
    }
    private fun persistHighlights() = putArray("highlights", highlights.map {
        JSONObject().put("id", it.id).put("chapterId", it.chapterId).put("blockId", it.blockId)
            .put("start", it.start).put("end", it.end).put("createdAt", it.createdAt)
    })

    private fun loadHistory(): List<HistoryEntry> = parseArray("history") {
        HistoryEntry(it.optString("chapterId"), it.optString("anchor").takeIf(String::isNotBlank), it.optLong("openedAt"))
    }
    private fun persistHistory() = putArray("history", history.map {
        JSONObject().put("chapterId", it.chapterId).put("anchor", it.anchor ?: "").put("openedAt", it.openedAt)
    })

    private fun loadProgress() {
        val raw = prefs.getString("progress", null) ?: return
        runCatching {
            val o = JSONObject(raw)
            o.keys().forEach { key -> progress[key] = o.optInt(key) }
        }
    }
    private fun progressToJson() = JSONObject().also { o -> progress.forEach { (k, v) -> o.put(k, v) } }

    private fun loadFolders() {
        val arr = runCatching { JSONArray(prefs.getString("folders", "[]")) }.getOrNull() ?: return
        for (i in 0 until arr.length()) {
            val f = canonicalBookmarkFolder(arr.optString(i))
            if (f.isNotBlank() && f !in bookmarkFolders) bookmarkFolders.add(f)
        }
    }
    private fun persistFolders() = prefs.edit().putString("folders", JSONArray(bookmarkFolders).toString()).apply()

    private inline fun <T> parseArray(key: String, factory: (JSONObject) -> T): List<T> {
        val arr = runCatching { JSONArray(prefs.getString(key, "[]")) }.getOrNull() ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { add(factory(it)) }
        }
    }
    private fun putArray(key: String, values: List<JSONObject>) {
        prefs.edit().putString(key, JSONArray(values).toString()).apply()
    }
}
