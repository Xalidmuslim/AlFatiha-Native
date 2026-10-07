from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('.')
data=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/data'
ui=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/ui'

p=data/'BookRepository.kt'
s=p.read_text(encoding='utf-8')

# Add a cache for parsed conjugation tables.
old='''    @Volatile private var proverbsCache: List<ProverbEntry>? = null
'''
new='''    @Volatile private var proverbsCache: List<ProverbEntry>? = null
    @Volatile private var conjugationCache: List<ConjugationQuestion>? = null
'''
if old not in s:
    raise SystemExit('conjugation cache insertion point not found')
s=s.replace(old,new,1)

old_func='''    fun conjugationQuestions(limit: Int = 60): List<ConjugationQuestion> {
        val out = mutableListOf<ConjugationQuestion>()
        for (section in sections("morphology").filter { it.found }) {
            for (group in readerGroups(section.id)) {
                if (group.kind != ReaderKind.TABLE) continue
                val lineList: (String) -> List<String> = { text ->
                    text.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toList()
                }
                val labels = group.blocks.firstOrNull()?.let { lineList(it.text) }.orEmpty()
                val columns = group.blocks.drop(1).take(3).map { block ->
                    lineList(block.text).filterNot { it.lowercase().contains("число") }
                }
                val valid = labels.size in 4..6 &&
                    labels.any { it.lowercase().contains("лицо") } &&
                    columns.size == 3 &&
                    columns.all { it.size >= labels.size }
                if (!valid) continue

                val numbers = listOf("единственное число", "двойственное число", "множественное число")
                labels.forEachIndexed { row, raw ->
                    val label = raw.replace("жен.рода", "жен. рода").replace("муж.рода", "муж. рода")
                    columns.forEachIndexed { column, values ->
                        val answer = values.getOrNull(row).orEmpty().trim()
                        if (answer.isNotBlank()) {
                            out += ConjugationQuestion(
                                id = section.id + ":table:" + group.blocks.first().index + ":" + row + ":" + column,
                                sectionId = section.id,
                                sectionTitle = section.title,
                                personLabel = label,
                                numberLabel = numbers[column],
                                answer = answer,
                            )
                        }
                    }
                }
            }
        }
        return out.shuffled().take(limit)
    }
'''
new_func='''    fun conjugationQuestions(limit: Int = 60): List<ConjugationQuestion> {
        conjugationCache?.let { return it.shuffled().take(limit) }
        val out = mutableListOf<ConjugationQuestion>()
        val arabic = Regex("[\\\\u0600-\\\\u06FF\\\\u0750-\\\\u077F\\\\u08A0-\\\\u08FF]")
        val cyrillic = Regex("[А-Яа-яЁё]")
        val canonicalLabels = listOf(
            "1 лицо",
            "2 лицо жен. рода",
            "2 лицо муж. рода",
            "3 лицо жен. рода",
            "3 лицо муж. рода",
        )
        val lineList: (String) -> List<String> = { text ->
            text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }

        for (section in sections("morphology").filter { it.found }) {
            val bs = blocks(section.id)
            for (i in bs.indices) {
                val headerLines = lineList(bs[i].text)
                val header = headerLines.joinToString(" ").lowercase()
                val isHeader = header.contains("1 лицо") &&
                    header.contains("2 лицо") &&
                    header.contains("3 лицо")
                if (!isHeader) continue

                val cue = ((i - 1) downTo maxOf(0, i - 4))
                    .map { bs[it].text.replace(Regex("\\\\s+"), " ").trim() }
                    .firstOrNull {
                        it.length in 2..180 &&
                            arabic.containsMatchIn(it) &&
                            cyrillic.containsMatchIn(it) &&
                            !it.contains("лицо", ignoreCase = true)
                    }
                val displayTitle = if (cue.isNullOrBlank()) section.title else section.title + "\\n" + cue

                for (rowOffset in 1..4) {
                    val rowBlock = bs.getOrNull(i + rowOffset) ?: break
                    val rowLines = lineList(rowBlock.text)
                    val rowText = rowLines.joinToString(" ").lowercase()
                    val numberLabel = when {
                        rowText.contains("единств") -> "единственное число"
                        rowText.contains("двойств") -> "двойственное число"
                        rowText.contains("множеств") -> "множественное число"
                        else -> break
                    }
                    val forms = rowLines.take(5)
                    if (forms.size < 5) continue
                    forms.forEachIndexed { column, rawAnswer ->
                        val answer = rawAnswer.trim()
                        val usable = answer.isNotBlank() &&
                            answer.lowercase() != "нет" &&
                            !answer.contains('_') &&
                            arabic.containsMatchIn(answer)
                        if (!usable) return@forEachIndexed
                        out += ConjugationQuestion(
                            id = section.id + ":conj:" + bs[i].index + ":" + rowOffset + ":" + column,
                            sectionId = section.id,
                            sectionTitle = displayTitle,
                            personLabel = canonicalLabels[column],
                            numberLabel = numberLabel,
                            answer = answer,
                        )
                    }
                }
            }
        }
        val parsed = out.distinctBy { it.id }
        conjugationCache = parsed
        return parsed.shuffled().take(limit)
    }
'''
if old_func not in s:
    raise SystemExit('old conjugationQuestions function not found')
s=s.replace(old_func,new_func,1)

# Background parsing makes the trainer open without a first-use pause.
old='''        Thread({ runCatching { sections(); sections("syntax"); sections("morphology"); sections("extras"); proverbs(); sections().asSequence().filter{it.found}.forEach{readerGroups(it.id)} } }, "book-prewarm").apply { priority=Thread.MIN_PRIORITY; start() }
'''
new='''        Thread({ runCatching { sections(); sections("syntax"); sections("morphology"); sections("extras"); proverbs(); sections().asSequence().filter{it.found}.forEach{readerGroups(it.id)}; conjugationQuestions(240) } }, "book-prewarm").apply { priority=Thread.MIN_PRIORITY; start() }
'''
if old not in s:
    raise SystemExit('prewarm insertion point not found')
s=s.replace(old,new,1)
p.write_text(s,encoding='utf-8')

# Review mode must not randomly lose previously marked wrong forms.
p=ui/'ConjugationTrainerScreen.kt'
s=p.read_text(encoding='utf-8')
old='''val all=remember(key){repository.conjugationQuestions(240)}'''
new='''val all=remember(key,reviewOnly){repository.conjugationQuestions(if(reviewOnly)Int.MAX_VALUE else 240)}'''
if old not in s:
    raise SystemExit('trainer all-questions pattern not found')
s=s.replace(old,new,1)
p.write_text(s,encoding='utf-8')

print('Conjugation table parser fixed.')
