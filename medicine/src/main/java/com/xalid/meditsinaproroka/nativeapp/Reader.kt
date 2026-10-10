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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.distinctUntilChanged

/** Semantic decoration only; chapter text and stored selection indices are unchanged. */
private fun attributedScholarParagraph(text: String): Boolean {
    val start = text.trimStart().lowercase()
    val scholar = listOf("ибн ", "абу ", "аль-", "ал-", "муджахид ", "хасан ", "шейх ").any(start::startsWith)
    if (!scholar) return false
    val prefix = start.take(135)
    val attribution = Regex("""\b(сказал|говорил|писал|объяснял|отмечал|считает|полагал|утверждал|подчёркивал)\b""").containsMatchIn(prefix)
    val chain = Regex("""\b(передал|передаёт|приводит|сообщил|передавал)\b""").containsMatchIn(prefix)
    return attribution && !chain
}

private fun numberedOpening(text: String): Int? {
    val digits = Regex("""^\s*([1-9])[.)]\s""").find(text)
    if (digits != null) return digits.groupValues[1].toIntOrNull()
    val words = Regex("""^\s*(Первое|Второе|Третье|Четвёртое|Пятое|Первый|Второй|Третий|Четвёртый)(?=[:.\s—–-])""", RegexOption.IGNORE_CASE)
        .find(text)?.groupValues?.get(1)?.lowercase() ?: return null
    return when (words) {
        "первое", "первый" -> 1
        "второе", "второй" -> 2
        "третье", "третий" -> 3
        "четвёртое", "четвёртый" -> 4
        "пятое" -> 5
        else -> null
    }
}

@Composable
private fun SemanticReaderHeading(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        Text(
            label, color = MaterialTheme.colorScheme.primary,
            fontFamily = WebSansFont, fontSize = 12.sp,
            lineHeight = 15.sp, fontWeight = FontWeight.SemiBold,
        )
    }
}


/**
 * Some source text blocks break in the middle of a sentence. Join only the
 * unmistakable continuations, leaving all source blocks/IDs untouched.
 * Section headings, Quran, hadith, enumerations and citations stay separate.
 */
/**
 * Single source of truth for the paper variants, shared by both the actual
 * reading page and the miniature previews in reading settings.
 * A colour wash changes the tone but never edits/replaces the source texture.
 */
internal fun readerPaperOverlay(paperBackground: String): Color? = when (paperBackground) {
    "original" -> null
    "sage" -> Color(0xFFDDE9DA).copy(alpha = 0.36f)
    // 6.5% was visually indistinguishable from the original. 42% gives a
    // visibly brighter ivory sheet, retaining the original aged grain/edges.
    else -> Color(0xFFFFFCF6).copy(alpha = 0.42f)
}

private fun joinsUnfinishedSentence(previous: BookBlock, next: BookBlock): Boolean {
    if (previous.type != "text" || next.type != "text") return false
    if (attributedScholarParagraph(previous.text) || attributedScholarParagraph(next.text)) return false
    if (numberedOpening(previous.text) != null || numberedOpening(next.text) != null) return false
    val tail = previous.text.trimEnd().lastOrNull() ?: return false
    if (tail in ".!?…؟؛»”\"") return false
    val firstLetter = next.text.trimStart().firstOrNull { it.isLetter() } ?: return false
    return firstLetter.isLowerCase()
}

private data class ReaderTextSegment(val block: BookBlock, val start: Int) {
    val end: Int get() = start + block.text.length
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: BookData,
    store: AppStore,
    route: Route.Reader,
    modifier: Modifier,
    back: () -> Unit,
    animateEntrance: Boolean = false,
    navigate: (Route) -> Unit,
) {
    // Section-only records are not reading chapters. Resolve old bookmarks or
    // navigation pointing at an empty divider to the next real chapter.
    val requestedChapter = book.chapters.firstOrNull { it.id == route.chapterId }
    val chapter = if (requestedChapter != null && requestedChapter.blocks.isEmpty()) {
        book.chapters.firstOrNull { it.order > requestedChapter.order && it.blocks.isNotEmpty() }
            ?: requestedChapter
    } else requestedChapter
    if (chapter == null) {
        Column(modifier.fillMaxSize()) {
            PageHeader("Глава не найдена", null, back)
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Не удалось открыть главу")
            }
        }
        return
    }

    val scrollState = rememberLazyListState()
    // Keep one layout coordinate for every original block so progress,
    // anchors, bookmarks and resume positions continue to work.
    val paragraphGroupStart = remember(chapter.id, chapter.blocks) {
        IntArray(chapter.blocks.size) { it }.also { first ->
            for (index in 1 until chapter.blocks.size) {
                if (joinsUnfinishedSentence(chapter.blocks[index - 1], chapter.blocks[index])) {
                    first[index] = first[index - 1]
                }
            }
        }
    }
    // Compose only paragraphs near the viewport. Original source blocks remain
    // unchanged so selection, notes and highlights retain their exact IDs.
    val groupStarts = remember(chapter.id, paragraphGroupStart) {
        chapter.blocks.indices.filter { paragraphGroupStart[it] == it }
    }
    val groupIndexForBlock = remember(chapter.id, paragraphGroupStart) {
        IntArray(chapter.blocks.size).also { result ->
            var group = -1
            for (index in chapter.blocks.indices) {
                if (paragraphGroupStart[index] == index) group++
                result[index] = group.coerceAtLeast(0)
            }
        }
    }
    var initialJumpDone by rememberSaveable(chapter.id, route.anchor, route.resume) {
        mutableStateOf(false)
    }
    val foregroundOpacity = remember(chapter.id, animateEntrance) {
        // Almost imperceptible finish; do not reveal any intermediary surface.
        Animatable(if (animateEntrance) 0.90f else 1f)
    }
    LaunchedEffect(chapter.id, animateEntrance) {
        if (animateEntrance) {
            foregroundOpacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
            )
        }
    }
    // Chapter turns follow source order and ignore empty book-section headings.
    val previousChapter = remember(book, chapter.id) {
        book.chapters.lastOrNull { it.order < chapter.order && it.blocks.isNotEmpty() }
    }
    val followingChapter = remember(book, chapter.id) {
        book.chapters.firstOrNull { it.order > chapter.order && it.blocks.isNotEmpty() }
    }
    var settingsOpen by remember { mutableStateOf(false) }
    var bookmarkFolderOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val topics = remember(book, chapter) {
        chapter.topics.mapNotNull { id ->
            book.topics.firstOrNull { it.id == id }
        }
    }
    val remedies = remember(book, chapter) {
        chapter.remedies.mapNotNull { id ->
            book.remedies.firstOrNull { it.id == id }
        }
    }

    fun visibleBlockIndex(): Int {
        if (chapter.blocks.isEmpty()) return 0
        val row = scrollState.firstVisibleItemIndex - 1
        return when {
            row < 0 -> 0
            row >= groupStarts.size -> chapter.blocks.lastIndex
            else -> groupStarts[row]
        }
    }

    val targetBlock = remember(
        chapter.id,
        route.anchor,
        route.resume,
    ) {
        when {
            chapter.blocks.isEmpty() -> 0
            route.anchor != null ->
                chapter.blocks
                    .indexOfFirst { it.anchor == route.anchor }
                    .takeIf { it >= 0 }
                    ?: 0
            route.resume ->
                (store.progress[chapter.id]
                    ?: if (store.lastChapterId == chapter.id) {
                        store.lastBlockIndex
                    } else {
                        0
                    }).coerceIn(0, chapter.blocks.lastIndex)
            else -> 0
        }
    }

    LaunchedEffect(chapter.id, route.anchor) {
        store.addHistory(chapter.id, route.anchor)
    }

    LaunchedEffect(chapter.id, route.anchor, route.resume, targetBlock) {
        if (!initialJumpDone) {
            if ((route.anchor != null || route.resume) && chapter.blocks.isNotEmpty()) {
                // Heading is row 0; each paragraph group follows.
                scrollState.scrollToItem(groupIndexForBlock[targetBlock] + 1)
            } else {
                scrollState.scrollToItem(0)
            }
            initialJumpDone = true
        }
    }

    LaunchedEffect(chapter.id, scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (!scrolling && initialJumpDone) {
                    store.setLastPosition(chapter.id, visibleBlockIndex())
                }
            }
    }

    DisposableEffect(chapter.id, scrollState) {
        onDispose {
            if (initialJumpDone) {
                store.setLastPosition(chapter.id, visibleBlockIndex())
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        if (MaterialTheme.colorScheme.background.red >= 0.25f) {
            // Keep the original antique paper asset unchanged. This very
            // subtle translucent wash raises its brightness without replacing
            // any grain, cracks, edges or the underlying paper image.
            Image(
                painter = painterResource(R.drawable.reader_parchment_source),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
            )
            // A stronger visible light-paper option; exact same bitmap.
            readerPaperOverlay(store.settings.paperBackground)?.let { wash ->
                Box(Modifier.matchParentSize().background(wash))
            }
        }
        // The page, status-bar area and transparent toolbar stay fixed and
        // fully visible. Fade only the actual chapter content below the header.
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.statusBarsPadding()) {
                PageHeader(
                    "Медицина Пророка ﷺ",
                    "Глава ${chapter.order} из ${book.chapters.size}",
                    back,
                    settings = { settingsOpen = true },
                    compact = true,
                )
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = foregroundOpacity.value }
                    .clipToBounds()
                    // Gestures in the reading body turn CHAPTERS, never
                    // navigate back to the contents. Android keeps its native
                    // Back gesture at the left/right screen edges.
                    .pointerInput(chapter.id, previousChapter?.id, followingChapter?.id) {
                        var horizontalDistance = 0f
                        var startedAtSystemEdge = false
                        val pageThresholdPx = 76.dp.toPx()
                        val reservedSystemEdgePx = 38.dp.toPx()
                        detectHorizontalDragGestures(
                            onDragStart = { point ->
                                horizontalDistance = 0f
                                startedAtSystemEdge =
                                    point.x < reservedSystemEdgePx ||
                                        point.x > size.width - reservedSystemEdgePx
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (!startedAtSystemEdge) {
                                    horizontalDistance += dragAmount
                                    change.consume()
                                }
                            },
                            onDragCancel = {
                                horizontalDistance = 0f
                                startedAtSystemEdge = false
                            },
                            onDragEnd = {
                                val distance = horizontalDistance
                                val isSystemEdge = startedAtSystemEdge
                                horizontalDistance = 0f
                                startedAtSystemEdge = false
                                if (!isSystemEdge) {
                                    val destination = when {
                                        distance <= -pageThresholdPx -> followingChapter
                                        distance >= pageThresholdPx -> previousChapter
                                        else -> null
                                    }
                                    if (destination != null) {
                                        navigate(Route.Reader(destination.id))
                                    }
                                }
                            },
                        )
                    }
                    ,
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 5.dp, bottom = 112.dp,
                ),
            ) {
                item(key = "chapter-header") {
                    Column {
                Text(
                    chapter.section,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = WebSansFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    chapter.title.removeSurrounding("[", "]"),
                    fontFamily = WebLiterataFont,
                    fontSize = (if (chapter.title.length > 60) 23 else 27).sp,
                    lineHeight = (if (chapter.title.length > 60) 28 else 32).sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline
                        .copy(alpha = 0.72f)
                )
                Spacer(Modifier.height(7.dp))

                    }
                }

                items(
                    count = groupStarts.size,
                    key = { row -> "paragraph:${chapter.id}:${groupStarts[row]}" },
                    contentType = { row -> chapter.blocks[groupStarts[row]].type },
                ) { group ->
                    val index = groupStarts[group]
                    val block = chapter.blocks[index]
                    val end = groupStarts.getOrNull(group + 1) ?: chapter.blocks.size
                    if (end > index + 1) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                            SelectableNativeText(
                                chapter, block, store,
                                mergedBlocks = chapter.blocks.subList(index, end),
                            )
                        }
                    } else {
                        ReaderBlock(chapter = chapter, block = block, store = store)
                    }
                }

                item(key = "chapter-footer") {
                    Column {
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val prev = previousChapter
                    val next = followingChapter

                    OutlinedButton(
                        onClick = {
                            if (prev != null) {
                                navigate(Route.Reader(prev.id))
                            }
                        },
                        enabled = prev != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = null,
                        )
                        Text("Предыдущая")
                    }

                    Button(
                        onClick = {
                            if (next != null) {
                                navigate(Route.Reader(next.id))
                            }
                        },
                        enabled = next != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Следующая")
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            copyToClipboard(
                                context,
                                chapter.title,
                                chapterPlainText(book, chapter),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 10.dp,
                        ),
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Копировать",
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            shareText(
                                context,
                                chapter.title,
                                chapterPlainText(
                                    book,
                                    chapter,
                                    includeSource = true,
                                ),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 10.dp,
                        ),
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Поделиться",
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (store.isChapterBookmarked(chapter.id)) {
                                store.toggleChapterBookmark(chapter.id)
                            } else {
                                bookmarkFolderOpen = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 10.dp,
                        ),
                    ) {
                        Icon(
                            if (store.isChapterBookmarked(chapter.id)) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Закладка",
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                }

                if (topics.isNotEmpty() || remedies.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Связанные материалы",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(8.dp))

                    topics.forEach { topic ->
                        SuggestionChip(
                            onClick = {
                                navigate(Route.TopicDetail(topic.id))
                            },
                            label = { Text(topic.title) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    remedies.forEach { remedy ->
                        SuggestionChip(
                            onClick = {
                                navigate(Route.RemedyDetail(remedy.id))
                            },
                            label = { Text(remedy.title) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                    }
                }
            }
        }

    }

    if (settingsOpen) {
        ModalBottomSheet(
            onDismissRequest = { settingsOpen = false },
            containerColor = if (MaterialTheme.colorScheme.background.red >= 0.25f)
                Color(0xFFF7F0E4) else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            scrimColor = Color(0xFF181914).copy(alpha = 0.36f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
            ),
            dragHandle = {
                Box(
                    Modifier.padding(top = 9.dp, bottom = 6.dp)
                        .size(width = 31.dp, height = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            androidx.compose.foundation.shape.RoundedCornerShape(50),
                        )
                )
            },
        ) {
            WebReaderSettingsSheet(
                store = store,
                onDone = { settingsOpen = false },
            )
        }
    }

    if (bookmarkFolderOpen) {
        AlertDialog(
            onDismissRequest = {
                bookmarkFolderOpen = false
            },
            title = {
                Text("Сохранить в папку")
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp),
                ) {
                    store.bookmarkFolders.forEach { folder ->
                        TextButton(
                            onClick = {
                                store.toggleChapterBookmark(
                                    chapter.id,
                                    folder,
                                )
                                bookmarkFolderOpen = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Folder, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                folder,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        bookmarkFolderOpen = false
                    },
                ) {
                    Text("Отмена")
                }
            },
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
                SemanticReaderHeading(Icons.Outlined.FormatQuote, "ХАДИС")
                Spacer(Modifier.height(6.dp))
                SelectableNativeText(chapter, block, store)
                Spacer(Modifier.height(5.dp))
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

    val isQuran = block.type == "quran"
    val isHistorical = block.type == "historical_note"
    val isScholar = block.type == "text" && attributedScholarParagraph(block.text)
    val listNumber = if (block.type == "text" && !isScholar) numberedOpening(block.text) else null

    when {
        isQuran || isHistorical || isScholar -> {
            val icon = when {
                isQuran -> Icons.Outlined.MenuBook
                isHistorical -> Icons.Outlined.MedicalServices
                else -> Icons.Outlined.PersonOutline
            }
            val heading = when {
                isQuran -> "КОРАН"
                isHistorical -> if (settings.showHistoricalLabels) "МЕДИЦИНА ЭПОХИ" else null
                else -> "ВЫСКАЗЫВАНИЕ"
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 6.dp),
            ) {
                Box(
                    Modifier.width(3.dp).fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f))
                )
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    if (heading != null) {
                        SemanticReaderHeading(icon, heading)
                        Spacer(Modifier.height(6.dp))
                    } else {
                        Icon(
                            icon, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.height(5.dp))
                    }
                    SelectableNativeText(chapter, block, store)
                    if (isQuran) {
                        block.quranReference?.takeIf { it.isNotBlank() }?.let { reference ->
                            Spacer(Modifier.height(5.dp))
                            Text(
                                reference.trim(),
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = WebSansFont,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                            )
                        }
                    }
                }
            }
        }
        listNumber != null -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier.size(34.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            androidx.compose.foundation.shape.CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        listNumber.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = WebSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    SelectableNativeText(chapter, block, store)
                }
            }
        }
        else -> {
            Box(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                SelectableNativeText(chapter, block, store)
            }
        }
    }
}

@Composable
private fun SelectableNativeText(
    chapter: Chapter,
    block: BookBlock,
    store: AppStore,
    textColorOverride: Color? = null,
    mergedBlocks: List<BookBlock> = listOf(block),
) {
    val settings = store.settings
    val textColor = (textColorOverride ?: MaterialTheme.colorScheme.onBackground).toArgb()
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f).toArgb()

    // A visual paragraph may contain multiple original blocks. Each range
    // always maps back to its own persisted block ID and character offsets.
    val segments = remember(mergedBlocks) {
        var cursor = 0
        mergedBlocks.map { source ->
            ReaderTextSegment(source, cursor).also {
                cursor += source.text.length + 1
            }
        }
    }
    val displayText = remember(mergedBlocks) {
        mergedBlocks.joinToString(" ") { it.text }
    }
    val highlights = segments.flatMap { segment ->
        store.highlightsFor(chapter.id, segment.block.id).mapNotNull { h ->
            val from = h.start.coerceIn(0, segment.block.text.length)
            val to = h.end.coerceIn(from, segment.block.text.length)
            if (to > from) (segment.start + from) to (segment.start + to) else null
        }
    }
    val notes = segments.flatMap { segment ->
        store.notesFor(chapter.id, segment.block.id).mapNotNull { note ->
            val from = note.start.coerceIn(0, segment.block.text.length)
            val to = note.end.coerceIn(from, segment.block.text.length)
            if (to > from) (segment.start + from) to (segment.start + to) else null
        }
    }

    val styledText = remember(displayText, highlights, notes, highlightColor) {
        SpannableString(displayText).also { span ->
            highlights.forEach { (from, to) ->
                span.setSpan(
                    BackgroundColorSpan(highlightColor),
                    from,
                    to,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            notes.forEach { (from, to) ->
                span.setSpan(UnderlineSpan(), from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    val renderKey = remember(
        displayText,
        settings.fontSizeSp,
        settings.lineSpacing,
        settings.fontFamily,
        textColor,
        highlightColor,
        highlights,
        notes,
    ) {
        listOf(
            mergedBlocks.joinToString(",") { it.id },
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
                        val start = tv.selectionStart.coerceIn(0, displayText.length)
                        val end = tv.selectionEnd.coerceIn(0, displayText.length)
                        if (end <= start) return false

                        val affected = segments.mapNotNull { segment ->
                            val relativeStart = (start - segment.start)
                                .coerceIn(0, segment.block.text.length)
                            val relativeEnd = (end - segment.start)
                                .coerceIn(0, segment.block.text.length)
                            if (relativeEnd > relativeStart) {
                                Triple(segment.block, relativeStart, relativeEnd)
                            } else null
                        }
                        if (affected.isEmpty()) return false
                        val selected = displayText.substring(start, end)

                        return when (item?.itemId) {
                            9101 -> {
                                affected.forEach { (source, from, to) ->
                                    store.addHighlight(chapter.id, source.id, from, to)
                                }
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
                                        affected.forEach { (source, from, to) ->
                                            store.addNote(
                                                chapter.id,
                                                source.id,
                                                from,
                                                to,
                                                source.text.substring(from, to),
                                                input.text.toString(),
                                            )
                                        }
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                                mode?.finish()
                                true
                            }
                            9103 -> {
                                affected.forEach { (source, from, to) ->
                                    store.removeHighlights(chapter.id, source.id, from, to)
                                }
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
