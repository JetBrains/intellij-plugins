// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.LineSeparator

data class PrettierFormattingContext(
  val document: Document,
  val formattedContent: String,
  val detectedLineSeparator: LineSeparator?,
)

internal fun createFormattingContext(
  document: Document,
  formattedContent: String,
): PrettierFormattingContext {
  val detectedLineSeparator = StringUtil.detectSeparators(formattedContent)
  val normalized = StringUtil.convertLineSeparators(formattedContent, "\n")

  return PrettierFormattingContext(
    document = document,
    formattedContent = normalized,
    detectedLineSeparator = detectedLineSeparator,
  )
}
