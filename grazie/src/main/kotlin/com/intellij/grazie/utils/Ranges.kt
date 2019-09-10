// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.utils

import com.intellij.openapi.util.TextRange
import com.intellij.grazie.grammar.Typo

fun Typo.toSelectionRange(): TextRange {
  val end = if (location.pointer?.element!!.textLength >= location.range.endInclusive + 1)
    location.range.endInclusive + 1
  else
    location.range.endInclusive
  return TextRange(location.range.start, end)
}

fun Typo.toAbsoluteSelectionRange(): TextRange {
  val end = if (location.pointer?.element!!.textLength >= location.range.endInclusive + 1)
    location.range.endInclusive + 1
  else
    location.range.endInclusive
  return location.element!!.textRange.cutOut(TextRange(location.range.start, end))
}
