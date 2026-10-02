package com.xalid.meditsinaproroka.nativeapp

sealed interface Route {
    data object Home : Route
    data object Book : Route
    data object Topics : Route
    data object Search : Route
    data object Bookmarks : Route
    data object More : Route
    data object Remedies : Route
    data object Treatments : Route
    data object Notes : Route
    data object Settings : Route
    data object Hadiths : Route
    data object History : Route
    data object Offline : Route
    data object About : Route
    data object Collections : Route
    data object Glossary : Route
    data object Source : Route
    data class TopicDetail(val id: String) : Route
    data class RemedyDetail(val id: String) : Route
    data class CollectionDetail(val id: String) : Route
    data class GlossaryDetail(val id: String) : Route
    data class Reader(val chapterId: String, val anchor: String? = null, val resume: Boolean = false) : Route
}

enum class SearchFilter(val label: String) {
    ALL("Весь текст"),
    HADITH("Хадисы"),
    AUTHOR("Слова автора"),
    REMEDIES("Средства"),
    TITLES("Названия глав"),
}
