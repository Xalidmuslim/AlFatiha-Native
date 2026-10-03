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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    val items = listOf(
        NavItem("Главная", R.drawable.ic_nav_home, onHome),
        NavItem("Содержание", R.drawable.ic_nav_contents, onContents),
        NavItem("Прогресс", R.drawable.ic_nav_progress, onProgress),
        NavItem("Меню", R.drawable.ic_nav_menu, onMenu),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
        ),
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = item.action)
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Image(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                    Text(
                        item.label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
