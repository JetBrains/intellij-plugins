// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import org.languagetool.rules.RuleMatch
import tanvd.kex.LinkedSet

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> List<T>.dropFirstIf(body: (T) -> Boolean) = this.firstOrNull()?.let { if (body(it)) drop(1) else this } ?: this

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
