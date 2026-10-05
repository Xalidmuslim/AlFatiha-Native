package com.xalid.meditsinaproroka.nativeapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    val context = LocalContext.current
    val activity = context as? Activity

    fun navigate(route: Route, push: Boolean = true) {
        if (push && current != route) backStack.add(current)
        current = route
    }

    fun goBack() {
        if (backStack.isNotEmpty()) current = backStack.removeAt(backStack.lastIndex)
    }

    fun root(route: Route) {
        backStack.clear()
        current = route
    }

    BackHandler(enabled = true) {
        when {
            backStack.isNotEmpty() -> goBack()
            current != Route.Home -> root(Route.Home)
            else -> navigateToHeartPrayer(context, "home")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HeartPrayerBottomNav(
                onHome = { navigateToHeartPrayer(context, "home") },
                onContents = { root(Route.Book) },
                onProgress = { navigateToHeartPrayer(context, "progress") },
                onMenu = { navigateToHeartPrayer(context, "menu") },
            )
        },
    ) { insets ->
        val modifier = Modifier.padding(insets)
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (
                    fadeIn(animationSpec = tween(150)) +
                        scaleIn(initialScale = 0.992f, animationSpec = tween(150))
                ).togetherWith(
                    fadeOut(animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.996f, animationSpec = tween(150))
                ).using(SizeTransform(clip = false))
            },
            label = "sectionTransition",
        ) { route ->
            screenStateHolder.SaveableStateProvider(routeStateKey(route)) {
                when (route) {
                Route.Home -> WebHomeScreen(
                    book = book,
                    store = store,
                    modifier = modifier,
                    navigate = ::navigate,
                    onGlobalSearch = { navigateToHeartPrayer(context, "search") },
                    onToggleTheme = store::toggleSharedTheme,
                )
                Route.Book -> BookScreen(book, modifier, ::goBack) { navigate(Route.Reader(it)) }
                Route.Topics -> TopicsScreen(book, modifier) { navigate(Route.TopicDetail(it)) }
                Route.Search -> SearchScreen(
                    book = book,
                    query = searchQuery,
                    onQuery = { searchQuery = it },
                    filter = searchFilter,
                    onFilter = { searchFilter = it },
                    modifier = modifier,
                    onOpen = { chapterId, anchor -> navigate(Route.Reader(chapterId, anchor)) },
                )
                Route.Bookmarks -> BookmarksScreen(
                    book = book,
                    store = store,
                    folder = bookmarkFolder,
                    onFolder = { bookmarkFolder = it },
                    modifier = modifier,
                    onOpen = { id, anchor -> navigate(Route.Reader(id, anchor)) },
                )
                Route.More -> WebMoreScreen(modifier, ::navigate)
                Route.Remedies -> RemediesScreen(book, modifier, ::goBack) { navigate(Route.RemedyDetail(it)) }
                Route.Treatments -> TreatmentsScreen(book, modifier, ::goBack) { navigate(Route.Reader(it)) }
                Route.Notes -> NotesScreen(book, store, modifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.Settings -> WebSettingsScreen(store, modifier, ::goBack)
                Route.Hadiths -> HadithsScreen(book, modifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.History -> HistoryScreen(book, store, modifier, ::goBack) { id, anchor -> navigate(Route.Reader(id, anchor)) }
                Route.Offline -> OfflineScreen(book, modifier, ::goBack)
                Route.About -> AboutScreen(book, modifier, ::goBack)
                Route.Collections -> CollectionsScreen(book, modifier, ::goBack) { navigate(Route.CollectionDetail(it)) }
                Route.Glossary -> GlossaryScreen(book, modifier, ::goBack) { navigate(Route.GlossaryDetail(it)) }
                Route.Source -> SourceScreen(book, modifier, ::goBack)
                is Route.GlossaryDetail -> GlossaryDetailScreen(book, route.id, modifier, ::goBack) { id, anchor ->
                    navigate(Route.Reader(id, anchor))
                }
                is Route.TopicDetail -> TopicDetailScreen(book, route.id, modifier, ::goBack) {
                    navigate(Route.Reader(it))
                }
                is Route.RemedyDetail -> RemedyDetailScreen(book, route.id, modifier, ::goBack) { id, anchor ->
                    navigate(Route.Reader(id, anchor))
                }
                is Route.CollectionDetail -> CollectionDetailScreen(book, route.id, modifier, ::goBack) {
                    navigate(Route.Reader(it))
                }
                is Route.Reader -> ReaderScreen(book, store, route, modifier, ::goBack) { next ->
                    navigate(next)
                }
                }
            }
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
private fun navigateToHeartPrayer(context: android.content.Context, destination: String) {
    val intent = Intent()
        .setClassName(context.packageName, "app.alfatiha.tafsir.MainActivity")
        .putExtra("heart_nav", destination)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    context.startActivity(intent)
    (context as? Activity)?.let { activity ->
        activity.finish()
        if (Build.VERSION.SDK_INT < 34) {
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(
                R.anim.section_return_enter,
                R.anim.section_return_exit,
            )
        }
    }
}

@Composable
private fun HeartPrayerBottomNav(
    onHome: () -> Unit,
    onContents: () -> Unit,
    onProgress: () -> Unit,
    onMenu: () -> Unit,
) {
    data class NavItem(
        val label: String,
        val iconRes: Int,
        val action: () -> Unit,
    )

    // Keep these dimensions in lock-step with MainActivity.buildShell()/navBtn().
    // This section is a separate Compose activity, but visually it must remain
    // indistinguishable from the main application's bottom navigation.
    val items = listOf(
        NavItem("Главная", R.drawable.ic_nav_home, onHome),
        NavItem("Содержание", R.drawable.ic_nav_contents, onContents),
        NavItem("Прогресс", R.drawable.ic_nav_progress, onProgress),
        NavItem("Меню", R.drawable.ic_nav_menu, onMenu),
    )
    val dark = MaterialTheme.colorScheme.background.red < 0.25f
    val navStart = if (dark) Color(0xFF1F2522) else Color(0xFFFBF7F0)
    val navEnd = if (dark) Color(0xFF1D2220) else Color(0xFFF8F4EC)
    val navLine = if (dark) Color(0xFF3D4641) else Color(0xFFE6DDD0)
    val homeColor = Color(0xFF356D57)
    val mutedColor = if (dark) Color(0xFFAAB3AD) else Color(0xFF6E6A63)
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .height(64.dp)
            .shadow(3.dp, shape, clip = false)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(navStart, navEnd),
                ),
                shape = shape,
            )
            .border(1.dp, navLine, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val color = if (index == 0) homeColor else mutedColor
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = item.action)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(color),
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = item.label,
                            style = TextStyle(
                                color = color,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.2.sp,
                                lineHeight = 13.sp,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false,
                                ),
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
