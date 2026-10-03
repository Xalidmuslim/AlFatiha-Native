package com.xalid.meditsinaproroka.nativeapp

import android.app.AlertDialog
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: BookData,
    store: AppStore,
    route: Route.Reader,
    modifier: Modifier,
    back: () -> Unit,
    navigate: (Route) -> Unit,
) {
    val chapter = book.chapters.firstOrNull { it.id == route.chapterId }
    if (chapter == null) {
        Column(modifier.fillMaxSize()) {
            PageHeader("Глава не найдена", null, back)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Не удалось открыть главу") }
        }
        return
    }

    val listState = rememberLazyListState()
    var settingsOpen by remember { mutableStateOf(false) }
    var bookmarkFolderOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val topics = remember(book, chapter) { chapter.topics.mapNotNull { id -> book.topics.firstOrNull { it.id == id } } }
    val remedies = remember(book, chapter) { chapter.remedies.mapNotNull { id -> book.remedies.firstOrNull { it.id == id } } }

    LaunchedEffect(chapter.id, route.anchor, route.resume) {
        store.addHistory(chapter.id, route.anchor)
        val target = when {
            route.anchor != null -> chapter.blocks.indexOfFirst { it.anchor == route.anchor }.takeIf { it >= 0 } ?: 0
            route.resume -> store.progress[chapter.id] ?: if (store.lastChapterId == chapter.id) store.lastBlockIndex else 0
            else -> 0
        }
        if (route.anchor != null || route.resume) listState.scrollToItem((target + 1).coerceAtMost(chapter.blocks.size))
        else listState.scrollToItem(0)
    }

    val latestVisibleBlock = remember(chapter.id) { intArrayOf(0) }

    LaunchedEffect(chapter.id, listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { index ->
                val blockIndex = (index - 1)
                    .coerceAtLeast(0)
                    .coerceAtMost((chapter.blocks.size - 1).coerceAtLeast(0))
                latestVisibleBlock[0] = blockIndex

                // Do not mutate Compose state / SharedPreferences for every row crossed
                // during a fling. Persist only after the viewport settles briefly.
                delay(220)
                store.setLastPosition(chapter.id, blockIndex)
            }
    }

    DisposableEffect(chapter.id) {
        onDispose {
            store.setLastPosition(chapter.id, latestVisibleBlock[0])
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            PageHeader(chapter.title, "Глава ${chapter.order} из ${book.chapters.size}", back)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                item(key = "header") {
                    Text(
                        chapter.section,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = WebSansFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        chapter.title,
                        fontFamily = WebLiterataFont,
                        fontSize = 28.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(11.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f))
                    Spacer(Modifier.height(7.dp))
                }

                items(
                    count = chapter.blocks.size,
                    key = { index -> chapter.blocks[index].id },
                    contentType = { index -> chapter.blocks[index].type },
                ) { index ->
                    ReaderBlock(
                        chapter = chapter,
                        block = chapter.blocks[index],
                        store = store,
                    )
                }

                item(key = "actions") {
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    Text("Действия с главой", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { copyToClipboard(context, chapter.title, chapterPlainText(book, chapter)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(6.dp)); Text("Копировать главу") }
                    Spacer(Modifier.height(7.dp))
                    OutlinedButton(
                        onClick = { copyToClipboard(context, chapter.title, chapterPlainText(book, chapter, includeSource = true)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Icon(Icons.Default.FormatQuote, null); Spacer(Modifier.width(6.dp)); Text("Копировать с названием книги и главы") }
                    Spacer(Modifier.height(7.dp))
                    OutlinedButton(
                        onClick = { shareText(context, chapter.title, chapterPlainText(book, chapter, includeSource = true)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Поделиться") }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (store.isChapterBookmarked(chapter.id)) store.toggleChapterBookmark(chapter.id)
                            else bookmarkFolderOpen = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (store.isChapterBookmarked(chapter.id)) Icons.Default.Star else Icons.Default.StarBorder, null)
                        Spacer(Modifier.width(7.dp))
                        Text(if (store.isChapterBookmarked(chapter.id)) "Убрать из закладок" else "В закладки")
                    }
                }

                if (topics.isNotEmpty() || remedies.isNotEmpty()) {
                    item(key = "related-title") {
                        Spacer(Modifier.height(22.dp))
                        Text("Связанные материалы", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(count = topics.size, key = { "topic-${topics[it].id}" }) { i ->
                        SuggestionChip(
                            onClick = { navigate(Route.TopicDetail(topics[i].id)) },
                            label = { Text(topics[i].title) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(count = remedies.size, key = { "remedy-${remedies[it].id}" }) { i ->
                        SuggestionChip(
                            onClick = { navigate(Route.RemedyDetail(remedies[i].id)) },
                            label = { Text(remedies[i].title) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item(key = "nav") {
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val prev = book.chapters.firstOrNull { it.id == chapter.previousId }
                        val next = book.chapters.firstOrNull { it.id == chapter.nextId }
                        OutlinedButton(
                            onClick = { if (prev != null) navigate(Route.Reader(prev.id)) },
                            enabled = prev != null,
                            modifier = Modifier.weight(1f)
                        ) { Icon(Icons.Default.ChevronLeft, null); Text("Предыдущая") }
                        Button(
                            onClick = { if (next != null) navigate(Route.Reader(next.id)) },
                            enabled = next != null,
                            modifier = Modifier.weight(1f)
                        ) { Text("Следующая"); Icon(Icons.Default.ChevronRight, null) }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { settingsOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text("Aa", fontFamily = WebSerifFont, fontWeight = FontWeight.Bold)
        }
    }

    if (settingsOpen) {
        ModalBottomSheet(onDismissRequest = { settingsOpen = false }) {
            WebReaderSettingsSheet(store = store, onDone = { settingsOpen = false })
        }
    }

    if (bookmarkFolderOpen) {
        AlertDialog(
            onDismissRequest = { bookmarkFolderOpen = false },
            title = { Text("Сохранить в папку") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    store.bookmarkFolders.forEach { folder ->
                        TextButton(
                            onClick = {
                                store.toggleChapterBookmark(chapter.id, folder)
                                bookmarkFolderOpen = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, null)
                            Spacer(Modifier.width(8.dp))
                            Text(folder, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { bookmarkFolderOpen = false }) { Text("Отмена") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderBlock(chapter: Chapter, block: BookBlock, store: AppStore) {
    val settings = store.settings
    var hadithSourceOpen by remember(block.id) { mutableStateOf(false) }
    val label = when (block.type) {
        "quran" -> "Коран"
        "hadith" -> "Хадис"
        "historical_note" -> if (settings.showHistoricalLabels) "Медицина эпохи" else null
        else -> null
    }

    if (block.type == "subheading") {
        Spacer(Modifier.height(12.dp))
        Text(
            block.text,
            fontFamily = readerComposeFontFamily(settings.fontFamily),
            fontSize = (settings.fontSizeSp + 5).sp,
            lineHeight = ((settings.fontSizeSp + 5) * 1.12f).sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(5.dp))
        return
    }

    if (block.type == "hadith") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 7.dp),
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "ХАДИС",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                )
                Spacer(Modifier.height(8.dp))
                SelectableNativeText(chapter, block, store)
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                    thickness = 1.dp,
                )
                val sourceText = if (block.hadithSources.isEmpty()) {
                    "Требует ручного тахриджа"
                } else {
                    block.hadithSources.joinToString(" · ")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hadithSourceOpen = true }
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Источник / Тахридж",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = WebSansFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    )
                    Text(
                        " · $sourceText",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = WebSansFont,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text("›", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                }
            }
        }

        if (hadithSourceOpen) {
            ModalBottomSheet(onDismissRequest = { hadithSourceOpen = false }) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 690.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        "Источник хадиса",
                        fontFamily = WebSerifFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                    )
                    Text(
                        block.text,
                        fontFamily = readerComposeFontFamily(settings.fontFamily),
                        fontSize = 14.sp,
                        lineHeight = (14f * 1.45f).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.CircleShape,
                    ) {
                        Text(
                            if (block.hadithSources.isEmpty()) "Источник не указан" else block.hadithSources.joinToString(" · "),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            fontFamily = WebSansFont,
                            fontSize = 11.sp,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "Формулировка исходного текста:",
                            fontFamily = WebSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                        Text(
                            if (block.hadithSources.isEmpty()) {
                                "В данных книги источник для этого фрагмента не указан."
                            } else {
                                "В тексте книги указан источник: " + block.hadithSources.joinToString(" · ")
                            },
                            fontFamily = WebSansFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    ) {
                        Row(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .heightIn(min = 92.dp)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                "Степень достоверности хадиса здесь автоматически не присваивается. Для проверенного статуса нужен ручной тахридж по первоисточнику.",
                                modifier = Modifier.padding(14.dp),
                                fontFamily = WebSansFont,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { hadithSourceOpen = false },
                        modifier = Modifier.align(Alignment.End),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
        return
    }

    val isSpecial = block.type == "quran" || block.type == "historical_note"
    if (isSpecial) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(13.dp)) {
                if (label != null) {
                    Text(
                        label.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                }
                SelectableNativeText(chapter, block, store)
            }
        }
    } else {
        Box(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
            SelectableNativeText(chapter, block, store)
        }
    }
}

@Composable
private fun SelectableNativeText(chapter: Chapter, block: BookBlock, store: AppStore) {
    val settings = store.settings
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f).toArgb()
    val highlights = store.highlightsFor(chapter.id, block.id)
    val notes = store.notesFor(chapter.id, block.id)

    val styledText = remember(block.text, highlights, notes, highlightColor) {
        SpannableString(block.text).also { span ->
            highlights.forEach { h ->
                val start = h.start.coerceIn(0, block.text.length)
                val end = h.end.coerceIn(start, block.text.length)
                if (end > start) {
                    span.setSpan(
                        BackgroundColorSpan(highlightColor),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
            }
            notes.forEach { n ->
                val start = n.start.coerceIn(0, block.text.length)
                val end = n.end.coerceIn(start, block.text.length)
                if (end > start) {
                    span.setSpan(
                        UnderlineSpan(),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
            }
        }
    }

    val renderKey = remember(
        block.id,
        settings.fontSizeSp,
        settings.lineSpacing,
        settings.fontFamily,
        textColor,
        highlightColor,
        highlights,
        notes,
    ) {
        listOf(
            block.id,
            settings.fontSizeSp,
            settings.lineSpacing,
            settings.fontFamily,
            textColor,
            highlightColor,
            highlights.hashCode(),
            notes.hashCode(),
        ).joinToString("|")
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextIsSelectable(true)
                setTextColor(textColor)
                setPadding(0, 0, 0, 0)
                includeFontPadding = false
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
            }
        },
        onReset = { tv ->
            tv.text = ""
            tv.tag = null
            tv.customSelectionActionModeCallback = null
        },
        update = { tv ->
            if (tv.tag != renderKey) {
                tv.setTextColor(textColor)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.fontSizeSp)
                tv.setLineSpacing(0f, settings.lineSpacing)
                tv.typeface = readerTypeface(tv.context, settings.fontFamily)

                tv.customSelectionActionModeCallback = object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        menu?.clear()
                        menu?.add(0, 9101, 0, "Выделить")
                        menu?.add(0, 9102, 1, "Заметка")
                        menu?.add(0, 9103, 2, "Снять выделение")
                        return true
                    }

                    override fun onPrepareActionMode(
                        mode: ActionMode?,
                        menu: Menu?,
                    ) = false

                    override fun onActionItemClicked(
                        mode: ActionMode?,
                        item: MenuItem?,
                    ): Boolean {
                        val start = tv.selectionStart.coerceAtLeast(0)
                        val end = tv.selectionEnd.coerceAtLeast(0)
                        if (end <= start) return false

                        val selected = block.text.substring(
                            start.coerceAtMost(block.text.length),
                            end.coerceAtMost(block.text.length),
                        )

                        return when (item?.itemId) {
                            9101 -> {
                                store.addHighlight(
                                    chapter.id,
                                    block.id,
                                    start,
                                    end,
                                )
                                mode?.finish()
                                true
                            }
                            9102 -> {
                                val input = EditText(tv.context).apply {
                                    hint = "Ваша заметка"
                                    setPadding(28, 18, 28, 18)
                                }
                                AlertDialog.Builder(tv.context)
                                    .setTitle("Заметка к выделению")
                                    .setMessage("«$selected»")
                                    .setView(input)
                                    .setPositiveButton("Сохранить") { _, _ ->
                                        store.addNote(
                                            chapter.id,
                                            block.id,
                                            start,
                                            end,
                                            selected,
                                            input.text.toString(),
                                        )
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                                mode?.finish()
                                true
                            }
                            9103 -> {
                                store.removeHighlights(
                                    chapter.id,
                                    block.id,
                                    start,
                                    end,
                                )
                                mode?.finish()
                                true
                            }
                            else -> false
                        }
                    }

                    override fun onDestroyActionMode(mode: ActionMode?) = Unit
                }

                tv.text = styledText
                tv.tag = renderKey
            }
        },
    )
}

@Composable
fun SettingsScreen(store: AppStore, modifier: Modifier, back: () -> Unit) {
    Column(modifier.fillMaxSize()) {
        PageHeader("Настройки чтения", null, back)
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReaderSettingsContent(store = store, compact = false)
            OutlinedButton(onClick = { store.clearProgress() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.RestartAlt, null)
                Spacer(Modifier.width(7.dp))
                Text("Сбросить прогресс и историю")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsContent(store: AppStore, compact: Boolean) {
    val s = store.settings

    Text("Предпросмотр", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(15.dp)) {
            Text("Руководство Пророка ﷺ в лечении", fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            NativePreviewText(settings = s)
        }
    }

    Text("Шрифт", fontWeight = FontWeight.Bold)
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FontChoice("Literata", "classic", s.fontFamily, Modifier.weight(1f)) { store.updateSettings { it.copy(fontFamily = "classic") } }
            FontChoice("PT Serif", "book", s.fontFamily, Modifier.weight(1f)) { store.updateSettings { it.copy(fontFamily = "book") } }
            FontChoice("Inter", "modern", s.fontFamily, Modifier.weight(1f)) { store.updateSettings { it.copy(fontFamily = "modern") } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FontChoice("Manrope", "compact", s.fontFamily, Modifier.weight(1f)) { store.updateSettings { it.copy(fontFamily = "compact") } }
            FontChoice("Android", "system", s.fontFamily, Modifier.weight(1f)) { store.updateSettings { it.copy(fontFamily = "system") } }
            FontChoice("Serif", "system_serif", s.fontFamily, Modifier.weight(1f)) { store.updateSettings { it.copy(fontFamily = "system_serif") } }
        }
    }

    Text("Размер текста", fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { store.updateSettings { it.copy(fontSizeSp = (it.fontSizeSp - 1).coerceAtLeast(13f)) } }) { Text("A−") }
        Text("${s.fontSizeSp.toInt()} sp", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        OutlinedButton(onClick = { store.updateSettings { it.copy(fontSizeSp = (it.fontSizeSp + 1).coerceAtMost(24f)) } }) { Text("A+") }
    }

    Text("Межстрочный интервал", fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            1.08f to "Минимальный",
            1.14f to "Очень плотный",
            1.20f to "Плотный",
            1.28f to "Компактный",
            1.38f to "Обычный",
            1.48f to "Свободный",
        ).forEach { (value, label) ->
            FilterChip(
                selected = kotlin.math.abs(s.lineSpacing - value) < 0.05f,
                onClick = { store.updateSettings { it.copy(lineSpacing = value) } },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (!compact) {
        Text("Тема", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("system" to "Система", "light" to "Светлая", "dark" to "Тёмная").forEach { (value, label) ->
                FilterChip(
                    selected = s.theme == value,
                    onClick = { store.updateSettings { it.copy(theme = value) } },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        ListItem(
            headlineContent = { Text("Метки «Медицина эпохи»") },
            supportingContent = { Text("Текст не скрывается; меняется только редакционная метка") },
            trailingContent = {
                Switch(
                    checked = s.showHistoricalLabels,
                    onCheckedChange = { checked -> store.updateSettings { it.copy(showHistoricalLabels = checked) } }
                )
            }
        )
    }
}

@Composable
private fun FontChoice(label: String, value: String, selected: String, modifier: Modifier, onClick: () -> Unit) {
    val family = readerComposeFontFamily(value)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        colors = if (selected == value) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors(),
        contentPadding = PaddingValues(7.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Аб", fontFamily = family, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NativePreviewText(settings: ReaderSettings) {
    val color = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context -> TextView(context).apply { includeFontPadding = false } },
        update = { tv ->
            tv.text = "Полезное знание требует спокойного и внимательного чтения. Здесь сразу видно выбранный шрифт, размер и интервал."
            tv.setTextColor(color)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.fontSizeSp)
            tv.setLineSpacing(0f, settings.lineSpacing)
            tv.typeface = readerTypeface(tv.context, settings.fontFamily)
        }
    )
}
