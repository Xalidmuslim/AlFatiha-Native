package app.xalidmuslim.azkar.content

data class AzkarTextVariant(
    val arabic: String,
    val translation: String,
)

data class AzkarReadingItem(
    val id: String,
    val title: String,
    val count: Int,
    val disputed: Boolean,
    val arabic: String,
    val transliteration: String = "",
    val translation: String,
    val source: String,
    val note: String?,
    val hasInsight: Boolean,
    val explanation: AzkarExplanation? = null,
)

data class AzkarContentItem(
    val id: String,
    val title: String,
    val periods: Set<AzkarPeriod>,
    val targetCount: Int,
    val disputed: Boolean,
    val defaultVariant: AzkarTextVariant,
    val eveningVariant: AzkarTextVariant? = null,
    val source: String,
    val note: String? = null,
    val explanation: AzkarExplanation,
) {
    init {
        require(id.isNotBlank()) { "Stable dhikr id must not be blank" }
        require(periods.isNotEmpty()) { "Dhikr must belong to at least one period" }
        require(targetCount > 0) { "Target count must be positive" }
        if (eveningVariant != null) {
            require(AzkarPeriod.Evening in periods) {
                "Evening variant requires Evening membership"
            }
        }
    }

    fun resolve(period: AzkarPeriod): AzkarReadingItem {
        require(period in periods) { "$id is not visible in $period" }
        val variant = if (period == AzkarPeriod.Evening) {
            eveningVariant ?: defaultVariant
        } else {
            defaultVariant
        }
        return AzkarReadingItem(
            id = id,
            title = title,
            count = targetCount,
            disputed = disputed,
            arabic = variant.arabic,
            transliteration = AzkarTransliteration.forItem(id, period),
            translation = variant.translation,
            source = source,
            note = note,
            hasInsight = true,
            explanation = explanation,
        )
    }
}
