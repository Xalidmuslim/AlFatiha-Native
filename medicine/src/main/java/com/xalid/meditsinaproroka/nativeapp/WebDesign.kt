package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import android.graphics.Typeface
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

val WebSansFont = FontFamily.SansSerif
val WebCompactFont = FontFamily(Font(R.font.manrope_variable))
val WebModernFont = FontFamily(Font(R.font.inter_variable))
val WebLiterataFont = FontFamily(Font(R.font.literata_variable))
val WebSerifFont = FontFamily(
    Font(R.font.pt_serif_regular, FontWeight.Normal),
    Font(R.font.pt_serif_bold, FontWeight.Bold),
)
val WebClassicFont = WebSerifFont

fun readerComposeFontFamily(value: String): FontFamily = when (value) {
    "classic" -> WebLiterataFont
    "modern" -> WebModernFont
    "compact" -> WebCompactFont
    "system" -> FontFamily.SansSerif
    "system_serif" -> FontFamily.Serif
    else -> WebSerifFont
}

fun readerTypeface(context: Context, value: String): Typeface = when (value) {
    "classic" -> ResourcesCompat.getFont(context, R.font.literata_variable) ?: Typeface.SERIF
    "modern" -> ResourcesCompat.getFont(context, R.font.inter_variable) ?: Typeface.SANS_SERIF
    "compact" -> ResourcesCompat.getFont(context, R.font.manrope_variable) ?: Typeface.SANS_SERIF
    "system" -> Typeface.SANS_SERIF
    "system_serif" -> Typeface.SERIF
    else -> ResourcesCompat.getFont(context, R.font.pt_serif_regular) ?: Typeface.SERIF
}

@Composable
fun WebHeader(
    title: String,
    subtitle: String? = null,
    back: (() -> Unit)? = null,
    settings: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    // Do not paint a flat Material background over the textured reading page.
    // Other app headers retain their original opaque panel.
    Surface(
        color = if (compact) Color.Transparent else MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = if (compact) 40.dp else 46.dp)
                    .padding(horizontal = 7.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(if (compact) 36.dp else 40.dp), contentAlignment = Alignment.Center) {
                    if (back != null) {
                        IconButton(onClick = back, modifier = Modifier.size(if (compact) 36.dp else 40.dp)) {
                            Text("←", fontFamily = WebSansFont, fontSize = 24.sp)
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        title,
                        fontFamily = WebLiterataFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (if (compact) 15.sp else 16.5.sp),
                        lineHeight = (if (compact) 18.sp else 20.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            fontFamily = WebSansFont,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Box(Modifier.size(if (compact) 40.dp else 40.dp), contentAlignment = Alignment.Center) {
                    if (settings != null) {
                        IconButton(onClick = settings, modifier = Modifier.size(40.dp)) {
                            Text(
                                if (compact) "Aa" else "⚙",
                                fontFamily = if (compact) WebSerifFont else WebSansFont,
                                fontSize = if (compact) 17.sp else 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            if (!compact) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun WebHomeScreen(
    book: BookData,
    store: AppStore,
    modifier: Modifier,
    navigate: (Route) -> Unit,
    onGlobalSearch: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    AntiqueHomeScreen(book, store, modifier, navigate, onGlobalSearch, onToggleTheme)
}

@Composable
internal fun WebCollectionCard(
    collection: BookCollection,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 152.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                collection.title,
                fontFamily = WebLiterataFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                collection.description,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${collection.chapterIds.size} глав →",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun WebChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 12.sp)
    }
}

private data class MoreItem(
    val icon: String,
    val title: String,
    val subtitle: String,
    val route: Route,
)

@Composable
fun WebMoreScreen(modifier: Modifier, navigate: (Route) -> Unit) {
    val rows = listOf(
        MoreItem("▤", "Оглавление книги", "111 глав в оригинальном порядке", Route.Book),
        MoreItem("✚", "Как лечили / что применялось", "Состояния и методы из лечебных глав", Route.Treatments),
        MoreItem("▦", "Быстрые подборки", "Головная боль, сон, тревога, рукъя и другое", Route.Collections),
        MoreItem("⚗", "Справочник средств", "Переходы к местам полного текста", Route.Remedies),
        MoreItem("Aa", "Словарь терминов", "Рукъя, кыст, тальбина и другое", Route.Glossary),
        MoreItem("≡", "Хадисы и источники", "Источники, указанные в тексте", Route.Hadiths),
        MoreItem("✎", "Мои заметки", "Выделения и личные записи", Route.Notes),
        MoreItem("◷", "История чтения", "Недавно открытые главы", Route.History),
        MoreItem("⚙", "Настройки чтения", "Шрифт, интервал и оформление", Route.Settings),
        MoreItem("i", "О книге", "Автор, содержание и важное примечание", Route.About),
        MoreItem("§", "Об издании", "Состав книги и указанные источники", Route.Source),
    )

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        WebHeader("Ещё", settings = { navigate(Route.Settings) })
        androidx.compose.foundation.lazy.LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 110.dp),
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column {
                        rows.forEachIndexed { index, row ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable { navigate(row.route) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(11.dp),
                                ) {
                                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            row.icon,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontFamily = if (row.icon == "Aa") WebSerifFont else WebSansFont,
                                            fontSize = if (row.icon == "Aa") 20.sp else 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(row.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        row.subtitle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text("→", fontSize = 18.sp)
                            }
                            if (index != rows.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val readerSizeOptions = listOf(
    13f to "Очень мелкий",
    14f to "Мелкий",
    15f to "Компактный",
    16f to "Средний",
    17f to "Обычный",
    18f to "Увеличенный",
    20f to "Крупный",
    22f to "Очень крупный",
)

private val readerLineOptions = listOf(
    1.08f to "Минимальный",
    1.14f to "Очень плотный",
    1.20f to "Плотный",
    1.28f to "Компактный",
    1.38f to "Обычный",
    1.48f to "Свободный",
)

private fun closestSizeIndex(value: Float): Int =
    readerSizeOptions.indices.minByOrNull { abs(readerSizeOptions[it].first - value) } ?: 1

private fun closestLine(value: Float): Float =
    readerLineOptions.minByOrNull { abs(it.first - value) }?.first ?: 1.48f

@Composable
fun WebSettingsScreen(store: AppStore, modifier: Modifier, back: () -> Unit) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        WebHeader("Настройки чтения", back = back)
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Изменения сразу видны в образце.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = WebSansFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                )
            }
            item { WebSettingsPreview(store.settings) }

            item {
                WebSettingCard("Фон чтения") {
                    PaperBackgroundChoices(store)
                    Text(
                        "Для светлой темы. Фактура бумаги сохраняется.",
                        fontFamily = WebSansFont,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                WebSettingCard("Оформление") {
                    Text(
                        "Светлая и тёмная тема общие для всех разделов «Пути сердца».",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = WebSansFont,
                        fontSize = 11.5.sp,
                    )
                    WebChoiceGrid(
                        options = listOf(
                            "light" to "Светлая",
                            "dark" to "Тёмная",
                        ),
                        selected = store.settings.theme,
                        columns = 2,
                    ) { value ->
                        store.updateSettings { it.copy(theme = value) }
                    }
                }
            }

            item {
                WebSettingCard("Шрифт книги") {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            WebFontOption("Literata", "Книжный", "classic", store.settings.fontFamily, Modifier.weight(1f)) {
                                store.updateSettings { it.copy(fontFamily = "classic") }
                            }
                            WebFontOption("PT Serif", "Классика", "book", store.settings.fontFamily, Modifier.weight(1f)) {
                                store.updateSettings { it.copy(fontFamily = "book") }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            WebFontOption("Inter", "Современный", "modern", store.settings.fontFamily, Modifier.weight(1f)) {
                                store.updateSettings { it.copy(fontFamily = "modern") }
                            }
                            WebFontOption("Manrope", "Компактный", "compact", store.settings.fontFamily, Modifier.weight(1f)) {
                                store.updateSettings { it.copy(fontFamily = "compact") }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            WebFontOption("Android Sans", "Системный", "system", store.settings.fontFamily, Modifier.weight(1f)) {
                                store.updateSettings { it.copy(fontFamily = "system") }
                            }
                            WebFontOption("Android Serif", "Книжный", "system_serif", store.settings.fontFamily, Modifier.weight(1f)) {
                                store.updateSettings { it.copy(fontFamily = "system_serif") }
                            }
                        }
                    }
                }
            }

            item {
                WebSettingCard("Размер текста") {
                    val selected = readerSizeOptions[closestSizeIndex(store.settings.fontSizeSp)].first.toString()
                    WebChoiceGrid(
                        options = readerSizeOptions.map { it.first.toString() to "${it.first.toInt()} sp" },
                        selected = selected,
                        columns = 4,
                    ) { value ->
                        store.updateSettings { it.copy(fontSizeSp = value.toFloat()) }
                    }
                }
            }

            item {
                WebSettingCard("Межстрочный интервал") {
                    WebChoiceGrid(
                        options = readerLineOptions.map { it.first.toString() to it.second },
                        selected = closestLine(store.settings.lineSpacing).toString(),
                        columns = 2,
                    ) { value ->
                        store.updateSettings { it.copy(lineSpacing = value.toFloat()) }
                    }
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Метки медицины эпохи",
                                fontFamily = WebSansFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp,
                            )
                            Spacer(Modifier.height(1.dp))
                            Text(
                                "Показывать редакционную маркировку",
                                fontFamily = WebSansFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                store.updateSettings {
                                    it.copy(showHistoricalLabels = !it.showHistoricalLabels)
                                }
                            },
                            shape = RoundedCornerShape(9.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(if (store.settings.showHistoricalLabels) "Вкл." else "Выкл.")
                        }
                    }
                }
            }

            item {
                WebSettingCard("Данные чтения") {
                    OutlinedButton(
                        onClick = { store.clearProgress() },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(9.dp),
                    ) {
                        Text("Сбросить прогресс и историю", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaperBackgroundChoices(store: AppStore) {
    val selected = store.settings.paperBackground
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        listOf(
            "original" to "Старинная",
            "light" to "Светлая",
            "sage" to "Шалфей",
        ).forEach { (value, label) ->
            Surface(
                onClick = { store.updateSettings { it.copy(paperBackground = value) } },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (selected == value) 1.6.dp else 0.8.dp,
                    if (selected == value) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                ),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.fillMaxWidth().height(57.dp)
                            .clip(RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painterResource(R.drawable.reader_parchment_source),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        readerPaperOverlay(value)?.let { wash ->
                            Box(Modifier.fillMaxSize().background(wash))
                        }
                        Text(
                            "Aa",
                            color = Color(0xFF2D2822),
                            fontFamily = WebLiterataFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        label,
                        fontFamily = WebSansFont,
                        fontWeight = if (selected == value) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun WebReaderSettingsSheet(store: AppStore, onDone: () -> Unit) {
    val settings = store.settings
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Настройки чтения",
                    modifier = Modifier.weight(1f),
                    fontFamily = WebModernFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
                IconButton(onClick = onDone, modifier = Modifier.size(36.dp)) {
                    Text("×", fontSize = 21.sp)
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    "Предпросмотр шрифта и интервала для чтения.",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontFamily = readerComposeFontFamily(settings.fontFamily),
                    fontSize = settings.fontSizeSp.sp,
                    lineHeight = (settings.fontSizeSp * settings.lineSpacing).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        item {
            Text(
                "Фон чтения",
                fontFamily = WebModernFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
            )
        }
        item { PaperBackgroundChoices(store) }

        item {
            Text(
                "Шрифт",
                fontFamily = WebModernFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReaderFontChip("Literata", "classic", settings.fontFamily, Modifier.weight(1f)) {
                        store.updateSettings { it.copy(fontFamily = "classic") }
                    }
                    ReaderFontChip("PT Serif", "book", settings.fontFamily, Modifier.weight(1f)) {
                        store.updateSettings { it.copy(fontFamily = "book") }
                    }
                    ReaderFontChip("Inter", "modern", settings.fontFamily, Modifier.weight(1f)) {
                        store.updateSettings { it.copy(fontFamily = "modern") }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReaderFontChip("Manrope", "compact", settings.fontFamily, Modifier.weight(1f)) {
                        store.updateSettings { it.copy(fontFamily = "compact") }
                    }
                    ReaderFontChip("Android", "system", settings.fontFamily, Modifier.weight(1f)) {
                        store.updateSettings { it.copy(fontFamily = "system") }
                    }
                    ReaderFontChip("Serif", "system_serif", settings.fontFamily, Modifier.weight(1f)) {
                        store.updateSettings { it.copy(fontFamily = "system_serif") }
                    }
                }
            }
        }

        item {
            val sizeIndex = closestSizeIndex(settings.fontSizeSp)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Размер",
                    fontFamily = WebModernFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    modifier = Modifier.width(62.dp),
                )
                OutlinedButton(
                    onClick = {
                        val next = readerSizeOptions[(sizeIndex - 1).coerceAtLeast(0)].first
                        store.updateSettings { it.copy(fontSizeSp = next) }
                    },
                    modifier = Modifier.size(width = 50.dp, height = 38.dp),
                    contentPadding = PaddingValues(0.dp),
                ) { Text("A−") }
                Text(
                    "${settings.fontSizeSp.toInt()} sp",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontFamily = WebSansFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                )
                OutlinedButton(
                    onClick = {
                        val next = readerSizeOptions[(sizeIndex + 1).coerceAtMost(readerSizeOptions.lastIndex)].first
                        store.updateSettings { it.copy(fontSizeSp = next) }
                    },
                    modifier = Modifier.size(width = 50.dp, height = 38.dp),
                    contentPadding = PaddingValues(0.dp),
                ) { Text("A+") }
            }
        }

        item {
            Text(
                "Межстрочный интервал",
                fontFamily = WebModernFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
            )
        }

        item {
            WebChoiceGrid(
                options = readerLineOptions.map { it.first.toString() to "×${"%.2f".format(it.first)}" },
                selected = closestLine(settings.lineSpacing).toString(),
                columns = 3,
            ) { value ->
                store.updateSettings { it.copy(lineSpacing = value.toFloat()) }
            }
        }

        item {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(9.dp),
            ) {
                Text("Готово", fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReaderFontChip(
    label: String,
    value: String,
    selected: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val active = value == selected
    val backgroundColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(170),
        label = "readerFontBackground",
    )
    val strokeColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(170),
        label = "readerFontStroke",
    )
    Surface(
        onClick = onClick,
        modifier = modifier.height(37.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(
            if (active) 2.dp else 1.dp,
            strokeColor,
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                fontFamily = readerComposeFontFamily(value),
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WebSettingsPreview(settings: ReaderSettings) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "ПРЕДПРОСМОТР",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Руководство Пророка ﷺ в лечении",
                fontFamily = readerComposeFontFamily(settings.fontFamily),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.5.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Полезное знание требует спокойного и внимательного чтения.",
                fontFamily = readerComposeFontFamily(settings.fontFamily),
                fontSize = settings.fontSizeSp.sp,
                lineHeight = (settings.fontSizeSp * settings.lineSpacing).sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                when (settings.fontFamily) {
                    "classic" -> "Literata"
                    "modern" -> "Inter"
                    "compact" -> "Manrope"
                    "system" -> "Android Sans"
                    "system_serif" -> "Android Serif"
                    else -> "PT Serif"
                } + " · " + settings.fontSizeSp.toInt() + " sp · ×" + "%.2f".format(settings.lineSpacing),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = WebSansFont,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp,
            )
        }
    }
}

@Composable
private fun WebSettingCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                title,
                fontFamily = WebModernFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.5.sp,
            )
            content()
        }
    }
}

@Composable
private fun WebChoiceGrid(
    options: List<Pair<String, String>>,
    selected: String,
    columns: Int,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowOptions.forEach { (value, label) ->
                    val active = value == selected
                    val backgroundColor by animateColorAsState(
                        targetValue = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        animationSpec = tween(170),
                        label = "readingChoiceBackground",
                    )
                    val foregroundColor by animateColorAsState(
                        targetValue = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(170),
                        label = "readingChoiceText",
                    )
                    Surface(
                        onClick = { onSelect(value) },
                        modifier = Modifier.weight(1f),
                        color = backgroundColor,
                        contentColor = foregroundColor,
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(
                            1.dp,
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label,
                                fontFamily = WebSansFont,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                repeat(columns - rowOptions.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WebFontOption(
    label: String,
    fontName: String,
    value: String,
    selected: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val active = value == selected
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            if (active) 2.dp else 1.dp,
            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp)) {
            Text(
                label,
                fontFamily = readerComposeFontFamily(value),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                fontName,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = WebSansFont,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Пример текста",
                fontFamily = readerComposeFontFamily(value),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
