// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import org.languagetool.rules.RuleMatch

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun Iterable<*>.joinToStringWithOxfordComma(separator: String = ", ") = with(toList()) {
  if (size > 1) {
    dropLast(1).joinToString(separator, postfix = ", ") + "and ${last()}"
  }
  else {
    last().toString()
  }
}

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

fun <T> Collection<T>.toLinkedSet() = LinkedSet<T>(this)

typealias LinkedSet<T> = LinkedHashSet<T>

val IntRange.length
  get() = endInclusive - start + 1
