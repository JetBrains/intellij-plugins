package tanvd.grazi.grammar

import org.languagetool.rules.RuleMatch
import tanvd.grazi.model.Typo

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> tryRun(body: () -> T): T? = try {
    body()
} catch (e: Exception) {
    null
}

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <T> List<T>.dropFirst() = this.drop(1)
