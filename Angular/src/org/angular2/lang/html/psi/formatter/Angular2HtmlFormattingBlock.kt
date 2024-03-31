// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.*
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

class Angular2HtmlFormattingBlock(node: ASTNode?,
                                  wrap: Wrap?,
                                  alignment: Alignment?,
                                  policy: XmlFormattingPolicy?,
                                  indent: Indent?,
                                  textRange: TextRange?,
                                  preserveSpace: Boolean)
  : XmlBlock(node, wrap, alignment, policy, indent, textRange, preserveSpace) {

  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock =
    Angular2HtmlFormattingHelper.createTagBlock(
      myNode, child, indent, wrap, alignment, myXmlFormattingPolicy, isPreserveSpace)

  override fun createSimpleChild(child: ASTNode, indent: Indent?,
                                 wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock =
    Angular2HtmlFormattingHelper.createSimpleChild(
      myNode, child, indent, wrap, alignment, range, myXmlFormattingPolicy, isPreserveSpace)

  override fun processChild(result: MutableList<Block>, child: ASTNode, wrap: Wrap?, alignment: Alignment?, indent: Indent?): ASTNode? =
    Angular2HtmlFormattingHelper.processChild(this, result, child, wrap, alignment, indent,
                                              myXmlFormattingPolicy, isPreserveSpace) { r, c, w, a, i ->
      super.processChild(r, c, w, a, i)
    }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean =
    childLanguage.isKindOf(Angular2HtmlLanguage)
    || super.useMyFormatter(myLanguage, childLanguage, childPsi)

  override fun getSpacing(child1: Block?, child2: Block): Spacing? =
    Angular2HtmlFormattingHelper.getSpacing(myNode, child1, child2, myXmlFormattingPolicy, ::getSubBlocks)
    ?: super.getSpacing(child1, child2)

  override fun isLeaf(): Boolean =
    myNode.elementType is Angular2EmbeddedExprTokenType
    || super.isLeaf()

  override fun isAttributeElementType(elementType: IElementType): Boolean =
    Angular2HtmlFormattingHelper.isAngular2AttributeElementType(elementType)

  override fun getChildAttributes(newChildIndex: Int): ChildAttributes =
    Angular2HtmlFormattingHelper.getChildAttributes(this)
    ?: super.getChildAttributes(newChildIndex)

}