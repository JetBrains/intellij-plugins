package tanvd.grazi.utils

import org.apache.commons.text.similarity.LevenshteinDistance

object Text {
    private val newLineCharRegex = Regex("\\n")

    fun containsBlank(str: String) = str.any { it.isWhitespace() }

    fun isNewline(char: Char) = newLineCharRegex.matches(char)

    private val urlRegex = Regex("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)")
    fun isURL(str: String) = urlRegex.matches(str)

    private val filePathRegex = Regex("([/A-z0-9-_+])*(/[A-z0-9-_+.]*)+")
    fun isFilePath(str: String) = filePathRegex.matches(str)

    fun isHiddenFile(str: String) = str.startsWith(".")
    fun isHtmlUnicodeSymbol(str: String) = str.startsWith("&")

    private val levenshtein = LevenshteinDistance()
    fun levenshteinDistance(str1: CharSequence, str2: CharSequence): Int = levenshtein.apply(str1, str2)

    fun isLatin(str: String) = str.matches(Regex("\\p{IsLatin}+"))
}

/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(vararg separators: Char, insideOf: IntRange? = null, consumer: (IntRange, String) -> Unit) = splitWithRanges(separators.toList(), insideOf, consumer = consumer)

/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(separators: List<Char>, insideOf: IntRange? = null, ignoreBlank: Boolean = true,
                           consumer: (IntRange, String) -> Unit) {
    val word = StringBuilder()
    val offset = insideOf?.start ?: 0
    for ((index, char) in this@splitWithRanges.withIndex()) {
        if (char in separators) {
            if (ignoreBlank && word.isBlank()) {
                word.clear()
                continue
            }
            consumer(IntRange(index - word.length, index).withOffset(offset), word.toString())
            word.clear()
            continue
        }
        word.append(char)
    }
    if (!ignoreBlank || word.isNotBlank()) {
        consumer(IntRange(this@splitWithRanges.length - word.length, this@splitWithRanges.length - 1).withOffset(offset), word.toString())
    }
}

fun Regex.matches(char: Char) = this.matches(char.toString())

