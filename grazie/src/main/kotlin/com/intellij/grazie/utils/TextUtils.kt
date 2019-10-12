// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import org.apache.commons.text.similarity.LevenshteinDistance

@Suppress("MemberVisibilityCanBePrivate")
object Text {
  private val newLineCharRegex = Regex("\\n")

  fun isNewline(char: Char) = newLineCharRegex.matches(char)

  fun isQuote(char: Char) = char in setOf('\'', '\"')

  object Levenshtein {
    private val levenshtein = LevenshteinDistance()

    fun distance(str1: CharSequence?, str2: CharSequence?): Int = levenshtein.apply(str1, str2)
  }

  fun quotesOffset(str: CharSequence): Int {
    var index = 0
    while (index < str.length / 2) {
      if (str[index] != str[str.length - index - 1] || !isQuote(str[index])) {
        return index
      }
      index++
    }

    return index
  }
}

/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(vararg separators: Char, insideOf: IntRange? = null, consumer: (IntRange, String) -> Unit) = splitWithRanges(
  separators.toList(), insideOf, consumer = consumer)

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

