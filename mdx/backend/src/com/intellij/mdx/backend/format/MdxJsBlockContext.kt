package com.intellij.mdx.backend.format

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.formatter.JSSpacingProcessor
import com.intellij.lang.javascript.formatter.blocks.JSBlock
import com.intellij.lang.javascript.formatter.blocks.JSSpacingStrategy
import com.intellij.lang.javascript.formatter.blocks.alignment.ASTNodeBasedAlignmentFactory
import com.intellij.lang.javascript.psi.JSXmlLiteralExpression
import com.intellij.psi.codeStyle.CodeStyleSettings

class MdxJsBlockContext(topSettings: CodeStyleSettings,
                        dialect: Language,
                        explicitSettings: JSCodeStyleSettings?,
                        formattingMode: FormattingMode) : JSBlockContext(topSettings, dialect, explicitSettings, formattingMode) {

  override fun createBlock(child: ASTNode,
                           wrap: Wrap?,
                           childAlignment: Alignment?,
                           childIndent: Indent?,
                           alignmentFactory: ASTNodeBasedAlignmentFactory?,
                           parentBlock: JSBlock?): Block {
    val policy = myPolicy
    if (child is JSXmlLiteralExpression && policy != null) {
      return MdxJsxTagBlock(child, wrap, childAlignment, policy, childIndent, parentBlock)
    }
    return super.createBlock(child, wrap, childAlignment, childIndent, alignmentFactory, parentBlock)
  }

  override fun createSpacingStrategy(node: ASTNode): JSSpacingStrategy {
    return JSSpacingStrategy(myDialectSettings, commonSettings
    ) { child1: ASTNode, child2: ASTNode -> createMdxSpacingProcessor(node, child1, child2).calcSpacing() }
  }

  private fun createMdxSpacingProcessor(node: ASTNode, child1: ASTNode, child2: ASTNode): JSSpacingProcessor {
    return MdxJsSpacingProcessor(node, child1, child2, topSettings, dialect, myDialectSettings)
  }
}
