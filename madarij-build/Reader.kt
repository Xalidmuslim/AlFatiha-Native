package ru.madarij.nativeapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.madarij.nativeapp.data.*
import ru.madarij.nativeapp.data.Paragraph
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun Reader(
    vm: BookViewModel, chapter: Chapter, settings: ReadingSettings, anchor: String? = null, query: String = "",
    onStudy: (String) -> Unit = {}, onOpenParagraph: (String) -> Unit = {}, onBack: () -> Unit = {}, navigate: (String) -> Unit
) {
    val context = LocalContext.current
    val actionScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val read by vm.read.collectAsStateWithLifecycle()
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    val notesFlow = remember(chapter.id) { vm.repository.dao.notes(chapter.id) }
    val notes by notesFlow.collectAsStateWithLifecycle(emptyList())
    val groups = rememberStructure()
    val chapterGroup = groups.find { g -> g.items.any { it.chapterId == chapter.id } }
    val chapterStructure = chapterGroup?.items?.find { it.chapterId == chapter.id }
    val topicsByParagraph = remember(chapterStructure) { chapterStructure?.topics?.associateBy { it.paragraphId }.orEmpty() }
    val learning = remember { LearningRepository(context.applicationContext) }
    var paragraphs by remember(chapter.id) { mutableStateOf<List<Paragraph>>(emptyList()) }
    var loaded by remember(chapter.id) { mutableStateOf(false) }
    var ready by remember(chapter.id) { mutableStateOf(false) }
    var anchorConsumed by rememberSaveable(chapter.id, anchor) { mutableStateOf(false) }
    var matchRevealed by rememberSaveable(chapter.id, anchor, query) { mutableStateOf(false) }
    var loadingError by remember(chapter.id) { mutableStateOf("") }
    var retry by remember(chapter.id) { mutableIntStateOf(0) }
    var panel by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var actionsMode by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Paragraph?>(null) }
    var noteDialog by remember { mutableStateOf(false) }
    var noteAnchor by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var bookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkAnchor by remember { mutableStateOf<String?>(null) }
    var bookmarkTitle by remember { mutableStateOf("") }
    var bookmarkNote by remember { mutableStateOf("") }
    var sourceDialog by remember { mutableStateOf(false) }
    var termDialog by remember { mutableStateOf<StudyTerm?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var message by remember { mutableStateOf("") }
    var activeSince by remember(chapter.id) { mutableLongStateOf(0L) }
    val visible = remember(paragraphs, settings.showNotes, anchor) { paragraphs.filter { settings.showNotes || it.id == anchor || it.role !in listOf("editor_note", "edition_note") } }
    val currentParagraph by remember(visible) { derivedStateOf { if (visible.isEmpty()) null else visible[(listState.firstVisibleItemIndex - 1).coerceIn(0, visible.lastIndex)] } }
    fun savePosition() {
        if (ready) currentParagraph?.let { vm.savePosition(chapter.id, it.id, if (listState.firstVisibleItemIndex !in 1..visible.size) 0 else listState.firstVisibleItemScrollOffset) }
    }
    fun recordTime() {
        if (activeSince > 0) {
            val now = SystemClock.elapsedRealtime()
            val seconds = ((now - activeSince) / 1000).toInt()
            if (seconds > 0 && ready) currentParagraph?.let { vm.recordReading(chapter.id, it.id, seconds) }
            activeSince = now
        }
    }
    LaunchedEffect(chapter.id, retry) {
        try { paragraphs = vm.repository.dao.paragraphs(chapter.id); loaded = true; loadingError = "" }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (_: Exception) { loadingError = "Не удалось открыть текст. Повторите загрузку." }
    }
    LaunchedEffect(chapter.id, loaded, anchor) {
        if (loaded && visible.isNotEmpty()) {
            val old = vm.repository.dao.position(chapter.id)
            val target = if (anchor != null && !anchorConsumed) anchor else old?.paragraphId
            val found = visible.indexOfFirst { it.id == target }
            if (found >= 0) listState.scrollToItem(found + 1, if (target == anchor && !anchorConsumed) 0 else old?.offset ?: 0)
            anchorConsumed = true
            ready = true
        }
    }
    LaunchedEffect(ready, visible) {
        if (ready) snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged().debounce(400).collect { savePosition() }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(chapter.id, ready, visible, lifecycle) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) activeSince = SystemClock.elapsedRealtime()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> activeSince = SystemClock.elapsedRealtime()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> { savePosition(); recordTime(); activeSince = 0L }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { savePosition(); recordTime(); activeSince = 0L; lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(chapter.id, ready) { if (ready) while (true) { delay(30_000); recordTime() } }
    LaunchedEffect(message) { if (message.isNotEmpty()) { snackbar.showSnackbar(message); message = "" } }
    fun textFor(p: Paragraph): String = (when (p.role) { "editor_note" -> "[Пояснение редакции приложения]\n"; "edition_note" -> "[Примечание издания]\n"; else -> "" }) + (if (settings.showArabic && p.ar.isNotBlank()) p.ar + "\n\n" else "") + p.ru
    fun fullChapter() = "Степени идущих — Мадаридж ас-саликин\nИбн аль-Каййим\nТом ${chapter.volume} · ${chapter.title}\nЛитературная сверка завершена · проверка источников отдельно\n\n" + paragraphs.joinToString("\n\n") { textFor(it) }
    fun copy(text: String) { (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(chapter.title, text)); message = "Текст скопирован" }
    fun share(text: String) { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, chapter.title); putExtra(Intent.EXTRA_TEXT, text) }, "Поделиться текстом")) }
    fun newBookmark(paragraph: Paragraph?) { bookmarkAnchor = paragraph?.id; val existing=bookmarks.find {it.chapterId==chapter.id && it.paragraphId==paragraph?.id};bookmarkTitle = existing?.title ?: chapter.title; bookmarkNote = existing?.note.orEmpty(); bookmarkDialog = true }
    fun newNote(paragraph: Paragraph?) { noteAnchor = paragraph?.id; noteText = ""; noteDialog = true }
    val progress by remember(visible) { derivedStateOf { if (visible.isEmpty()) 0 else ((listState.firstVisibleItemIndex.toFloat() / visible.size) * 100).toInt().coerceIn(0, 100) } }
    val chapterIndex = chapters.indexOfFirst { it.id == chapter.id }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val backGestureInset = with(density) { 48.dp.toPx() }
    val swipeThreshold = with(density) { 72.dp.toPx() }
    val swipeLockDistance = with(density) { 12.dp.toPx() }
    var swipeOffset by remember(chapter.id) { mutableFloatStateOf(0f) }
    val reduceMotion = settings.reducedMotion || !android.animation.ValueAnimator.areAnimatorsEnabled()
    val swipeModifier = Modifier
        .graphicsLayer { translationX = swipeOffset }
        .pointerInput(chapter.id, chapters, backGestureInset, swipeThreshold, swipeLockDistance, reduceMotion) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var totalX = 0f
                var totalY = 0f
                var horizontalLocked = false
                var verticalLocked = false
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    val delta = change.position - change.previousPosition
                    totalX += delta.x
                    totalY += delta.y

                    if (!horizontalLocked && !verticalLocked && (abs(totalX) > swipeLockDistance || abs(totalY) > swipeLockDistance)) {
                        val systemBackEdge = down.position.x <= backGestureInset && totalX > 0f
                        verticalLocked = abs(totalY) > abs(totalX) * 1.15f || systemBackEdge
                        horizontalLocked = !verticalLocked && abs(totalX) > abs(totalY) * 1.15f
                    }

                    if (horizontalLocked) {
                        change.consume()
                        val maxOffset = size.width * 0.34f
                        swipeOffset = totalX.coerceIn(-maxOffset, maxOffset)
                    }
                }

                if (horizontalLocked) {
                    val direction = classifyReaderSwipe(
                        down.position.x, size.width.toFloat(), totalX, totalY,
                        backGestureInset, swipeThreshold
                    )
                    val targetIndex = when (direction) {
                        ReaderPageSwipe.NEXT -> chapterIndex + 1
                        ReaderPageSwipe.PREVIOUS -> chapterIndex - 1
                        null -> -1
                    }
                    val target = chapters.getOrNull(targetIndex)
                    if (direction != null && target != null) {
                        savePosition()
                        recordTime()
                        if (!reduceMotion) {
                            val startOffset = swipeOffset
                            val endOffset = if (direction == ReaderPageSwipe.NEXT) -size.width.toFloat() else size.width.toFloat()
                            actionScope.launch {
                                animate(
                                    initialValue = startOffset,
                                    targetValue = endOffset,
                                    animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing)
                                ) { value, _ -> swipeOffset = value }
                                swipeOffset = 0f
                                navigate(target.id)
                            }
                        } else {
                            swipeOffset = 0f
                            navigate(target.id)
                        }
                    } else if (!reduceMotion) {
                        val startOffset = swipeOffset
                        actionScope.launch {
                            animate(
                                initialValue = startOffset,
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.86f, stiffness = 520f)
                            ) { value, _ -> swipeOffset = value }
                        }
                    } else {
                        swipeOffset = 0f
                    }
                } else if (swipeOffset != 0f) {
                    swipeOffset = 0f
                }
            }
        }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 58.dp).padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)) {
                Text("Назад", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Степени идущих",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ReaderControl("Аа") { panel = true }
            Box {
                ReaderControl("⋯") { menu = true }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem({ Text("Закладка главы") }, { newBookmark(null); menu = false })
                        DropdownMenuItem({ Text("Заметка к главе") }, { newNote(null); menu = false })
                        DropdownMenuItem({ Text("Повторить раздел через 3 дня") }, {
                            menu = false
                            actionScope.launch {
                                try { learning.scheduleChapterReview(chapter.id); message = "Раздел добавлен на повторение через 3 дня" }
                                catch (e: kotlinx.coroutines.CancellationException) { throw e }
                                catch (_: Exception) { message = "Не удалось назначить повторение. Повторите действие." }
                            }
                        })
                        DropdownMenuItem({ Text(if (actionsMode) "Скрыть действия с абзацами" else "Действия с абзацами") }, { actionsMode = !actionsMode; menu = false })
                        DropdownMenuItem({ Text("Скопировать главу целиком") }, { copy(fullChapter()); menu = false }, enabled = loaded)
                        DropdownMenuItem({ Text("Поделиться главой") }, { share(fullChapter()); menu = false }, enabled = loaded)
                        DropdownMenuItem({ Text("Источник главы") }, { sourceDialog = true; menu = false })
                    }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Том ${chapter.volume} · $progress% прочитано", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (actionsMode) Text("Режим абзацев: закладки, заметки и копирование", Modifier.padding(horizontal = 20.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .14f))
        if (loadingError.isNotEmpty()) { InfoCard("Текст недоступен", loadingError); TextButton(onClick = { retry++ }) { Text("Повторить") } }
        LazyColumn(state = listState, modifier = Modifier.weight(1f).then(swipeModifier), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            item(key = "heading") {
                Column(
                    Modifier.fillMaxWidth(settings.textWidth).padding(top = 4.dp, bottom = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    if (chapterGroup != null) Text(chapterGroup.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        chapterStructure?.title ?: chapter.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 32.sp),
                        fontFamily = russianFamily(settings),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (chapterStructure?.subtitle?.isNotBlank() == true) Text(
                        chapterStructure.subtitle.replace(" · название для навигации", ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (query.isNotBlank()) Text("Найденный текст: «$query»", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .5f))
                }
            }
            items(visible, key = { it.id }) { p ->
                val terms = learning.terms.filter { it.sourceParagraphId == p.id }
                val isNote = p.role in listOf("editor_note", "edition_note")
                var expanded by rememberSaveable(p.id) { mutableStateOf(anchor == p.id && isNote) }
                Column(Modifier.fillMaxWidth(settings.textWidth).combinedClickable(onClick = {}, onLongClick = { selected = p }), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topicsByParagraph[p.id]?.let { topic ->
                        Column(
                            Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                if (topic.provenance == "source_heading") "ТЕМА КНИГИ" else "ТЕМА",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                topic.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = russianFamily(settings),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (isNote) {
                        OutlinedCard(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text((if (p.role == "editor_note") "Пояснение редакции" else "Примечание издания") + (if (expanded) " · Скрыть" else " · Открыть"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                if (expanded) {
                                    if (settings.showArabic && p.ar.isNotBlank()) SelectionContainer { ArabicText(p.ar, settings) }
                                    SelectionContainer { SearchAnchorText(highlight(p.ru, query, MaterialTheme.colorScheme.primary),p.ru,query,readerStyle(settings),ready && p.id==anchor && !matchRevealed) {matchRevealed=true} }
                                }
                            }
                        }
                    } else {
                        val arabicMatch = p.id == anchor && query.isNotBlank() && normalizedText(p.ar).text.contains(normalizedText(query).text) && !normalizedText(p.ru).text.contains(normalizedText(query).text)
                        val arabicBlocks = remember(p.id, p.ar) { splitReaderText(p.ar, "فصل", "فصل") }
                        val russianBlocks = remember(p.id, p.ru) { splitReaderText(p.ru, "Раздел", "Раздел") }
                        if ((settings.showArabic || arabicMatch) && p.ar.isNotBlank()) SelectionContainer {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                arabicBlocks.forEach { block ->
                                    when (block) {
                                        is ReaderTextBlock.Section -> ReaderSectionCard(block, settings, arabic = true)
                                        is ReaderTextBlock.Body -> readerParagraphs(block.text).forEach { paragraph ->
                                            if (arabicMatch) SearchAnchorText(highlight(paragraph, query, MaterialTheme.colorScheme.primary), paragraph, query,
                                                androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(if(settings.arabicFont=="alternate") R.font.arabic_naskh_alt else R.font.naskh)),fontSize=settings.arabicSize.sp,lineHeight=(settings.arabicSize*1.85f).sp,textDirection=androidx.compose.ui.text.style.TextDirection.Rtl,textAlign=androidx.compose.ui.text.style.TextAlign.Right),
                                                ready && !matchRevealed) { matchRevealed = true }
                                            else ArabicText(paragraph, settings)
                                        }
                                    }
                                }
                            }
                        }
                        SelectionContainer {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                russianBlocks.forEach { block ->
                                    when (block) {
                                        is ReaderTextBlock.Section -> ReaderSectionCard(block, settings, arabic = false)
                                        is ReaderTextBlock.Body -> readerParagraphs(block.text).forEach { paragraph ->
                                            val annotated = annotatedReaderText(paragraph, query, MaterialTheme.colorScheme.primary, terms) { termDialog = it }
                                            SearchAnchorText(annotated, paragraph, query, readerStyle(settings), ready && p.id == anchor && !matchRevealed && !arabicMatch) { matchRevealed = true }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    notes.filter { it.paragraphId == p.id }.forEach { note ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("Моя заметка", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text(note.text, style = MaterialTheme.typography.bodyMedium) } }
                    }
                    if (actionsMode) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { newBookmark(p) }) { Text(if (bookmarks.any { it.paragraphId == p.id }) "Закладка ✓" else "Закладка") }
                        TextButton(onClick = { newNote(p) }) { Text("Заметка") }
                        TextButton(onClick = { copy(textFor(p)) }) { Text("Копировать") }
                    }
                }
            }
            item(key = "end") {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HorizontalDivider()
                    Text("Конец раздела", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val isRead = read.any { it.chapterId == chapter.id }
                    Button(onClick = { if (isRead) vm.markUnread(chapter.id) else vm.markRead(chapter.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(if (isRead) "Прочитано ✓ · Снять отметку" else "Отметить прочитанным") }
                    OutlinedButton(onClick = { savePosition(); onStudy(chapter.id) }, modifier = Modifier.fillMaxWidth()) { Text("Проверить понимание") }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { newBookmark(null) }) { Text("Закладка") }; TextButton(onClick = { newNote(null) }) { Text("Заметка") }; TextButton(onClick = { copy(fullChapter()) }) { Text("Копировать") }; TextButton(onClick = { share(fullChapter()) }) { Text("Поделиться") } }
                    notes.filter { it.paragraphId == null }.forEach { Text("Моя заметка · ${it.text}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { savePosition(); recordTime(); chapters.getOrNull(chapterIndex - 1)?.let { navigate(it.id) } }, modifier = Modifier.weight(1f), enabled = chapterIndex > 0) { Text("← Предыдущий") }
                        OutlinedButton(onClick = { savePosition(); recordTime(); chapters.getOrNull(chapterIndex + 1)?.let { navigate(it.id) } }, modifier = Modifier.weight(1f), enabled = chapterIndex >= 0 && chapterIndex < chapters.lastIndex) { Text("Следующий →") }
                    }
                    if (chapterStructure?.topics?.isNotEmpty() == true) {
                        Text("Темы этого раздела", style = MaterialTheme.typography.titleSmall)
                        chapterStructure.topics.forEach { topic -> TextButton(onClick = { savePosition(); onOpenParagraph(topic.paragraphId) }, modifier = Modifier.fillMaxWidth()) { Text(topic.title, Modifier.fillMaxWidth(), maxLines = 2, overflow = TextOverflow.Ellipsis) } }
                    }
                }
            }
        }
        SnackbarHost(snackbar)
    }
    if (panel) ModalBottomSheet(onDismissRequest = { panel = false }) { Box(Modifier.fillMaxHeight(.9f)) { SettingsPanel(settings, vm::settings) } }
    selected?.let { p -> AlertDialog(onDismissRequest = { selected = null }, title = { Text("Действия с абзацем") }, text = { Column { TextButton(onClick = { newBookmark(p); selected = null }) { Text("Добавить закладку") }; TextButton(onClick = { newNote(p); selected = null }) { Text("Написать заметку") }; TextButton(onClick = { copy(textFor(p)); selected = null }) { Text("Копировать абзац") }; TextButton(onClick = { share(textFor(p)); selected = null }) { Text("Поделиться абзацем") } } }, confirmButton = { TextButton(onClick = { selected = null }) { Text("Закрыть") } }) }
    if (noteDialog) AlertDialog(onDismissRequest = { noteDialog = false }, title = { Text(if (noteAnchor == null) "Заметка к разделу" else "Заметка к абзацу") }, text = { OutlinedTextField(noteText, { noteText = it }, label = { Text("Ваши мысли") }, modifier = Modifier.fillMaxWidth(), minLines = 5) }, confirmButton = { TextButton(onClick = { vm.addNote(chapter.id, noteAnchor, noteText); noteDialog = false; message = "Заметка сохранена" }, enabled = noteText.isNotBlank()) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = { noteDialog = false }) { Text("Отмена") } })
    if (bookmarkDialog) AlertDialog(onDismissRequest = { bookmarkDialog = false }, title = { Text("Сохранить закладку") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(bookmarkTitle, { bookmarkTitle = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(bookmarkNote, { bookmarkNote = it }, label = { Text("Комментарий · необязательно") }, modifier = Modifier.fillMaxWidth(), minLines = 3) } }, confirmButton = { TextButton(onClick = { vm.bookmarkParagraph(chapter.id, bookmarkAnchor, bookmarkTitle, bookmarkNote); bookmarkDialog = false; message = "Закладка сохранена" }, enabled = bookmarkTitle.isNotBlank()) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = { bookmarkDialog = false }) { Text("Отмена") } })
    if (sourceDialog) AlertDialog(onDismissRequest = { sourceDialog = false }, title = { Text("Источник раздела") }, text = { SelectionContainer { Text(chapter.source.ifBlank { "Источник требует редакционной сверки." }) } }, confirmButton = { TextButton(onClick = { sourceDialog = false }) { Text("Закрыть") } })
    termDialog?.let { term -> AlertDialog(onDismissRequest = { termDialog = null }, title = { Text(term.transcript) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { ArabicText(term.ar, settings); Text(term.definition); Text("Пояснение редакции к этому фрагменту", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }, confirmButton = { TextButton(onClick = { val id = term.sourceParagraphId; termDialog = null; onOpenParagraph(id) }) { Text("Исходный контекст") } }, dismissButton = { TextButton(onClick = { termDialog = null }) { Text("Закрыть") } }) }
}

@Composable
private fun ReaderSectionCard(block: ReaderTextBlock.Section, settings: ReadingSettings, arabic: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (arabic) ArabicText(block.label, settings, Modifier.heightIn(min = 28.dp))
        else Text(block.label.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        block.title?.let { title ->
            if (arabic) ArabicText(title, settings)
            else Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = russianFamily(settings),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun readerParagraphs(text: String): List<String> =
    text.split(Regex("\\r?\\n[ \\t]*\\r?\\n+"))
        .map { it.trim('\r', '\n', ' ') }
        .filter { it.isNotBlank() }

private val numberedLeadRegex = Regex(
    """(?im)^(?:(?:первая|вторая|третья|четвёртая|пятая|шестая|седьмая|восьмая|девятая|десятая|одиннадцатая|двенадцатая|первое|второе|третье|четвёртое|пятое|шестое|седьмое|восьмое|девятое|десятое|одиннадцатое|двенадцатое)(?:\s+[^\n:]{1,42})?:|во-(?:первых|вторых|третьих|четвёртых|пятых|шестых|седьмых|восьмых|девятых|десятых)[,.:]?|(?:\d{1,3}|[ivxlcdm]+)[.)])\s*"""
)

private val standaloneHeadingRegex = Regex(
    """(?i)^(?:глава|раздел|степень|ступень|смысл|польза|правило|основа|вопрос|ответ|положение|сторона)(?:\s+[^.!?…]{0,82})?$"""
)

private fun annotatedReaderText(text: String, query: String, color: androidx.compose.ui.graphics.Color, terms: List<StudyTerm>, open: (StudyTerm) -> Unit): AnnotatedString = buildAnnotatedString {
    append(highlight(text, query, color))

    numberedLeadRegex.findAll(text).forEach { match ->
        addStyle(
            SpanStyle(color = color, fontWeight = FontWeight.SemiBold),
            match.range.first, match.range.last + 1
        )
    }
    val trimmed = text.trim()
    if (!trimmed.contains('\n') && trimmed.length <= 100 && standaloneHeadingRegex.matches(trimmed)) {
        val start = text.indexOf(trimmed)
        if (start >= 0) addStyle(
            SpanStyle(fontWeight = FontWeight.SemiBold),
            start, start + trimmed.length
        )
    }

    terms.forEach { term ->
        val definitionLead = term.definition.substringBefore(';').substringBefore('.').substringBefore(',').trim()
        val transcriptBare = term.transcript.removePrefix("аль-").removePrefix("ал-").trim()
        val candidates = listOf(term.transcript.trim(), transcriptBare, definitionLead).filter { it.length >= 3 }.distinct()
        val hit = candidates.mapNotNull { candidate ->
            text.indexOf(candidate, ignoreCase = true).takeIf { it >= 0 }?.let { it to candidate }
        }.minByOrNull { it.first }
        if (hit != null) {
            val (first, label) = hit
            addLink(
                LinkAnnotation.Clickable(
                    "term:${term.transcript}",
                    TextLinkStyles(style = SpanStyle(color = color, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)),
                    linkInteractionListener = { open(term) }
                ),
                first, first + label.length
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchAnchorText(text:AnnotatedString,raw:String,query:String,style:TextStyle,reveal:Boolean,onRevealed:()->Unit) {
    val requester=remember {BringIntoViewRequester()}
    var bounds by remember(raw,query) {mutableStateOf<Rect?>(null)}
    Text(text,modifier=Modifier.fillMaxWidth().bringIntoViewRequester(requester),style=style,onTextLayout={layout ->
        val needle=normalizedText(query).text
        if(needle.isNotBlank()) {
            val normalized=normalizedText(raw);val match=normalized.text.indexOf(needle)
            if(match>=0) {val box=layout.getBoundingBox(normalized.offsets[match]);bounds=Rect(0f,box.top,layout.size.width.toFloat(),box.bottom)}
        }
    })
    LaunchedEffect(reveal,bounds) {
        if(reveal && bounds!=null) {withFrameNanos {};requester.bringIntoView(bounds);onRevealed()}
    }
}