// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.BlockEx
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.xml.AnotherLanguageBlockWrapper
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import org.jetbrains.vuejs.lang.expr.VueExprMetaLanguage
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START

class VueSyntheticBlock(subBlocks: List<Block>,
                        parent: Block,
                        indent: Indent?,
                        policy: XmlFormattingPolicy,
                        childIndent: Indent?,
                        private val myLanguage: Language?)
  : SyntheticBlock(subBlocks, parent, indent, policy, childIndent), BlockEx {

  override fun getLanguage(): Language? = myLanguage

  override fun getSpacing(child1: Block?, child2: Block): Spacing? =
    if (isVueInterpolationBorder(child1, child2) || isVueInterpolationBorder(child2, child1)) {
      val injectedWrapper = (child1 as? AnotherLanguageBlockWrapper ?: child2 as AnotherLanguageBlockWrapper)
      val spacesWithinInterpolation: Boolean
      val insertNewLine: Boolean
      myXmlFormattingPolicy.settings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        spacesWithinInterpolation = it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS
        insertNewLine = if (child1 !is AnotherLanguageBlockWrapper) it.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER
        else it.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER

      }
      val spaces = if (spacesWithinInterpolation) 1 else 0
      if (insertNewLine) {
        val prevBlock = findSibling(injectedWrapper, -1)
        val nextBlock = findSibling(injectedWrapper, 1)
        if (prevBlock != null && nextBlock != null) {
          Spacing.createDependentLFSpacing(spaces, spaces, TextRange(prevBlock.textRange.startOffset, nextBlock.textRange.endOffset),
                                           true, myXmlFormattingPolicy.keepBlankLines)
        }
        else {
          // Fallback for a very unlikely case of no prev or next block
          Spacing.createSpacing(spaces, spaces, 0, true, myXmlFormattingPolicy.keepBlankLines)
        }
      }
      else {
        Spacing.createSpacing(spaces, spaces, 0, true, myXmlFormattingPolicy.keepBlankLines)
      }
    }
    else {
      super.getSpacing(child1, child2)
    }

  override fun startsWithText(): Boolean =
    super.startsWithText()
    || myStartTreeNode.elementType == INTERPOLATION_START

  override fun endsWithText(): Boolean =
    super.endsWithText()
    || myEndTreeNode.elementType == INTERPOLATION_END

  private fun findSibling(block: Block, relativeIndex: Int): Block? {
    val subBlocks = subBlocks
    val ind = subBlocks.indexOf(block)
    if (ind >= 0 && ind + relativeIndex >= 0 && ind + relativeIndex < subBlocks.size) {
      return subBlocks[ind + relativeIndex]
    }
    return null
  }

  companion object {
    fun isVueInterpolationBorder(child1: Block?, child2: Block?): Boolean =
      (child1 is VueHtmlBlock || child1 is VueBlock)
      && VueExprMetaLanguage.matches((child2 as? AnotherLanguageBlockWrapper)?.node?.psi?.language)
  }
}
