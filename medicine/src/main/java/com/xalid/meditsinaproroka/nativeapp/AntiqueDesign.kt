package com.xalid.meditsinaproroka.nativeapp

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Median calm paper regions sampled from the supplied reference.
internal val AntiqueParchment = Color(0xFFF0E4D3)
internal val AntiquePaper = Color(0xFFF8F0E4)
internal val AntiqueInk = Color(0xFF211D19)
internal val AntiqueMuted = Color(0xFF625D53)
internal val AntiqueGreen = Color(0xFF305A43)
internal val AntiqueBorder = Color(0xFFDCCAB1)

@Composable
internal fun AntiqueIcon(@DrawableRes resource: Int, modifier: Modifier, description: String? = null,
                         scale: ContentScale = ContentScale.Fit) {
    Image(MedicineHomeArtwork.painter(resource), description, modifier, contentScale = scale)
}

/** The scalable frame, text and click target are native; artwork is a separate layer. */
@Composable
private fun PaperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    Surface(
        // Do not force an off-screen framebuffer per card: on mid-range
        // devices it consumes extra memory/bandwidth while scrolling.
        modifier = modifier.then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        color = if (dark) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = if (dark) RoundedCornerShape(13.dp) else RectangleShape,
        border = if (dark) BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline) else null,
        shadowElevation = 0.dp,
    ) {
        val context = LocalContext.current
        val frame = remember(context) { MedicineHomeArtwork.newCardFrame(context) }
        val paperGradient = remember {
            Brush.horizontalGradient(
                0f to Color(0xFFDAC6A8).copy(alpha = 0.12f),
                0.12f to Color(0xFFFFF9F0).copy(alpha = 0.44f),
                0.88f to Color(0xFFFFF9F0).copy(alpha = 0.44f),
                1f to Color(0xFFDAC6A8).copy(alpha = 0.12f),
            )
        }
        Box(Modifier.drawBehind {
            if (!dark) drawIntoCanvas { canvas ->
                val width = size.width.toInt()
                val height = size.height.toInt()
                if (frame.bounds.width() != width || frame.bounds.height() != height) {
                    frame.setBounds(0, 0, width, height)
                }
                frame.draw(canvas.nativeCanvas)
            }
        }) {
            if (!dark) {
                Image(
                    MedicineHomeArtwork.painter(R.drawable.antique_card_paper), null,
                    Modifier.matchParentSize().padding(4.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop, alpha = 0.36f,
                )
                Box(
                    Modifier.matchParentSize().padding(5.dp).clip(RoundedCornerShape(9.dp))
                        .background(paperGradient)
                )
            }
            content()
        }
    }
}

@Composable
private fun AntiqueHomeHeader(book: BookData, onSettings: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    val enlarged = LocalDensity.current.fontScale > 1.15f
    // Keep the home hero tall enough for the illustration and two-line title,
    // but avoid the extra empty space that pushed Quick Collections behind nav.
    BoxWithConstraints(Modifier.fillMaxWidth().heightIn(min = 148.dp)) {
        val artWidth = maxWidth * 0.34f
        val heroHeight = maxWidth / 3.45f
        if (!dark) {
            // Image begins at y=0 so Android's transparent status bar belongs to the same paper.
            Image(MedicineHomeArtwork.painter(R.drawable.antique_hero_refined), null,
                Modifier.fillMaxWidth().height(148.dp).align(Alignment.TopCenter),
                contentScale = ContentScale.Crop)
            Box(
                Modifier.fillMaxWidth().height(148.dp).align(Alignment.TopCenter)
                    .background(Color(0xFFF5EADB).copy(alpha = 0.32f))
            )
            Box(
                Modifier.fillMaxWidth().height(148.dp).align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0xFFF7EBDD).copy(alpha = 0.12f),
                            0.73f to Color.Transparent,
                            1f to AntiqueParchment,
                        )
                    )
            )
        }
        Column(
            Modifier.fillMaxWidth().statusBarsPadding()
                .padding(start = 14.dp, end = 12.dp, top = 9.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(MedicineHomeArtwork.painter(R.drawable.medicine_launcher_book), "Настройки чтения",
                    Modifier.size(70.dp).clip(RoundedCornerShape(15.dp)).clickable(role = Role.Button, onClick = onSettings),
                    contentScale = ContentScale.Fit)
                if (!enlarged) {
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f).offset(y = (-5).dp).padding(end = artWidth - 12.dp)) {
                        Text(book.title, fontFamily = WebLiterataFont, fontWeight = FontWeight.Bold,
                            fontSize = 22.sp, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(6.dp))
                        Text(book.author, fontFamily = WebSansFont, fontSize = 10.5.sp, lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            if (enlarged) {
                // At accessible font sizes, put all native text below the still life.
                Spacer(Modifier.height(heroHeight - 70.dp + 4.dp))
                Text(book.title, fontFamily = WebLiterataFont, fontWeight = FontWeight.Bold,
                    fontSize = 23.sp, lineHeight = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(book.author, fontFamily = WebSansFont, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun ReadingCard(title: String, pct: Int, hasLast: Boolean, onContinue: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    val animatedProgress by animateFloatAsState(
        targetValue = (pct / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "readerProgress",
    )
    PaperCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Only transparent vertical padding is trimmed; the book keeps its scale.
                AntiqueIcon(R.drawable.antique_open_book_detail, Modifier.size(width = 52.dp, height = 36.dp), scale = ContentScale.Fit)
                Spacer(Modifier.width(5.dp))
                Text("Продолжить чтение", Modifier.weight(1f), fontFamily = WebSansFont,
                    fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size((39 * LocalDensity.current.fontScale.coerceAtLeast(1f)).dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress }, modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = if (dark) MaterialTheme.colorScheme.outline else AntiqueBorder.copy(alpha = 0.6f),
                        strokeWidth = 2.dp,
                    )
                    Text("$pct%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(title, fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold,
                fontSize = 16.5.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(9.dp))
            LinearProgressIndicator(
                progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = if (dark) MaterialTheme.colorScheme.outline else AntiqueBorder,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF315A46),
                border = BorderStroke(1.dp, Color(0xFFAF986C)),
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Keep the button matte; artwork must never compete with its label.
                    Text(if (hasLast) "Продолжить →" else "Начать чтение →",
                        Modifier.padding(horizontal = 38.dp, vertical = 10.dp),
                        fontFamily = WebLiterataFont, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, lineHeight = 23.sp, color = Color(0xFFFFFAEF))
                }
            }
        }
    }
}

@Composable
private fun AntiqueQuickCard(@DrawableRes icon: Int, title: String, subtitle: String,
                             modifier: Modifier, onClick: () -> Unit) {
    PaperCard(modifier.fillMaxHeight().heightIn(min = 88.dp), onClick) {
        Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 3.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AntiqueIcon(icon, Modifier.size(width = 70.dp, height = 48.dp))
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp))
            }
            Text(title, fontFamily = WebLiterataFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(1.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = WebSansFont,
                fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AntiqueHomeScreen(
    book: BookData, store: AppStore, modifier: Modifier,
    navigate: (Route) -> Unit, onGlobalSearch: () -> Unit, onToggleTheme: () -> Unit,
) {
    // Preserve the existing home screen's progress calculation and resume behavior.
    val chaptersById = remember(book) { book.chapters.associateBy { it.id } }
    val last = store.lastChapterId?.let(chaptersById::get)
    val completed = book.chapters.count { chapter ->
        val index = store.progress[chapter.id]
        index != null && chapter.blocks.isNotEmpty() &&
            index.toFloat() >= ((chapter.blocks.size - 1).toFloat() * 0.96f)
    }
    val pct = if (book.chapters.isEmpty()) 0 else
        ((completed * 100f) / book.chapters.size).toInt().coerceIn(0, 100)
    val quickCollections = book.collections.take(4)
    val continueTitle = when {
        last == null -> "Начать с первой главы"
        last.title.trim().startsWith("Глава:", ignoreCase = true) -> last.title.trim()
        else -> "Глава: ${last.title.trim()}"
    }
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!dark) Image(MedicineHomeArtwork.painter(R.drawable.antique_parchment), null,
            Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = 0.72f)
        // No clipped leaves over the page edges; the image stays within the header.
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item(key = "hero", contentType = "hero") { AntiqueHomeHeader(book) { navigate(Route.Settings) } }
            item(key = "hadith") {
                // The same real antique edge artwork used by other book cards.
                // Text remains native, selectable by accessibility and crisp.
                PaperCard(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(
                        start = 16.dp, end = 16.dp, top = 11.dp, bottom = 12.dp,
                    )) {
                        Text(
                            "Посланник Аллаха ﷺ сказал:",
                            fontFamily = WebSansFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "«Аллах не ниспослал ни одной болезни, не ниспослав вместе с ней исцеления»",
                            fontFamily = WebLiterataFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            "Источник: Сахих аль-Бухари, хадис № 5678.",
                            fontFamily = WebSansFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            item(key = "reading") {
                Box(Modifier.padding(horizontal = 14.dp)) {
                    ReadingCard(continueTitle, pct, last != null) {
                        val chapter = last ?: book.chapters.firstOrNull()
                        if (chapter != null) navigate(Route.Reader(chapter.id, resume = last != null))
                    }
                }
            }
            item(key = "search") {
                PaperCard(Modifier.fillMaxWidth().padding(horizontal = 14.dp), onClick = { navigate(Route.Search) }) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        AntiqueIcon(R.drawable.antique_search_detail, Modifier.size(39.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Поиск по всей книге", Modifier.weight(1f), fontFamily = WebLiterataFont,
                            fontSize = 14.sp, lineHeight = 19.sp)
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(22.dp))
                    }
                }
            }
            item(key = "treatments") {
                PaperCard(Modifier.fillMaxWidth().padding(horizontal = 14.dp), onClick = { navigate(Route.Treatments) }) {
                    Row(Modifier.fillMaxWidth().padding(start = 5.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        AntiqueIcon(R.drawable.antique_mortar_detail, Modifier.size(60.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Как лечили / что применялось", fontFamily = WebLiterataFont,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp)
                            Spacer(Modifier.height(3.dp))
                            Text("Состояние → средства → полный текст", fontFamily = WebSansFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp, lineHeight = 14.sp)
                        }
                        Spacer(Modifier.width(7.dp))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp))
                    }
                }
            }
            item(key = "quick-books") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AntiqueQuickCard(R.drawable.antique_books_detail, "Читать книгу", "${book.chapters.size} глав", Modifier.weight(1f)) { navigate(Route.Book) }
                    AntiqueQuickCard(R.drawable.antique_scroll_detail, "Темы", "${book.topics.size} разделов", Modifier.weight(1f)) { navigate(Route.Topics) }
                }
            }
            item(key = "quick-remedies") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AntiqueQuickCard(R.drawable.antique_remedies_detail, "Средства", "${book.remedies.size} позиций", Modifier.weight(1f)) { navigate(Route.Remedies) }
                    AntiqueQuickCard(R.drawable.antique_bookmark_detail, "Закладки", "${store.bookmarks.size} сохранено", Modifier.weight(1f)) { navigate(Route.Bookmarks) }
                }
            }
            if (quickCollections.isNotEmpty()) {
                item(key = "collections-heading") {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Быстрые подборки", Modifier.weight(1f), fontFamily = WebLiterataFont,
                            fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        TextButton(onClick = { navigate(Route.Collections) }) { Text("Все") }
                    }
                }
                for (rowIndex in 0 until ((quickCollections.size + 1) / 2)) {
                    item(key = "collections-$rowIndex") {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            val first = quickCollections.getOrNull(rowIndex * 2)
                            val second = quickCollections.getOrNull(rowIndex * 2 + 1)
                            if (first != null) WebCollectionCard(first, Modifier.weight(1f)) { navigate(Route.CollectionDetail(first.id)) }
                            if (second != null) WebCollectionCard(second, Modifier.weight(1f)) { navigate(Route.CollectionDetail(second.id)) }
                            else Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item(key = "study") {
                Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Продолжить изучение", fontFamily = WebLiterataFont, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    // Wrap at narrower widths / larger system fonts instead of horizontal scroll.
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = { navigate(Route.Glossary) }) { Text("Словарь терминов") }
                        OutlinedButton(onClick = { navigate(Route.Hadiths) }) { Text("Хадисы и источники") }
                        OutlinedButton(onClick = { navigate(Route.Notes) }) { Text("Мои заметки") }
                        OutlinedButton(onClick = { navigate(Route.Offline) }) { Text("Чтение без интернета") }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onGlobalSearch) { Text("Поиск") }
                        TextButton(onClick = onToggleTheme) { Text(if (dark) "Светлая тема" else "Тёмная тема") }
                        TextButton(onClick = { navigate(Route.Settings) }) { Text("Aa · Настройки чтения") }
                    }
                }
            }
        }
    }
}
