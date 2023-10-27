// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.xml.AnotherLanguageBlockWrapper
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.expr.Angular2Language

internal fun getSpacingIfInterpolationBorder(
  child1: Block?, child2: Block,
  xmlFormattingPolicy: XmlFormattingPolicy,
  subBlocksProvider: () -> List<Block>
): Spacing? =
  if (isAngularInterpolationBorder(child1, child2) || isAngularInterpolationBorder(child2, child1)) {
    val injectedWrapper = (child1 as? AnotherLanguageBlockWrapper ?: child2 as AnotherLanguageBlockWrapper)
    val spacesWithinInterpolation: Boolean
    val insertNewLine: Boolean
    xmlFormattingPolicy.settings.getCustomSettings(Angular2HtmlCodeStyleSettings::class.java).let {
      spacesWithinInterpolation = it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS
      insertNewLine = if (child1 !is AnotherLanguageBlockWrapper) it.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER
      else it.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER

    }
    val spaces = if (spacesWithinInterpolation) 1 else 0
    if (insertNewLine) {
      val prevBlock = findSibling(injectedWrapper, -1, subBlocksProvider)
      val nextBlock = findSibling(injectedWrapper, 1, subBlocksProvider)
      if (prevBlock != null && nextBlock != null) {
        Spacing.createDependentLFSpacing(spaces, spaces, TextRange(prevBlock.textRange.startOffset, nextBlock.textRange.endOffset),
                                         true, xmlFormattingPolicy.keepBlankLines)
      }
      else {
        // Fallback for a very unlikely case of no prev or next block
        Spacing.createSpacing(spaces, spaces, 0, true, xmlFormattingPolicy.keepBlankLines)
      }
    }
    else {
      Spacing.createSpacing(spaces, spaces, 0, true, xmlFormattingPolicy.keepBlankLines)
    }
  }
  else null

private fun findSibling(block: Block, relativeIndex: Int, subBlocksProvider: () -> List<Block>): Block? {
  val subBlocks = subBlocksProvider()
  val ind = subBlocks.indexOf(block)
  if (ind >= 0 && ind + relativeIndex >= 0 && ind + relativeIndex < subBlocks.size) {
    return subBlocks[ind + relativeIndex]
  }
  return null
}

private fun isAngularInterpolationBorder(child1: Block?, child2: Block?): Boolean =
  child1 is Angular2HtmlFormattingBlock
  && (child2 as? AnotherLanguageBlockWrapper)?.node?.psi?.let { it.language is Angular2Language && it.parent !is XmlAttribute } == true

