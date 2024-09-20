// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.formatter.xml.AnotherLanguageBlockWrapper
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import com.intellij.util.asSafely
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock

internal object Angular2HtmlFormattingHelper {

  fun isAngular2AttributeElementType(elementType: IElementType): Boolean =
    elementType === XmlElementType.XML_ATTRIBUTE
    || Angular2HtmlElementTypes.ALL_ATTRIBUTES.contains(elementType)

  fun createSimpleChild(
    parent: ASTNode, child: ASTNode, indent: Indent?, wrap: Wrap?,
    alignment: Alignment?, range: TextRange?, xmlFormattingPolicy: XmlFormattingPolicy,
    preserveSpace: Boolean,
  ): XmlBlock =
    when (parent.elementType) {
      Angular2HtmlElementTypes.BLOCK -> {
        Angular2HtmlFormattingBlock(child, null, alignment, xmlFormattingPolicy, Indent.getNoneIndent(), range, false)
      }
      Angular2HtmlElementTypes.BLOCK_CONTENTS -> {
        when (child.elementType) {
          Angular2HtmlTokenTypes.BLOCK_START, Angular2HtmlTokenTypes.BLOCK_END -> {
            Angular2HtmlFormattingBlock(child, null, alignment, xmlFormattingPolicy, Indent.getNoneIndent(), range, false)
          }
          else -> {
            Angular2HtmlFormattingBlock(child, null, alignment, xmlFormattingPolicy, Indent.getNormalIndent(), range, preserveSpace)
          }
        }
      }
      else -> {
        Angular2HtmlFormattingBlock(child, wrap, alignment, xmlFormattingPolicy, indent, range, preserveSpace)
      }
    }

  fun createTagBlock(
    parent: ASTNode, child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?,
    xmlFormattingPolicy: XmlFormattingPolicy, preserveSpace: Boolean,
  ): XmlTagBlock =
    if (parent.elementType == Angular2HtmlElementTypes.BLOCK_CONTENTS) {
      Angular2HtmlTagBlock(child, wrap, alignment, xmlFormattingPolicy, Indent.getNormalIndent(), preserveSpace)
    }
    else {
      Angular2HtmlTagBlock(child, wrap, alignment, xmlFormattingPolicy, indent ?: Indent.getNoneIndent(), preserveSpace)
    }

  fun processChild(
    parent: Block,
    result: MutableList<Block>,
    child: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    indent: Indent?,
    xmlFormattingPolicy: XmlFormattingPolicy,
    preserveSpace: Boolean,
    originalProcessChild: (MutableList<Block>, ASTNode, Wrap?, Alignment?, Indent?) -> ASTNode?,
  ): ASTNode? {
    if (child.elementType == Angular2HtmlElementTypes.BLOCK) {
      val firstBlock = child.psi as Angular2HtmlBlock
      if (firstBlock.isPrimary) {
        val subBlocks = mutableListOf<Block>(
          Angular2HtmlFormattingBlock(child, null, null, xmlFormattingPolicy, Indent.getNoneIndent(), null, preserveSpace)
        )
        firstBlock.blockSiblingsForward()
          .mapTo(subBlocks) {
            Angular2HtmlFormattingBlock(it.node, null, null, xmlFormattingPolicy, Indent.getNoneIndent(), null, preserveSpace)
          }
        val blocksGroupIndent = if (child.treeParent.elementType == XmlElementType.HTML_DOCUMENT) Indent.getNoneIndent() else Indent.getNormalIndent()
        result.add(Angular2SyntheticBlock(subBlocks, parent, blocksGroupIndent, xmlFormattingPolicy, Indent.getNoneIndent(), true))
        return (subBlocks.last() as Angular2HtmlFormattingBlock).node
      }
    }
    return originalProcessChild(result, child, wrap, alignment, indent)
  }

  fun getSpacingWithinTag(
    parent: ASTNode?,
    child1: Block?, child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
  ): Spacing? =
    getSpacingBetweenTagsAndBlocksWithinTag(child1, child2, xmlFormattingPolicy)

  fun getSpacing(
    parent: ASTNode?,
    child1: Block?, child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
    subBlocksProvider: () -> List<Block>,
  ): Spacing? =
    getSpacingBetweenAngularBlockGroups(child1, child2, xmlFormattingPolicy)
    ?: getSpacingBetweenAngularBlocks(child1, child2)
    ?: getSpacingAroundLetBlocks(child1, child2, xmlFormattingPolicy)
    ?: getSpacingWithinAngularBlock(parent, child1, child2, xmlFormattingPolicy)
    ?: getSpacingIfInterpolationBorder(child1, child2, xmlFormattingPolicy, subBlocksProvider)

  fun getChildAttributes(block: Angular2HtmlFormattingBlock): ChildAttributes? =
    when (block.node.elementType) {
      Angular2HtmlElementTypes.BLOCK_CONTENTS, Angular2HtmlElementTypes.BLOCK_PARAMETERS -> {
        ChildAttributes(Indent.getNormalIndent(), null)
      }
      else -> null
    }

  private fun getSpacingBetweenAngularBlockGroups(
    child1: Block?,
    child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
  ): Spacing? =
    if (child1 is Angular2SyntheticBlock && child2 is Angular2SyntheticBlock
        && child1.isBlockGroup && child2.isBlockGroup)
      Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
    else
      null

  private fun getSpacingAroundLetBlocks(
    child1: Block?,
    child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
  ): Spacing? =
    if (isLetBlock(child1) || isLetBlock(child2))
      Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
    else
      null

  private fun getSpacingBetweenTagsAndBlocksWithinTag(
    child1: Block?,
    child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
  ): Spacing? =
    if ((endsWithLetBlockWithinTag(child1) && child2 is Angular2SyntheticBlock && child2.isStartOfTag)
        || (child1 is Angular2SyntheticBlock && child1.isEndOfTag && startsWithLetBlockWithinTag(child2)))
      Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
    else
      null

  private fun endsWithLetBlockWithinTag(block: Block?):Boolean =
    block is Angular2SyntheticBlock
    && isLetBlock(block.subBlocks.lastOrNull())

  private fun startsWithLetBlockWithinTag(block: Block?):Boolean =
    block is Angular2SyntheticBlock
    && isLetBlock(block.subBlocks.firstOrNull())

  private fun isLetBlock(block: Block?): Boolean =
    block is Angular2SyntheticBlock
    && block.isBlockGroup
    && block.subBlocks[0]?.asSafely<AbstractBlock>()
      ?.node?.psi?.asSafely<Angular2HtmlBlock>()
      ?.name == BLOCK_LET

  private fun getSpacingBetweenAngularBlocks(
    child1: Block?,
    child2: Block,
  ): Spacing? =
    if ((child1 as? AbstractBlock)?.node?.elementType == Angular2HtmlElementTypes.BLOCK
        && (child2 as? AbstractBlock)?.node?.elementType == Angular2HtmlElementTypes.BLOCK) {
      val block1 = child1.node.psi as Angular2HtmlBlock
      // Blocks from the same primary block are always grouped together within a synthetic block - see processChild above
      if (block1.primaryBlockDefinition?.hasNestedSecondaryBlocks != true) {
        Spacing.createSpacing(1, 1, 0, false, 0)
      }
      else {
        Spacing.createSpacing(0, 0, 1, false, 0)
      }
    }
    else null

  private fun getSpacingWithinAngularBlock(
    parent: ASTNode?,
    child1: Block?,
    child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
  ): Spacing? =
    when (parent?.elementType) {
      Angular2HtmlElementTypes.BLOCK -> {
        when ((child1 as? AbstractBlock)?.node?.elementType) {
          Angular2HtmlTokenTypes.BLOCK_NAME,
          Angular2HtmlElementTypes.BLOCK_PARAMETERS,
          Angular2HtmlElementTypes.BLOCK_CONTENTS,
            -> { Spacing.createSpacing(1, 1, 0,
                                  false, xmlFormattingPolicy.keepBlankLines)
          }
          else -> null
        }
      }
      Angular2HtmlElementTypes.BLOCK_PARAMETERS -> {
        when ((child1 as? AbstractBlock)?.node?.elementType) {
          Angular2HtmlTokenTypes.BLOCK_SEMICOLON ->
            Spacing.createSpacing(1, 1, 0, true, xmlFormattingPolicy.keepBlankLines)
          else ->
            if ((child2 as? AbstractBlock)?.node?.elementType == Angular2HtmlTokenTypes.BLOCK_SEMICOLON)
              Spacing.createSpacing(0, 0, 0, false, 0)
            else
              Spacing.createSpacing(0, 0, 0, false, xmlFormattingPolicy.keepBlankLines)
        }
      }
      Angular2HtmlElementTypes.BLOCK_CONTENTS -> {
        when ((child1 as? AbstractBlock)?.node?.elementType) {
          Angular2HtmlTokenTypes.BLOCK_START -> {
            Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
          }
          else -> when ((child2 as? AbstractBlock)?.node?.elementType) {
            Angular2HtmlTokenTypes.BLOCK_END -> {
              Spacing.createSpacing(0, 0, 1, false, xmlFormattingPolicy.keepBlankLines)
            }
            else -> null
          }
        }
      }
      else -> null
    }

  private fun getSpacingIfInterpolationBorder(
    child1: Block?, child2: Block,
    xmlFormattingPolicy: XmlFormattingPolicy,
    subBlocksProvider: () -> List<Block>,
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
    (child1 is Angular2HtmlFormattingBlock && (child1.node.elementType == Angular2HtmlTokenTypes.INTERPOLATION_START
                                               || child1.node.elementType == Angular2HtmlTokenTypes.INTERPOLATION_END))
    && (child2 as? AnotherLanguageBlockWrapper)?.node?.psi?.let { it.language is Angular2Language && it.parent !is XmlAttribute } == true

}