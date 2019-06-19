package tanvd.grazi.utils

import org.languagetool.rules.RuleMatch
import tanvd.grazi.grammar.Typo

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> String.ifContains(value: String, body: (Int) -> T): T? {
    val index = value.indexOf(this)
    if (index != -1) {
        return body(index)
    }
    return null
}

fun <T> List<T>.dropFirst() = this.drop(1)

fun <T> List<T>.dropFirstIf(body: (T) -> Boolean) = this.getOrNull(0)?.let { if (body(it)) dropFirst() else this } ?: this

