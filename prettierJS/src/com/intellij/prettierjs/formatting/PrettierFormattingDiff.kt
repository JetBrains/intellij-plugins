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
  val markerOffsets: List<Int> = buildList {
    snapshot?.primary?.takeIf { it.isValid }?.let { rm ->
      add(rm.startOffset)
    }
    snapshot?.secondary?.takeIf { it.isValid }?.let { rm ->
      add(rm.startOffset)
      add(rm.endOffset)
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

      val blockFragments = charFragments.map { charFragment ->
        PrettierDiffFragment(
          startOffset1 = originalRange.startOffset + charFragment.startOffset1,
          endOffset1 = originalRange.startOffset + charFragment.endOffset1,
          startOffset2 = formattedRange.startOffset + charFragment.startOffset2,
          endOffset2 = formattedRange.startOffset + charFragment.endOffset2,
          isCharFragment = true,
        )
      }
      addAll(joinFragmentsBetweenOffsets(blockFragments, markerOffsets))
    }
  }
}

/**
 * Joins the [fragments] into as few fragments as the [offsets] allow.
 * A fragment that touches an offset stays as it is, and a joined fragment never spans an offset.
 * A range marker at an offset thus moves the same way as it does with the full list.
 * The [fragments] must be sorted by offset and must not overlap.
 */
internal fun joinFragmentsBetweenOffsets(fragments: List<PrettierDiffFragment>, offsets: List<Int>): List<PrettierDiffFragment> {
  val result = ArrayList<PrettierDiffFragment>()
  var groupStart = 0
  for (index in fragments.indices) {
    val fragment = fragments[index]
    val touchesOffset = offsets.any { it in fragment.startOffset1..fragment.endOffset1 }
    val gapHoldsOffset = index > groupStart && offsets.any { it in fragments[index - 1].endOffset1..fragment.startOffset1 }
    if (touchesOffset || gapHoldsOffset) {
      if (index > groupStart) result.add(joinFragments(fragments, groupStart, index))
      groupStart = index
    }
    if (touchesOffset) {
      result.add(fragment)
      groupStart = index + 1
    }
  }
  if (groupStart < fragments.size) result.add(joinFragments(fragments, groupStart, fragments.size))
  return result
}

/** One fragment that covers the fragments from [from] inclusive to [to] exclusive and the equal text between them. */
private fun joinFragments(fragments: List<PrettierDiffFragment>, from: Int, to: Int): PrettierDiffFragment {
  val first = fragments[from]
  val last = fragments[to - 1]
  return if (first === last) first else first.copy(endOffset1 = last.endOffset1, endOffset2 = last.endOffset2)
}

internal data class PrettierDiffFragment(
  override val startOffset1: Int,
  override val endOffset1: Int,
  override val startOffset2: Int,
  override val endOffset2: Int,
  val isCharFragment: Boolean,
) : DiffFragment
