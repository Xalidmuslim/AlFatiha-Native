package app.xalidmuslim.azkar.content

data class AzkarExplanation(
    val meaning: String,
    val relatedReport: String? = null,
    val scholarNotes: List<String> = emptyList(),
    val references: List<String> = emptyList(),
)
