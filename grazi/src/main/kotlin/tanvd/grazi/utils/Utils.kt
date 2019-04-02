package tanvd.grazi.utils

import org.languagetool.rules.RuleMatch
import tanvd.grazi.grammar.Typo

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> tryRun(body: () -> T): T? = try {
    body()
} catch (e: Exception) {
    null
}

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <T> List<T>.dropFirst() = this.drop(1)

fun <T> buildList(body: MutableList<T>.() -> Unit): List<T> {
    val result = ArrayList<T>()
    result.body()
    return result
}

fun <T> buildSet(body: MutableSet<T>.() -> Unit): Set<T> {
    val result = LinkedHashSet<T>()
    result.body()
    return result
}
