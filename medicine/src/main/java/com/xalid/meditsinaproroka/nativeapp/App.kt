package com.xalid.meditsinaproroka.nativeapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MedicinaApp(book: BookData, store: AppStore) {
    var current by remember { mutableStateOf<Route>(Route.Home) }
    val backStack = remember { mutableStateListOf<Route>() }
    val screenStateHolder = rememberSaveableStateHolder()
    var searchQuery by remember { mutableStateOf("") }
    var searchFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var bookmarkFolder by remember { mutableStateOf("Все") }
    var lastReaderChapterId by remember { mutableStateOf<String?>(null) }
    var contentsRequest by remember { mutableIntStateOf(0) }
    // Only the foreground of chapters opened from the contents fades in.
    // The parchment itself is always rendered fully opaque on first frame.
    var animateChapterEntry by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    fun navigate(route: Route, push: Boolean = true) {
        if (route is Route.Reader) {
            animateChapterEntry = current is Route.Book
            lastReaderChapterId = route.chapterId
        }
        // A page turn is not a new screen in the Android Back history.
        // Preserve the actual entry screen (contents, search, home, etc.).
        val isPageTurn = current is Route.Reader && route is Route.Reader
        if (push && current != route && !isPageTurn) backStack.add(current)
        current = route
    }

    fun goBack() {
        when {
            backStack.isNotEmpty() -> current = backStack.removeAt(backStack.lastIndex)
            current != Route.Home -> current = Route.Home
            else -> activity?.finish()
        }
    }

    fun root(route: Route) {
        backStack.clear()
        current = route
    }

    BackHandler(enabled = true) { goBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = current,
            transitionSpec = {
                val outgoingChapter = initialState as? Route.Reader
                val incomingChapter = targetState as? Route.Reader
                val pageTurn = outgoingChapter != null && incomingChapter != null &&
                    outgoingChapter.chapterId != incomingChapter.chapterId
                // Contents <-> chapter changes use no movement, alpha overlay
                // or size animation. This removes parchment afterimages/ghosting
                // caused by drawing both differently textured pages mid-transition.
                val betweenContentsAndReader =
                    (initialState is Route.Book && targetState is Route.Reader) ||
                        (initialState is Route.Reader && targetState is Route.Book)
                if (betweenContentsAndReader) {
                    EnterTransition.None.togetherWith(ExitTransition.None).using(null)
                } else if (pageTurn) {
                    val oldOrder = book.chapters.firstOrNull {
                        it.id == outgoingChapter!!.chapterId
                    }?.order ?: 0
                    val newOrder = book.chapters.firstOrNull {
                        it.id == incomingChapter!!.chapterId
                    }?.order ?: 0
                    val direction = if (newOrder >= oldOrder) 1 else -1
                    (
                        fadeIn(animationSpec = tween(205, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(
                                animationSpec = tween(245, easing = FastOutSlowInEasing),
                                initialOffsetX = { distance -> direction * distance / 5 },
                            )
                    ).togetherWith(
                        fadeOut(animationSpec = tween(175, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(
                                animationSpec = tween(235, easing = FastOutSlowInEasing),
                                targetOffsetX = { distance -> -direction * distance / 5 },
                            )
                    ).using(SizeTransform(clip = false))
                } else {
                    (
                        fadeIn(animationSpec = tween(205, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(
                                animationSpec = tween(225, easing = FastOutSlowInEasing),
                                initialOffsetX = { it / 40 },
                            )
                    ).togetherWith(
                        fadeOut(animationSpec = tween(155, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(
                                animationSpec = tween(175, easing = FastOutSlowInEasing),
                                targetOffsetX = { -it / 55 },
                            )
                    ).using(SizeTransform(clip = false))
                }
            },
            label = "sectionTransition",
        ) { route ->
            // The Reader parchment must extend behind the transparent Android
            // status bar; only its interactive header gets statusBarsPadding().
            val screenModifier = if (route == Route.Home || route is Route.Reader)
                Modifier.fillMaxSize()
            else Modifier.fillMaxSize().statusBarsPadding()
            screenStateHolder.SaveableStateProvider(routeStateKey(route)) {
                when (route) {
                Route.Home -> WebHomeScreen(
                    book = book,
                    store = store,
                    modifier = screenModifier,
                    navigate = ::navigate,
                    onGlobalSearch = { navigate(Route.Search) },
                    onToggleTheme = store::toggleSharedTheme,
                )
                Route.Book -> BookScreen(
                    book, screenModifier, ::goBack,
                    focusChapterId = lastReaderChapterId,
                    focusRequest = contentsRequest,
                ) { navigate(Route.Reader(it)) }
                Route.Topics -> TopicsScreen(book, screenModifier) { navigate(Route.TopicDetail(it)) }
                Route.Search -> SearchScreen(
                    book = book,
                    query = searchQuery,
                    onQuery = { searchQuery = it },
                    filter = searchFilter,
                    onFilter = { searchFilter = it },
                    modifier = screenModifier,
                    onOpen = { chapterId, anchor -> navigate(Route.Reader(chapterId, anchor)) },
                )
                Route.Bookmarks -> BookmarksScreen(
                    book = book,
                    store = store,
                    folder = bookmarkFolder,
                    onFolder = { bookmarkFolder = it },
                    modifier = screenModifier,
                    onOpen = { id, anchor -> navigate(Route.Reader(id, anchor)) },
                )
                Route.More -> WebMoreScreen(screenModifier, ::navigate)
                Route.Remedies -> RemediesScreen(book, screenModifier, ::goBack) { navigate(Route.RemedyDetail(it)) }
                Route.Treatments -> TreatmentsScreen(book, screenModifier, ::goBack) { navigate(Route.Reader(it)) }
                Route.Notes -> NotesScreen(book, store, screenModifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.Settings -> WebSettingsScreen(store, screenModifier, ::goBack)
                Route.Hadiths -> HadithsScreen(book, screenModifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.History -> HistoryScreen(book, store, screenModifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.Offline -> OfflineScreen(book, screenModifier, ::goBack)
                Route.About -> AboutScreen(book, screenModifier, ::goBack)
                Route.Collections -> CollectionsScreen(book, screenModifier, ::goBack) { navigate(Route.CollectionDetail(it)) }
                Route.Glossary -> GlossaryScreen(book, screenModifier, ::goBack) { navigate(Route.GlossaryDetail(it)) }
                Route.Source -> SourceScreen(book, screenModifier, ::goBack)
                is Route.GlossaryDetail -> GlossaryDetailScreen(book, route.id, screenModifier, ::goBack) { id, anchor ->
                    navigate(Route.Reader(id, anchor))
                }
                is Route.TopicDetail -> TopicDetailScreen(book, route.id, screenModifier, ::goBack) {
                    navigate(Route.Reader(it))
                }
                is Route.RemedyDetail -> RemedyDetailScreen(book, route.id, screenModifier, ::goBack) { id, anchor ->
                    navigate(Route.Reader(id, anchor))
                }
                is Route.CollectionDetail -> CollectionDetailScreen(book, route.id, screenModifier, ::goBack) {
                    navigate(Route.Reader(it))
                }
                is Route.Reader -> ReaderScreen(
                    book, store, route, screenModifier, ::goBack,
                    animateEntrance = animateChapterEntry,
                ) { next ->
                    navigate(next)
                }
                }
            }
        }
            StandaloneBottomNav(
                current = current,
                onHome = { root(Route.Home) },
                onTopics = {
                    if (current is Route.Reader) lastReaderChapterId = (current as Route.Reader).chapterId
                    contentsRequest += 1
                    root(Route.Book)
                },
                onSearch = { root(Route.Search) },
                onBookmarks = { root(Route.Bookmarks) },
                onMore = { root(Route.More) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}


private fun routeStateKey(route: Route): String = when (route) {
    Route.Home -> "home"
    Route.Book -> "book"
    Route.Topics -> "topics"
    Route.Search -> "search"
    Route.Bookmarks -> "bookmarks"
    Route.More -> "more"
    Route.Remedies -> "remedies"
    Route.Treatments -> "treatments"
    Route.Notes -> "notes"
    Route.Settings -> "settings"
    Route.Hadiths -> "hadiths"
    Route.History -> "history"
    Route.Offline -> "offline"
    Route.About -> "about"
    Route.Collections -> "collections"
    Route.Glossary -> "glossary"
    Route.Source -> "source"
    is Route.TopicDetail -> "topic:${route.id}"
    is Route.RemedyDetail -> "remedy:${route.id}"
    is Route.CollectionDetail -> "collection:${route.id}"
    is Route.GlossaryDetail -> "glossary:${route.id}"
    is Route.Reader -> "reader:${route.chapterId}:${route.anchor.orEmpty()}:${route.resume}"
}
@Composable
private fun StandaloneBottomNav(
    current: Route,
    onHome: () -> Unit,
    onTopics: () -> Unit,
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class NavItem(val label: String, val icon: Int, val selected: Boolean, val action: () -> Unit)
    val items = listOf(
        NavItem("Главная", R.drawable.nav_home, current == Route.Home, onHome),
        NavItem("Темы", R.drawable.nav_topics, current == Route.Topics || current == Route.Book, onTopics),
        NavItem("Поиск", R.drawable.nav_search, current == Route.Search, onSearch),
        NavItem("Закладки", R.drawable.nav_bookmark, current == Route.Bookmarks, onBookmarks),
        NavItem("Ещё", R.drawable.nav_more, current == Route.More, onMore),
    )
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    val panelShape = RoundedCornerShape(16.dp)
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(start = 13.dp, end = 13.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .clip(panelShape)
                    .background(if (dark) MaterialTheme.colorScheme.surface else Color(0xFFF2E8D9))
                    .border(
                        width = 0.9.dp,
                        color = if (dark) MaterialTheme.colorScheme.outline else Color(0xFFCDBBA2),
                        shape = panelShape,
                    ),
            ) {
                if (!dark) {
                    Image(
                        painter = MedicineHomeArtwork.painter(R.drawable.antique_card_paper),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.40f,
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        val color by animateColorAsState(
                            targetValue = if (item.selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(180, easing = FastOutSlowInEasing),
                            label = "bottomNavSelection",
                        )
                        Column(
                            Modifier.weight(1f).heightIn(min = 51.dp)
                                .semantics { selected = item.selected }
                                .clickable(role = Role.Tab, onClick = item.action)
                                .padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            AntiqueIcon(item.icon, Modifier.size(30.dp))
                            Spacer(Modifier.height(1.dp))
                            Text(
                                item.label,
                                fontFamily = WebSansFont,
                                fontSize = 10.5.sp,
                                lineHeight = 13.sp,
                                color = color,
                                fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}
