package tanvd.grazi.utils

import org.languagetool.rules.RuleMatch

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> List<T>.dropFirstIf(body: (T) -> Boolean) = this.firstOrNull()?.let { if (body(it)) drop(1) else this } ?: this

fun String.filterOutNewLines() = this.replace("\n", "")

fun String.safeSubstring(startIndex: Int) = if (this.length <= startIndex) "" else substring(startIndex)

fun List<*>.joinToStringWithOxfordComma(separator: String = ", ") = if (size > 1) dropLast(1).joinToString(separator, postfix = ", ") + "and ${last()}" else last().toString()
