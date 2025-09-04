// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.fragments.DiffFragment
import com.intellij.diff.tools.util.text.LineOffsetsUtil
import com.intellij.diff.util.DiffRangeUtil
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vcs.ex.compareLines

internal fun computeFormattingDiff(
  formattingContext: PrettierFormattingContext,
  snapshot: CaretSnapshot?,
): List<PrettierDiffFragment> {
  val document = formattingContext.document
  val original = document.charsSequence
  val formatted = formattingContext.formattedContent

  val markerLines: Set<Int> = buildSet {
    snapshot?.primary?.takeIf { it.isValid }?.let { rm ->
      add(document.getLineNumber(rm.startOffset))
    }
    snapshot?.secondary?.takeIf { it.isValid }?.let { rm ->
      add(document.getLineNumber(rm.endOffset))
    }
  }

  val originalLines = LineOffsetsUtil.create(document)
  val formattedLines = LineOffsetsUtil.create(formatted)

  val lineIterable = compareLines(
    original,
    formatted,
    originalLines,
    formattedLines,
  )

  return buildList {
    fun addFragment(
      startOffset1: Int,
      endOffset1: Int,
      startOffset2: Int,
      endOffset2: Int,
      isCharFragment: Boolean = false,
    ) {
      add(
        PrettierDiffFragment(
          startOffset1,
          endOffset1,
          startOffset2,
          endOffset2,
          isCharFragment,
        )
      )
    }

    val originalLength = original.length

    for (change in lineIterable.iterateChanges()) {
      ProgressManager.checkCanceled()

      if (change.isEmpty) {
        continue
      }

      val s1Line = change.start1
      val e1Line = change.end1
      val s2Line = change.start2
      val e2Line = change.end2

      val originalRange = DiffRangeUtil.getLinesRange(originalLines, s1Line, e1Line, false)
      val formattedRange = DiffRangeUtil.getLinesRange(formattedLines, s2Line, e2Line, false)

      // insertion (no lines on original side)
      if (s1Line == e1Line) {
        val insertAt = if (s1Line == document.lineCount) originalLength else document.getLineStartOffset(s1Line)
        addFragment(
          insertAt,
          insertAt,
          formattedRange.startOffset,
          formattedRange.endOffset,
        )
        continue
      }

      // deletion (no lines on formatted side)
      if (s2Line == e2Line) {
        var startOffset = originalRange.startOffset
        var endOffset = originalRange.endOffset

        if (startOffset > 0) {
          startOffset--
        }
        else if (endOffset < document.textLength) {
          endOffset++
        }

        addFragment(
          startOffset,
          endOffset,
          formattedRange.startOffset,
          formattedRange.endOffset,
        )
        continue
      }

      val isLineNeedingChar = markerLines.any { it in s1Line until e1Line }
      // replacement on both sides
      if (!isLineNeedingChar) {
        addFragment(
          originalRange.startOffset,
          originalRange.endOffset,
          formattedRange.startOffset,
          formattedRange.endOffset
        )
        continue
      }

      // if we have marker lines, we need to compare chars on both sides otherwise we can invalidate markers
      val charFragments = ComparisonManager.getInstance().compareChars(
        original.subSequence(originalRange.startOffset, originalRange.endOffset),
        formatted.subSequence(formattedRange.startOffset, formattedRange.endOffset),
        ComparisonPolicy.DEFAULT,
        DumbProgressIndicator.INSTANCE,
      )

      for (charFragment in charFragments) {
        addFragment(
          startOffset1 = originalRange.startOffset + charFragment.startOffset1,
          endOffset1 = originalRange.startOffset + charFragment.endOffset1,
          startOffset2 = formattedRange.startOffset + charFragment.startOffset2,
          endOffset2 = formattedRange.startOffset + charFragment.endOffset2,
          isCharFragment = true,
        )
      }
    }
  }
}

internal data class PrettierDiffFragment(
  override val startOffset1: Int,
  override val endOffset1: Int,
  override val startOffset2: Int,
  override val endOffset2: Int,
  val isCharFragment: Boolean,
) : DiffFragment
