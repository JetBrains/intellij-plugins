// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import org.languagetool.rules.RuleMatch
import com.intellij.grazie.utils.LinkedSet

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun String.safeSubstring(startIndex: Int) = if (this.length <= startIndex) "" else substring(startIndex)

fun Iterable<*>.joinToStringWithOxfordComma(separator: String = ", ") = with(toList()) {
  if (size > 1) {
    dropLast(1).joinToString(separator, postfix = ", ") + "and ${last()}"
  }
  else {
    last().toString()
  }
}

fun String.decapitalizeIfNotAbbreviation() = if (length > 1 && get(1).isUpperCase()) this else decapitalize()

fun <T> Iterable<T>.filterToSet(filter: (T) -> Boolean) = asSequence().filter(filter).toCollection(LinkedSet())
fun <T> Iterable<T>.filterNotToSet(filterNot: (T) -> Boolean) = asSequence().filterNot(filterNot).toCollection(LinkedSet())

fun Boolean?.orTrue() = this ?: true
fun Boolean?.orFalse() = this ?: false

fun <T> Boolean.ifTrue(body: () -> T): T? = if (this) {
  body()
}
else {
  null
}

fun String.trimToNull(): String? = takeIf(String::isNotBlank)

fun <T> buildList(body: MutableList<T>.() -> Unit): List<T> {
  val result = ArrayList<T>()
  result.body()
  return result
}

fun <T> buildSet(body: MutableSet<T>.() -> Unit): Set<T> {
  val result = LinkedSet<T>()
  result.body()
  return result
}

typealias LinkedSet<T> = LinkedHashSet<T>
