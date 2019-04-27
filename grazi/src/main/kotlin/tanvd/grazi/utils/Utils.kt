package tanvd.grazi.utils

import org.languagetool.rules.RuleMatch
import tanvd.grazi.grammar.Typo

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> tryRun(body: () -> T): T? = try {
    body()
} catch (e: Throwable) {
    null
}

fun Boolean?.orFalse(): Boolean = this ?: false
fun Boolean?.orTrue(): Boolean = this ?: true

fun <T, E> Collection<T>.firstNotNull(body: (T) -> E?): E? {
    for (value in this) {
        val result = body(value)
        if (result != null) {
            return result
        }
    }
    return null
}

fun <T> Boolean.ifTrue(body: () -> T): T? = if (this) body() else null

fun <T> String.ifContains(value: String, body: (Int) -> T): T? {
    val index = value.indexOf(this)
    if (index != -1) {
        return body(index)
    }
    return null
}

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <T> List<T>.dropFirst() = this.drop(1)

fun <T> List<T>.dropFirstIf(body: (T) -> Boolean) = this.getOrNull(0)?.let { if (body(it)) dropFirst() else this } ?: this

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

