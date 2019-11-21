// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import org.apache.commons.text.similarity.LevenshteinDistance

@Suppress("MemberVisibilityCanBePrivate")
object Text {
  private val newLineCharRegex = Regex("\\n")
  private val punctuationRegex = Regex("\\p{Punct}\\p{IsPunctuation}]")

  fun isNewline(char: Char) = newLineCharRegex.matches(char)

  fun isPunctuation(char: Char) = punctuationRegex.matches(char)

  fun isQuote(char: Char) = char in setOf('\'', '\"')

  object Levenshtein {
    private val levenshtein = LevenshteinDistance()

    fun distance(str1: CharSequence?, str2: CharSequence?): Int = levenshtein.apply(str1, str2)
  }
}

/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(vararg separators: Char, consumer: (IntRange, String) -> Unit) = splitWithRanges(
  separators.toList(), consumer = consumer)

/** Split by separators and return pairs of ranges to strings. Removes all blank lines from result */
fun String.splitWithRanges(separators: List<Char>, consumer: (IntRange, String) -> Unit) {
  val word = StringBuilder()
  for ((index, char) in this@splitWithRanges.withIndex()) {
    word.append(char)
    if (char in separators) {
      consumer(IntRange(index - (word.length - 1), index), word.toString())
      word.clear()
      continue
    }
  }
  if (word.isNotEmpty()) {
    consumer(IntRange(this@splitWithRanges.length - (word.length - 1), this@splitWithRanges.length - 1), word.toString())
  }
}

fun Regex.matches(char: Char) = this.matches(char.toString())

