// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.xml.AnotherLanguageBlockWrapper
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.asSafely
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.lexer.AstroTokenTypes
import org.jetbrains.astro.lang.parser.AstroStubElementTypes

class AstroBlock(node: ASTNode,
                 wrap: Wrap?,
                 alignment: Alignment?,
                 policy: AstroFormattingPolicy,
                 indent: Indent?,
                 textRange: TextRange?,
                 preserveSpace: Boolean)
  : XmlBlock(node, wrap, alignment, policy, indent, textRange, preserveSpace), BlockEx {

  override fun getLanguage(): Language? {
    return HTMLLanguage.INSTANCE
  }

  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return AstroTagBlock(child, wrap, alignment, myXmlFormattingPolicy as AstroFormattingPolicy, indent ?: Indent.getNoneIndent(),
                         isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return AstroBlock(child, wrap, alignment, myXmlFormattingPolicy as AstroFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage === AstroLanguage.INSTANCE || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }

  override fun getSpacing(child1: Block?, child2: Block): Spacing? {
    if (child1 == null && child2.asSafely<AstroBlock>()?.node?.elementType == AstroTokenTypes.FRONTMATTER_SEPARATOR) {
      return Spacing.createSpacing(0, 0, 0, false, 0)
    }
    else if (child1.asSafely<AstroBlock>()?.node?.elementType == XmlTokenType.XML_COMMENT_CHARACTERS
             && child2.asSafely<AstroBlock>()?.node?.elementType == AstroTokenTypes.FRONTMATTER_SEPARATOR) {
      return Spacing.createSpacing(0, 0, 1, false, 0)
    }
    else if ((child1.asSafely<AstroBlock>()?.node?.elementType == AstroTokenTypes.FRONTMATTER_SEPARATOR
              && child2 is AnotherLanguageBlockWrapper)
             || (child1 is AnotherLanguageBlockWrapper
                 && child2.asSafely<AstroBlock>()?.node?.elementType == AstroTokenTypes.FRONTMATTER_SEPARATOR)) {
      return Spacing.createSpacing(0, 0, 1, false, 0)
    }
    return super.getSpacing(child1, child2)
  }

  override fun getChildDefaultIndent(): Indent? {
    return if (myNode.elementType === AstroStubElementTypes.CONTENT_ROOT) {
      Indent.getNoneIndent()
    }
    else super.getChildDefaultIndent()
  }

  override fun splitAttribute(node: ASTNode, formattingPolicy: XmlFormattingPolicy): MutableList<Block> {
    node.firstChildNode.takeIf { it.elementType === JSStubElementTypes.EMBEDDED_EXPRESSION }?.let {
      val result = mutableListOf<Block>()
      (myXmlFormattingPolicy as AstroFormattingPolicy).buildInjectedEmbeddedExpressionBlock(
        result, it, wrap, alignment, indent, myInjectedBlockBuilder)
      return result
    }
    return super.splitAttribute(node, formattingPolicy)
  }

}