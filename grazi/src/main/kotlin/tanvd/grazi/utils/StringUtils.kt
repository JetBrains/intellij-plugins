package tanvd.grazi.utils

import tanvd.kex.buildList

val punctuationChars = listOf('.', ',', ':', ';')

val blankCharRegex = Regex("\\s")
val newLineCharRegex = Regex("\\n")
val blankOrNewLineCharRegex = Regex("[\\n\\s]")

val blankWithNewLinesOrEmpty = Regex("[\\n\\s]*")
/** Considers whitespaces, tabs and newlines */
fun String.isBlankWithNewLines() = blankWithNewLinesOrEmpty.matches(this)

val urlRegex = Regex("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)")
fun String.isUrl() = urlRegex.matches(this)
fun String.isHtmlPlainTextTag() = startsWith("&")

val filePathRegex = Regex("([/A-z0-9-_+])*(/[A-z0-9-_+.]*)+")
fun String.isFilePath() = filePathRegex.matches(this)


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

fun Regex.matches(char: Char) = this.matches(char.toString())
