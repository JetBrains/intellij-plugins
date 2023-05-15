// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
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
import com.intellij.psi.xml.XmlElementType
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.parser.Angular2HtmlElementTypes

class Angular2HtmlTagBlock(node: ASTNode,
                           wrap: Wrap?,
                           alignment: Alignment?,
                           policy: XmlFormattingPolicy,
                           indent: Indent?,
                           preserveSpace: Boolean)
  : XmlTagBlock(node, wrap, alignment, policy, indent, preserveSpace) {
  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return Angular2HtmlTagBlock(child, wrap, alignment, myXmlFormattingPolicy,
                                indent ?: Indent.getNoneIndent(), isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return Angular2HtmlBlock(child, wrap, alignment, myXmlFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun createSyntheticBlock(localResult: ArrayList<Block>, childrenIndent: Indent?): Block {
    return Angular2SyntheticBlock(localResult, this, Indent.getNoneIndent(), myXmlFormattingPolicy, childrenIndent)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage.isKindOf(Angular2HtmlLanguage.INSTANCE)
           || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }

  override fun isAttributeElementType(elementType: IElementType): Boolean {
    return isAngular2AttributeElementType(elementType)
  }

  companion object {
    fun isAngular2AttributeElementType(elementType: IElementType): Boolean {
      return elementType === XmlElementType.XML_ATTRIBUTE
             || Angular2HtmlElementTypes.ALL_ATTRIBUTES.contains(elementType)
    }
  }
}