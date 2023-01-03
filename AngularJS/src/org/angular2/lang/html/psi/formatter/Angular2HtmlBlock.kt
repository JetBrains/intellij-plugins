// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType
import org.angular2.lang.html.Angular2HtmlLanguage

class Angular2HtmlBlock(node: ASTNode?,
                        wrap: Wrap?,
                        alignment: Alignment?,
                        policy: XmlFormattingPolicy?,
                        indent: Indent?,
                        textRange: TextRange?,
                        preserveSpace: Boolean)
  : XmlBlock(node, wrap, alignment, policy, indent, textRange, preserveSpace) {
  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return Angular2HtmlTagBlock(child, wrap, alignment, myXmlFormattingPolicy, indent ?: Indent.getNoneIndent(), isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?,
                                 wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return Angular2HtmlBlock(child, wrap, alignment, myXmlFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage.isKindOf(Angular2HtmlLanguage.INSTANCE)
           || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }

  override fun isLeaf(): Boolean {
    return myNode.elementType is Angular2EmbeddedExprTokenType
           || super.isLeaf()
  }

  override fun isAttributeElementType(elementType: IElementType): Boolean {
    return Angular2HtmlTagBlock.isAngular2AttributeElementType(elementType)
  }
}