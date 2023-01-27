// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlTagBlock
import org.jetbrains.astro.lang.AstroLanguage

class AstroTagBlock(node: ASTNode,
                    wrap: Wrap?,
                    alignment: Alignment?,
                    policy: AstroFormattingPolicy,
                    indent: Indent?,
                    preserveSpace: Boolean)
  : XmlTagBlock(node, wrap, alignment, policy, indent, preserveSpace), BlockEx {

  override fun getLanguage(): Language? {
    return HTMLLanguage.INSTANCE
  }

  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return AstroTagBlock(child, wrap, alignment, myXmlFormattingPolicy as AstroFormattingPolicy,
                         indent ?: Indent.getNoneIndent(), isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return AstroBlock(child, wrap, alignment, myXmlFormattingPolicy as AstroFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun createSyntheticBlock(localResult: ArrayList<Block>, childrenIndent: Indent?): Block {
    return AstroSyntheticBlock(localResult, this, Indent.getNoneIndent(),
                               myXmlFormattingPolicy as AstroFormattingPolicy,
                               childrenIndent)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage === AstroLanguage.INSTANCE || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }

  override fun processChild(result: MutableList<Block>, child: ASTNode, wrap: Wrap?, alignment: Alignment?, indent: Indent?): ASTNode? {
    if (child.elementType === JSStubElementTypes.EMBEDDED_EXPRESSION) {
      if (!isBuildIndentsOnly) {
        (myXmlFormattingPolicy as AstroFormattingPolicy).buildInjectedEmbeddedExpressionBlock(
          result, child, wrap, alignment, indent, myInjectedBlockBuilder)
      }
      return child
    }
    return super.processChild(result, child, wrap, alignment, indent)
  }
}