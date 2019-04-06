package tanvd.grazi.utils

val punctuationChars = listOf('.', ',', ':', ';')

val blankCharRegex = Regex("\\s")
val newLineCharRegex = Regex("\\n")
val blankOrNewLineCharRegex = Regex("[\\n\\s]")

val blankWithNewLinesOrEmpty = Regex("[\\n\\s]*")
/** Considers whitespaces, tabs and newlines */
fun String.isBlankWithNewLines() = blankWithNewLinesOrEmpty.matches(this)

val camelCaseRegex = Regex("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")
fun String.splitCamelCase(insideOf: IntRange? = null) = buildList<Pair<IntRange, String>> {
    var index = insideOf?.start ?: 0
    for (word in split(camelCaseRegex)) {
        add(IntRange(index, index + word.length) to word)
        index += word.length
    }
}

val urlRegex = Regex("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)")
fun String.isUrl() = urlRegex.matches(this)
fun String.isHtmlPlainTextTag() = startsWith("&")


/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(vararg separators: Char, insideOf: IntRange? = null) = splitWithRanges(separators.toList(), insideOf)

/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(separators: List<Char>, insideOf: IntRange? = null, ignoreBlank: Boolean = true): List<Pair<IntRange, String>> = buildList {
    var word = ""
    val offset = insideOf?.start ?: 0
    for ((index, char) in this@splitWithRanges.withIndex()) {
        if (char in separators) {
            if (ignoreBlank && word.isBlank()) {
                word = ""
                continue
            }
            add(IntRange(index - word.length, index).withOffset(offset) to word)
            word = ""
            continue
        }
        word += char
    }
    if (!ignoreBlank || word.isNotBlank()) {
        add(IntRange(this@splitWithRanges.length - word.length, this@splitWithRanges.length - 1).withOffset(offset) to word)
    }
}

fun String.trimWithRange(trimmed: List<Char>, insideOf: IntRange? = null): Pair<IntRange?, String> {
    var start = 0
    while (start < length && this[start] in trimmed) {
        start++
    }
    var end = this.length - 1
    while (end >= 0 && this[end] in trimmed) {
        end--
    }
    val offset = insideOf?.start ?: 0
    if (start > end) {
        return null to ""
    }
    return IntRange(offset + start, offset + end) to this.subSequence(start, end + 1).toString()
}

fun Regex.matches(char: Char) = this.matches(char.toString())
