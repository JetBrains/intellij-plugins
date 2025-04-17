// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.LineSeparator

data class PrettierFormattingContext(
  val document: Document,
  val formattedContent: String,
  val detectedLineSeparator: LineSeparator?,
  val cursorOffset: Int,
  val contentLengthDelta: Int,
)

internal fun createFormattingContext(
  document: Document,
  formattedContent: String,
  cursorOffset: Int,
): PrettierFormattingContext {
  val detectedLineSeparator = StringUtil.detectSeparators(formattedContent)
  val offsetsToKeep = intArrayOf(cursorOffset)
  val normalized = StringUtil.convertLineSeparators(formattedContent, "\n", offsetsToKeep)
  val contentLengthDelta = formattedContent.length - document.charsSequence.length

  return PrettierFormattingContext(
    document = document,
    formattedContent = normalized,
    detectedLineSeparator = detectedLineSeparator,
    contentLengthDelta = contentLengthDelta,
    cursorOffset = offsetsToKeep[0]
  )
}
