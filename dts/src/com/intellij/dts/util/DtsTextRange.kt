package com.intellij.dts.util

import com.intellij.openapi.util.TextRange
import kotlin.math.max
import kotlin.math.min

/**
 * Used to trim the ends of text range, like for string values to remove the
 * trailing "...".
 */
fun TextRange.trimEnds(): TextRange {
    if (length < 2) return this

    return grown(-2).shiftRight(1)
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