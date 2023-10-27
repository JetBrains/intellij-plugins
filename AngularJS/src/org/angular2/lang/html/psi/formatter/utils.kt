// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.formatter.xml.AnotherLanguageBlockWrapper
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.blocks.getAngular2HtmlBlocksConfig
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock

internal fun createAngularBlockChild(child: ASTNode,
                                     indent: Indent?,
                                     wrap: Wrap?,
                                     alignment: Alignment?,
                                     range: TextRange?,
                                     xmlFormattingPolicy: XmlFormattingPolicy,
                                     preserveSpace: Boolean): XmlBlock =
  when (child.elementType) {
    Angular2HtmlTokenTypes.BLOCK_NAME,
    Angular2HtmlElementTypes.BLOCK_PARAMETERS,
    Angular2HtmlTokenTypes.BLOCK_START,
    Angular2HtmlTokenTypes.BLOCK_END ->
      Angular2HtmlFormattingBlock(child, null, null, xmlFormattingPolicy, Indent.getNoneIndent(), range, false)
    else -> Angular2HtmlFormattingBlock(child, null, null, xmlFormattingPolicy, Indent.getNormalIndent(), range, preserveSpace)
  }

internal fun getSpacingWithinAngularBlock(parent: ASTNode,
                                          child1: Block?,
                                          child2: Block,
                                          xmlFormattingPolicy: XmlFormattingPolicy): Spacing? {
  if (child1 !is AbstractBlock || child2 !is AbstractBlock) {
    return null
  }
  val elementType1 = child1.node.elementType
  val elementType2 = child2.node.elementType
  return if (elementType1 == Angular2HtmlElementTypes.BLOCK && elementType2 == Angular2HtmlElementTypes.BLOCK) {
    val block1 = child1.node.psi as Angular2HtmlBlock
    val block2 = child2.node.psi as Angular2HtmlBlock
    val blocksConfig = getAngular2HtmlBlocksConfig(block1)
    val block1PrimaryBlock = blocksConfig.definitions[block1.getName()]?.let { if (it.isPrimary) it.name else it.primaryBlock }
    val block2PrimaryBlock = blocksConfig.definitions[block2.getName()]?.let { if (it.isPrimary) it.name else it.primaryBlock }
    if (block1PrimaryBlock == block2PrimaryBlock && blocksConfig.definitions[block1PrimaryBlock]?.hasNestedSecondaryBlocks != true) {
      Spacing.createSpacing(1, 1, 0, false, 0)
    }
    else {
      Spacing.createSpacing(0, 0, 1, false,
                            if (block1PrimaryBlock == block2PrimaryBlock) 0 else xmlFormattingPolicy.keepBlankLines)
    }
  }
  else when (parent.elementType) {
    Angular2HtmlElementTypes.BLOCK -> {
      when (elementType1) {
        Angular2HtmlTokenTypes.BLOCK_NAME,
        Angular2HtmlElementTypes.BLOCK_PARAMETERS -> {
          Spacing.createSpacing(1, 1, 0, false, xmlFormattingPolicy.keepBlankLines)
        }
        Angular2HtmlTokenTypes.BLOCK_START -> {
          Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
        }
        else -> when (elementType2) {
          Angular2HtmlTokenTypes.BLOCK_END -> {
            Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
          }
          else -> null
        }
      }
    }
    Angular2HtmlElementTypes.BLOCK_PARAMETERS -> {
      Spacing.createSpacing(0, 0, 0, false, xmlFormattingPolicy.keepBlankLines)
    }
    else -> null
  }
}

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

