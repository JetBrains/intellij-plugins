// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.diff.comparison.iterables.FairDiffIterable
import com.intellij.diff.tools.util.text.LineOffsets
import com.intellij.diff.tools.util.text.LineOffsetsUtil
import com.intellij.openapi.vcs.ex.compareLines

internal data class PrettierFormattingDiff(
  val textDifferences: FairDiffIterable,
  val originalLineOffsets: LineOffsets,
  val formattedLineOffsets: LineOffsets,
)

internal fun computeFormattingDiff(
  formattingContext: PrettierFormattingContext,
): PrettierFormattingDiff {
  val originalLineOffsets = LineOffsetsUtil.create(formattingContext.document)
  val formattedLineOffsets = LineOffsetsUtil.create(formattingContext.formattedContent)
  val textDifferences = compareLines(formattingContext.document.charsSequence, formattingContext.formattedContent, originalLineOffsets, formattedLineOffsets)

  return PrettierFormattingDiff(
    textDifferences,
    originalLineOffsets,
    formattedLineOffsets,
  )
}