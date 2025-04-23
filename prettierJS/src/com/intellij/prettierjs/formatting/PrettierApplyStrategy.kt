// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.codeStyle.AbstractConvertLineSeparatorsAction
import com.intellij.diff.util.DiffRangeUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.LineSeparator

internal interface PrettierApplyFormattingStrategy {
  fun apply(project: Project, file: VirtualFile, context: PrettierFormattingContext): Boolean

  companion object {
    fun from(context: PrettierFormattingContext): PrettierApplyFormattingStrategy {
      return if (Registry.`is`("prettier.use.diff.formatting")) {
        val diff = computeFormattingDiff(context)
        DiffBased(diff)
      }
      else {
        ReplaceAll
      }
    }
  }

  class DiffBased(
    private val formattingDiff: PrettierFormattingDiff,
  ) : PrettierApplyFormattingStrategy {
    override fun apply(project: Project, virtualFile: VirtualFile, context: PrettierFormattingContext): Boolean {
      applyTextDifferencesToDocument(context, formattingDiff)
      return updateLineSeparatorIfNeeded(project, virtualFile, context.detectedLineSeparator)
    }
  }

  object ReplaceAll : PrettierApplyFormattingStrategy {
    override fun apply(project: Project, virtualFile: VirtualFile, context: PrettierFormattingContext): Boolean {
      context.document.setText(context.formattedContent)
      return updateLineSeparatorIfNeeded(project, virtualFile, context.detectedLineSeparator)
    }
  }
}

internal fun applyTextDifferencesToDocument(
  formattingContext: PrettierFormattingContext,
  formattingDiff: PrettierFormattingDiff,
) {
  val document = formattingContext.document
  val textDifferences = formattingDiff.textDifferences
  val originalLineOffsets = formattingDiff.originalLineOffsets
  val formattedLineOffsets = formattingDiff.formattedLineOffsets
  val formattedText = formattingContext.formattedContent

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
        ).subSequence(formattedText)

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
        ).subSequence(formattedText)

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
