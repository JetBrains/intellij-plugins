// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeStyle.AbstractConvertLineSeparatorsAction
import com.intellij.diff.comparison.iterables.FairDiffIterable
import com.intellij.diff.tools.util.text.LineOffsets
import com.intellij.diff.tools.util.text.LineOffsetsUtil
import com.intellij.diff.util.DiffRangeUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.ex.compareLines
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.LineSeparator

internal data class FormattingDiff(
  val textDifferences: FairDiffIterable,
  val originalLineOffsets: LineOffsets,
  val formattedLineOffsets: LineOffsets,
  val normalizedFormattedContent: String,
  val contentLengthDelta: Int,
  val detectedLineSeparator: LineSeparator?,
  val cursorOffset: Int,
)

internal fun computeFormattingDiff(
  originalContent: CharSequence,
  formattedContent: String,
  cursorOffset: Int,
): FormattingDiff {
  val detectedLineSeparator = StringUtil.detectSeparators(formattedContent)
  val offsetsToKeep = intArrayOf(cursorOffset)
  val normalizedFormattedContent = StringUtil.convertLineSeparators(formattedContent, "\n", offsetsToKeep)

  val originalLineOffsets = LineOffsetsUtil.create(originalContent)
  val formattedLineOffsets = LineOffsetsUtil.create(normalizedFormattedContent)
  val textDifferences = compareLines(originalContent, normalizedFormattedContent, originalLineOffsets, formattedLineOffsets)
  val contentLengthDelta = normalizedFormattedContent.length - originalContent.length

  return FormattingDiff(
    textDifferences,
    originalLineOffsets,
    formattedLineOffsets,
    normalizedFormattedContent,
    contentLengthDelta,
    detectedLineSeparator,
    offsetsToKeep[0]
  )
}

internal fun applyFormattingDiff(
  project: Project,
  document: Document,
  virtualFile: VirtualFile,
  formattingDiff: FormattingDiff,
): Boolean {
  applyTextDifferencesToDocument(document, formattingDiff)
  return updateLineSeparatorIfNeeded(project, virtualFile, formattingDiff.detectedLineSeparator)
}

internal fun applyTextDifferencesToDocument(
  document: Document,
  formattingDiff: FormattingDiff,
) {
  val textDifferences = formattingDiff.textDifferences
  val originalLineOffsets = formattingDiff.originalLineOffsets
  val formattedLineOffsets = formattingDiff.formattedLineOffsets
  val formattedContent = formattingDiff.normalizedFormattedContent

  for (change in textDifferences.iterateChanges().reversed()) {
    if (change.isEmpty) {
      continue
    }

    val originalStartLine = change.start1
    val originalEndLine = change.end1
    val formattedStartLine = change.start2
    val formattedEndLine = change.end2

    when {
      originalStartLine == originalEndLine -> {
        val insertionText = DiffRangeUtil.getLinesRange(
          formattedLineOffsets,
          formattedStartLine,
          formattedEndLine,
          false,
        ).subSequence(formattedContent)

        val offset = if (originalStartLine == document.lineCount) {
          document.textLength
        }
        else {
          document.getLineStartOffset(originalStartLine)
        }

        document.insertString(offset, "$insertionText\n")
      }
      formattedStartLine == formattedEndLine -> {
        val range = DiffRangeUtil.getLinesRange(
          originalLineOffsets,
          originalStartLine,
          originalEndLine,
          false,
        )

        var startOffset = range.startOffset
        var endOffset = range.endOffset

        if (startOffset > 0) {
          startOffset--
        }
        else if (endOffset < document.textLength) {
          endOffset++
        }

        document.deleteString(startOffset, endOffset)
      }
      else -> {
        val replacementText = DiffRangeUtil.getLinesRange(
          formattedLineOffsets,
          formattedStartLine,
          formattedEndLine,
          false,
        ).subSequence(formattedContent)

        val range = DiffRangeUtil.getLinesRange(originalLineOffsets, originalStartLine, originalEndLine, false)
        document.replaceString(range.startOffset, range.endOffset, replacementText)
      }
    }
  }
}

internal fun updateLineSeparatorIfNeeded(
  project: Project,
  virtualFile: VirtualFile,
  newSeparator: LineSeparator?,
): Boolean {
  if (newSeparator != null && virtualFile.detectedLineSeparator != newSeparator.separatorString) {
    AbstractConvertLineSeparatorsAction.changeLineSeparators(project, virtualFile, newSeparator.separatorString)
    return true
  }
  return false
}
