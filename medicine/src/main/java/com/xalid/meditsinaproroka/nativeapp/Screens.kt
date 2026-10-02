package com.xalid.meditsinaproroka.nativeapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageHeader(title: String, subtitle: String? = null, back: (() -> Unit)? = null) {
    WebHeader(title = title, subtitle = subtitle, back = back)
}

@Composable
private fun ChapterRow(chapter: Chapter, onClick: () -> Unit, trailing: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                chapter.order.toString().padStart(2, '0'),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(34.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(chapter.title, fontFamily = WebModernFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp)
                if (chapter.section.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        chapter.section,
                        fontFamily = WebSansFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailing != null) Text(trailing, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(18.dp)) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HomeScreen(
    book: BookData,
    store: AppStore,
    modifier: Modifier,
    navigate: (Route) -> Unit,
    search: (String) -> Unit,
) {
    var quickSearch by remember { mutableStateOf("") }
    val chaptersById = remember(book) { book.chapters.associateBy { it.id } }
    val last = store.lastChapterId?.let(chaptersById::get)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Медицина Пророка ﷺ", fontFamily = WebModernFont, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
            Text(book.author, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("Полный текст · чтение и изучение", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }

        if (last != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Продолжить чтение", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(5.dp))
                        Text(last.title, fontFamily = WebModernFont, fontSize = 23.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navigate(Route.Reader(last.id, resume = true)) }, modifier = Modifier.fillMaxWidth()) { Text("Продолжить") }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = quickSearch,
                onValueChange = { quickSearch = it },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Поиск по всей книге") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { if (quickSearch.isNotBlank()) IconButton(onClick = { search(quickSearch) }) { Icon(Icons.Default.ArrowForward, "Искать") } }
            )
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth().clickable { navigate(Route.Treatments) }) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Как лечили / что применялось", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Text("Состояние → средства → полный текст", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Читать книгу", "${book.chapters.size} глав", Modifier.weight(1f)) { navigate(Route.Book) }
                MetricCard("Темы", "${book.topics.size} разделов", Modifier.weight(1f)) { navigate(Route.Topics) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Средства", book.remedies.size.toString(), Modifier.weight(1f)) { navigate(Route.Remedies) }
                MetricCard("Закладки", store.bookmarks.size.toString(), Modifier.weight(1f)) { navigate(Route.Bookmarks) }
            }
        }

        item { Text("Быстрый доступ", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListItem(
                    headlineContent = { Text("Хадисы и источники") },
                    supportingContent = { Text("Источники показываются только там, где они указаны в тексте") },
                    leadingContent = { Icon(Icons.Default.FormatQuote, null) },
                    modifier = Modifier.clickable { navigate(Route.Hadiths) }
                )
                ListItem(
                    headlineContent = { Text("Мои заметки") },
                    supportingContent = { Text("${store.notes.size} заметок · ${store.highlights.size} выделений") },
                    leadingContent = { Icon(Icons.Default.EditNote, null) },
                    modifier = Modifier.clickable { navigate(Route.Notes) }
                )
                ListItem(
                    headlineContent = { Text("Офлайн") },
                    supportingContent = { Text("Весь текст книги находится внутри приложения") },
                    leadingContent = { Icon(Icons.Default.OfflinePin, null) },
                    modifier = Modifier.clickable { navigate(Route.Offline) }
                )
            }
        }
    }
}

@Composable
fun BookScreen(book: BookData, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    Column(modifier.fillMaxSize()) {
        PageHeader("Содержание", "${book.chapters.size} глав", back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(book.chapters, key = { it.id }) { chapter -> ChapterRow(chapter, { open(chapter.id) }) }
        }
    }
}

@Composable
fun TopicsScreen(book: BookData, modifier: Modifier, open: (String) -> Unit) {
    Column(modifier.fillMaxSize()) {
        WebHeader("Темы", "Изучение поверх полного текста")
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(Modifier.padding(horizontal = 2.dp)) {
                    Text(
                        "ТЕМАТИЧЕСКАЯ НАВИГАЦИЯ",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = WebModernFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "По смыслу, не только по порядку",
                        fontFamily = WebModernFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        "Одна глава может входить сразу в несколько тем.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = WebSansFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
            items((book.topics.size + 1) / 2) { rowIndex ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val first = book.topics.getOrNull(rowIndex * 2)
                    val second = book.topics.getOrNull(rowIndex * 2 + 1)
                    if (first != null) {
                        WebTopicCard(first, Modifier.weight(1f)) { open(first.id) }
                    }
                    if (second != null) {
                        WebTopicCard(second, Modifier.weight(1f)) { open(second.id) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WebTopicCard(topic: Topic, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 152.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                topic.title,
                fontFamily = WebModernFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.5.sp,
                lineHeight = 19.sp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Связанные главы полного текста",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = WebSansFont,
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
            )
            Text(
                "${topic.chapterIds.size} глав →",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = WebSansFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
            )
        }
    }
}

@Composable
fun TopicDetailScreen(book: BookData, id: String, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    val topic = book.topics.firstOrNull { it.id == id }
    val map = remember(book) { book.chapters.associateBy { it.id } }
    val chapters = topic?.chapterIds?.mapNotNull(map::get).orEmpty()
    Column(modifier.fillMaxSize()) {
        PageHeader(topic?.title ?: "Тема", "${chapters.size} глав", back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(chapters, key = { it.id }) { ChapterRow(it, { open(it.id) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    book: BookData,
    query: String,
    onQuery: (String) -> Unit,
    filter: SearchFilter,
    onFilter: (SearchFilter) -> Unit,
    modifier: Modifier,
    onOpen: (String, String?) -> Unit,
) {
    val results = remember(book, query, filter) { searchBook(book, query, filter) }
    Column(modifier.fillMaxSize()) {
        PageHeader("Поиск", if (query.length >= 2) "${results.size} результатов" else null)
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            placeholder = { Text("Введите слово или фразу") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Default.Close, "Очистить") } },
            singleLine = true
        )
        ScrollableTabRow(selectedTabIndex = SearchFilter.entries.indexOf(filter), edgePadding = 10.dp, divider = {}) {
            SearchFilter.entries.forEach { item -> Tab(selected = item == filter, onClick = { onFilter(item) }, text = { Text(item.label) }) }
        }
        when {
            query.length < 2 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Введите не менее двух символов", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else -> LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(results, key = { index, hit -> "${hit.chapterId}-${hit.anchor}-§index" }) { _, hit ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(hit.chapterId, hit.anchor) }) {
                        Column(Modifier.padding(15.dp)) {
                            Text(hit.chapterTitle, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(hit.snippet, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 5, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    book: BookData,
    store: AppStore,
    folder: String,
    onFolder: (String) -> Unit,
    modifier: Modifier,
    onOpen: (String, String?) -> Unit,
) {
    var showFolderDialog by remember { mutableStateOf(false) }
    var newFolder by remember { mutableStateOf("") }
    val map = remember(book) { book.chapters.associateBy { it.id } }
    val visible = store.bookmarks.filter { folder == "Все" || it.folder == folder }

    Column(modifier.fillMaxSize()) {
        PageHeader("Закладки", store.bookmarks.size.toString())
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Папки", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = { showFolderDialog = true }) { Icon(Icons.Default.CreateNewFolder, null); Spacer(Modifier.width(4.dp)); Text("Добавить") }
        }
        ScrollableTabRow(
            selectedTabIndex = (listOf("Все") + store.bookmarkFolders).indexOf(folder).coerceAtLeast(0),
            edgePadding = 10.dp,
            divider = {}
        ) {
            (listOf("Все") + store.bookmarkFolders).forEach { f -> Tab(selected = f == folder, onClick = { onFolder(f) }, text = { Text(f) }) }
        }
        if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("В этой папке пока нет закладок", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(visible, key = { "${it.chapterId}-${it.anchor}-${it.folder}" }) { bm ->
                    val ch = map[bm.chapterId] ?: return@items
                    ChapterRow(ch, { onOpen(ch.id, bm.anchor) }, if (bm.anchor != null) "Фрагмент" else bm.folder)
                }
            }
        }
    }

    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Новая папка") },
            text = { OutlinedTextField(newFolder, { newFolder = it }, label = { Text("Название") }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    store.addFolder(newFolder)
                    if (newFolder.trim().isNotEmpty()) onFolder(newFolder.trim())
                    newFolder = ""
                    showFolderDialog = false
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { showFolderDialog = false }) { Text("Отмена") } }
        )
    }
}

@Composable
fun MoreScreen(modifier: Modifier, navigate: (Route) -> Unit) {
    val rows = listOf(
        Triple("Оглавление книги", "111 глав в оригинальном порядке", Route.Book),
        Triple("Как лечили / что применялось", "Состояния и методы из лечебных глав", Route.Treatments),
        Triple("Быстрые подборки", "Головная боль, сон, тревога, рукъя и другое", Route.Collections),
        Triple("Справочник средств", "Переходы к местам полного текста", Route.Remedies),
        Triple("Словарь терминов", "Рукъя, кыст, тальбина и другое", Route.Glossary),
        Triple("Хадисы и источники", "Источники, указанные в тексте", Route.Hadiths),
        Triple("Мои заметки", "Выделения и личные записи", Route.Notes),
        Triple("История чтения", "Недавно открытые главы", Route.History),
        Triple("Офлайн-копия", "Книга уже сохранена внутри приложения", Route.Offline),
        Triple("Настройки чтения", "Шрифт, размер, интервал, тема", Route.Settings),
        Triple("О приложении", "Назначение и границы использования", Route.About),
        Triple("Об источнике текста", "Структура и полнота локального издания", Route.Source),
    )
    Column(modifier.fillMaxSize()) {
        PageHeader("Ещё")
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(rows) { (title, subtitle, route) ->
                ListItem(
                    headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(subtitle) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.clickable { navigate(route) }
                )
            }
        }
    }
}

@Composable
fun RemediesScreen(book: BookData, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    Column(modifier.fillMaxSize()) {
        PageHeader("Средства", book.remedies.size.toString(), back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(book.remedies.sortedBy { it.title }, key = { it.id }) { remedy ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { open(remedy.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(remedy.title, fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            if (remedy.aliases.isNotEmpty()) Text(remedy.aliases.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${remedy.chapterIds.size} связанных глав", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }
}

@Composable
fun RemedyDetailScreen(book: BookData, id: String, modifier: Modifier, back: () -> Unit, open: (String, String?) -> Unit) {
    val remedy = book.remedies.firstOrNull { it.id == id }
    val map = remember(book) { book.chapters.associateBy { it.id } }
    val chapters = remedy?.chapterIds?.mapNotNull(map::get).orEmpty()
    Column(modifier.fillMaxSize()) {
        PageHeader(remedy?.title ?: "Средство", "${chapters.size} глав", back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (remedy != null && remedy.aliases.isNotEmpty()) item {
                Card { Column(Modifier.padding(16.dp)) { Text("Также встречается как", fontWeight = FontWeight.SemiBold); Text(remedy.aliases.joinToString(", "), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            itemsIndexed(chapters, key = { _, ch -> ch.id }) { index, ch ->
                val anchor = remedy?.anchors?.getOrNull(index)
                ChapterRow(ch, { open(ch.id, anchor) })
            }
        }
    }
}

@Composable
fun NotesScreen(book: BookData, store: AppStore, modifier: Modifier, back: () -> Unit, open: (String, String?) -> Unit) {
    val chapters = remember(book) { book.chapters.associateBy { it.id } }
    Column(modifier.fillMaxSize()) {
        PageHeader("Мои заметки", store.notes.size.toString(), back)
        if (store.notes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Выделите текст в главе и выберите «Заметка»", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(store.notes, key = { it.id }) { note ->
                    val ch = chapters[note.chapterId]
                    Card {
                        Column(Modifier.padding(15.dp)) {
                            Text(ch?.title ?: "Глава", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(5.dp))
                            Text("«${note.selectedText}»", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(6.dp))
                            Text(note.note)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { open(note.chapterId, ch?.blocks?.firstOrNull { it.id == note.blockId }?.anchor) }) { Text("Открыть") }
                                TextButton(onClick = { store.deleteNote(note.id) }) { Text("Удалить") }
                                Spacer(Modifier.weight(1f))
                                Text(formatDate(note.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HadithsScreen(book: BookData, modifier: Modifier, back: () -> Unit, open: (String, String?) -> Unit) {
    val hits = remember(book) {
        buildList { book.chapters.forEach { ch -> ch.blocks.filter { it.type == "hadith" }.forEach { b -> add(Triple(ch, b, b.hadithSources.isNotEmpty())) } } }
    }
    Column(modifier.fillMaxSize()) {
        PageHeader("Хадисы и источники", hits.size.toString(), back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            itemsIndexed(hits, key = { index, item -> "${item.second.id}-§index" }) { _, (ch, block, hasSource) ->
                Card(Modifier.fillMaxWidth().clickable { open(ch.id, block.anchor) }) {
                    Column(Modifier.padding(15.dp)) {
                        Text(ch.title, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text(block.text, maxLines = 5, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(7.dp))
                        Text(
                            if (hasSource) block.hadithSources.joinToString(" · ") else "Источник требует ручного тахриджа",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasSource) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(book: BookData, store: AppStore, modifier: Modifier, back: () -> Unit, open: (String, String?) -> Unit) {
    val chapters = remember(book) { book.chapters.associateBy { it.id } }
    Column(modifier.fillMaxSize()) {
        PageHeader("История", store.history.size.toString(), back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(store.history, key = { "${it.chapterId}-${it.anchor}-${it.openedAt}" }) { entry ->
                val ch = chapters[entry.chapterId] ?: return@items
                Card(Modifier.fillMaxWidth().clickable { open(ch.id, entry.anchor) }) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ch.title, fontWeight = FontWeight.SemiBold)
                            Text(formatDate(entry.openedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }
}

private fun effectiveCollections(book: BookData): List<BookCollection> =
    if (book.collections.isNotEmpty()) book.collections else listOf(
        BookCollection("treatment", "Лечение и средства", book.chapters.filter { it.title.contains("леч", true) }.map { it.id }),
        BookCollection("food", "Питание и продукты", book.chapters.filter { it.topics.any { t -> t.contains("nutrition", true) } }.map { it.id }),
        BookCollection("spiritual", "Рукъя и духовное лечение", book.chapters.filter { it.title.contains("рук", true) || it.title.contains("сглаз", true) }.map { it.id }),
    )

@Composable
fun CollectionsScreen(book: BookData, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    val cols = remember(book) { effectiveCollections(book) }
    Column(modifier.fillMaxSize()) {
        WebHeader("Быстрые подборки", "${cols.size} подборок", back)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Подборки по смыслу и состояниям",
                    fontFamily = WebModernFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 23.sp,
                    lineHeight = 27.sp,
                )
            }
            items((cols.size + 1) / 2) { rowIndex ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val first = cols.getOrNull(rowIndex * 2)
                    val second = cols.getOrNull(rowIndex * 2 + 1)
                    if (first != null) {
                        CollectionGridCard(first, Modifier.weight(1f)) { open(first.id) }
                    }
                    if (second != null) {
                        CollectionGridCard(second, Modifier.weight(1f)) { open(second.id) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionGridCard(col: BookCollection, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 150.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                col.title,
                fontFamily = WebModernFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 20.sp,
            )
            if (col.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    col.description,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            Text(
                "${col.chapterIds.size} глав →",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun CollectionDetailScreen(book: BookData, id: String, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    val col = remember(book, id) { effectiveCollections(book).firstOrNull { it.id == id } }
    val map = remember(book) { book.chapters.associateBy { it.id } }
    val chapters = col?.chapterIds?.mapNotNull(map::get).orEmpty()
    Column(modifier.fillMaxSize()) {
        PageHeader(col?.title ?: "Подборка", "${chapters.size} глав", back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (!col?.description.isNullOrBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(col?.description.orEmpty(), modifier = Modifier.padding(16.dp))
                    }
                }
            }
            items(chapters, key = { it.id }) { ChapterRow(it, { open(it.id) }) }
        }
    }
}

@Composable
fun TreatmentsScreen(book: BookData, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Все") }
    val categories = remember(book) { listOf("Все") + book.treatments.map { it.category }.filter { it.isNotBlank() }.distinct() }
    val visible = remember(book, query, category) {
        val q = query.trim()
        book.treatments.filter { treatment ->
            val categoryMatches = category == "Все" || treatment.category == category
            val queryMatches = q.isBlank() ||
                treatment.condition.contains(q, ignoreCase = true) ||
                treatment.remedies.any { it.contains(q, ignoreCase = true) } ||
                treatment.methods.any { it.contains(q, ignoreCase = true) }
            categoryMatches && queryMatches
        }
    }

    Column(modifier.fillMaxSize()) {
        PageHeader("Как лечили", "${book.treatments.size} состояний", back)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                "Историко-религиозный справочник, а не персональная медицинская инструкция.",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Состояние или средство") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
        )
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(category).coerceAtLeast(0),
            edgePadding = 10.dp,
            divider = {}
        ) {
            categories.forEach { value ->
                Tab(selected = value == category, onClick = { category = value }, text = { Text(value) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(visible, key = { it.id }) { treatment ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { open(treatment.chapterId) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (treatment.category.isNotBlank()) {
                            Text(treatment.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(treatment.condition, fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        val mentioned = (treatment.methods + treatment.remedies).distinct()
                        if (mentioned.isNotEmpty()) {
                            Text("Что упоминается", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(mentioned.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Открыть полный текст →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun GlossaryScreen(book: BookData, modifier: Modifier, back: () -> Unit, open: (String) -> Unit) {
    Column(modifier.fillMaxSize()) {
        PageHeader("Словарь терминов", "${book.glossary.size} терминов", back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(book.glossary, key = { it.id }) { term ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { open(term.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(term.term, fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Text(term.definition, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Все упоминания →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun GlossaryDetailScreen(
    book: BookData,
    id: String,
    modifier: Modifier,
    back: () -> Unit,
    open: (String, String?) -> Unit,
) {
    val term = remember(book, id) { book.glossary.firstOrNull { it.id == id } }
    val hits = remember(book, term) {
        term?.let { searchBook(book, it.query, SearchFilter.ALL) }.orEmpty()
    }
    Column(modifier.fillMaxSize()) {
        PageHeader(term?.term ?: "Термин", "${hits.size} упоминаний", back)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (term != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(term.term, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text(term.definition)
                            if (term.aliases.isNotEmpty()) {
                                Text(term.aliases.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            itemsIndexed(hits, key = { index, hit -> "${hit.chapterId}-${hit.anchor}-$index" }) { _, hit ->
                Card(Modifier.fillMaxWidth().clickable { open(hit.chapterId, hit.anchor) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(hit.chapterTitle, fontWeight = FontWeight.SemiBold)
                        Text(hit.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SourceScreen(book: BookData, modifier: Modifier, back: () -> Unit) {
    val blocks = if (book.stats.blocks > 0) book.stats.blocks else book.chapters.sumOf { it.blocks.size }
    val words = book.stats.sourceWords
    Column(modifier.fillMaxSize()) {
        PageHeader("Об источнике текста", null, back)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Полный русский текст", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text("Один источник данных", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Главы, темы, средства, поиск и справочники строятся из одной локальной копии текста, встроенной в приложение.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Глав", (if (book.stats.chapters > 0) book.stats.chapters else book.chapters.size).toString(), Modifier.weight(1f)) {}
                MetricCard("Блоков", blocks.toString(), Modifier.weight(1f)) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Слов", if (words > 0) words.toString() else "—", Modifier.weight(1f)) {}
                MetricCard("Средств", (if (book.stats.remedies > 0) book.stats.remedies else book.remedies.size).toString(), Modifier.weight(1f)) {}
            }
            Text("Текст упакован в APK/AAB и не требует сети для чтения.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun OfflineScreen(book: BookData, modifier: Modifier, back: () -> Unit) {
    Column(modifier.fillMaxSize()) {
        PageHeader("Офлайн", null, back)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.OfflinePin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
            Text("Полная книга доступна без интернета", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Текст ${book.chapters.size} глав встроен непосредственно в APK/AAB. Поиск, закладки, заметки, настройки и прогресс чтения также работают локально.")
            Text("Интернет для чтения книги не требуется.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AboutScreen(book: BookData, modifier: Modifier, back: () -> Unit) {
    Column(modifier.fillMaxSize()) {
        PageHeader("О книге", null, back)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(book.title, fontFamily = WebModernFont, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(book.author, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("В приложении сохранён полный текущий текст издания, используемый в веб-версии, с исходным порядком глав и дополнительной тематической навигацией.")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Важное примечание", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Это историко-религиозный текст, а не современное медицинское руководство. Описанные методы не заменяют диагностику и лечение врача; отдельные исторические средства могут быть устаревшими или небезопасными.")
                }
            }
            Text("Источники хадисов показываются только там, где они присутствуют в данных книги. Приложение не присваивает степень достоверности автоматически.")
        }
    }
}
