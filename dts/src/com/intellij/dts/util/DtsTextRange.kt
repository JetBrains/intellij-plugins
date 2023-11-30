package com.intellij.dts.util

import com.intellij.openapi.util.TextRange
import kotlin.math.max
import kotlin.math.min

/**
 * Returns a text range having leading and trailing characters from the chars
 * array removed.
 */
fun TextRange.trim(value: String, vararg chars: Char): TextRange {
  var result = this

  val startTrimmed = value.trimStart(*chars)
  val shiftRight = value.length - startTrimmed.length
  result = TextRange(result.startOffset + shiftRight, result.endOffset)

  val endTrimmed = startTrimmed.trimEnd(*chars)
  val shiftLeft = startTrimmed.length - endTrimmed.length
  result = TextRange(result.startOffset, result.endOffset - shiftLeft)

  return result
}

/**
 * Makes a text range relative to a parent text range and ensures that the range
 * fits into the parent by cropping if necessary. Returns a range of length 0 if
 * the text range lies outside the parent.
 */
fun TextRange.relativeTo(parent: TextRange): TextRange {
  val start = max(0, startOffset - parent.startOffset)
  val end = max(0, endOffset - parent.startOffset)

  return TextRange(
    min(parent.length, start),
    min(parent.length, end),
  )
}