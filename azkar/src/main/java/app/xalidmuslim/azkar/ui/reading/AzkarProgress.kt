package app.xalidmuslim.azkar.ui.reading

internal fun countCompletedItems(entries: List<AzkarReaderEntry>): Int =
    entries.count { it.currentCount >= it.item.count }
