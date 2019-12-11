// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import org.apache.commons.text.similarity.LevenshteinDistance

object Text {
  fun isNewline(char: Char) = char == '\n'

  private val PUNCTUATIONS: Set<Byte> = setOf(Character.START_PUNCTUATION, Character.END_PUNCTUATION,
                                              Character.OTHER_PUNCTUATION, Character.CONNECTOR_PUNCTUATION,
                                              Character.DASH_PUNCTUATION, Character.INITIAL_QUOTE_PUNCTUATION,
                                              Character.FINAL_QUOTE_PUNCTUATION)

  fun isPunctuation(char: Char) = when (Character.getType(char).toByte()) {
    in PUNCTUATIONS -> true
    else -> false
  }

  fun isQuote(char: Char) = char == '\'' || char == '\"'

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
    }
  }
  if (word.isNotEmpty()) {
    consumer(IntRange(this@splitWithRanges.length - word.length, this@splitWithRanges.length - 1), word.toString())
  }
}

fun Regex.matches(char: Char) = this.matches(char.toString())

