// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes

class Angular2SyntheticBlock(
  subBlocks: List<Block>, parent: Block, indent: Indent?, policy: XmlFormattingPolicy, childIndent: Indent?,
  val isBlockGroup: Boolean,
)
  : SyntheticBlock(subBlocks, parent, indent, policy, childIndent) {

  override fun isAttributeElementType(elementType: IElementType): Boolean =
    Angular2HtmlFormattingHelper.isAngular2AttributeElementType(elementType)

  override fun getSpacing(child1: Block?, child2: Block): Spacing? =
    Angular2HtmlFormattingHelper.getSpacing(null, child1, child2, myXmlFormattingPolicy, ::getSubBlocks)
    ?: super.getSpacing(child1, child2)

  override fun startsWithText(): Boolean =
    super.startsWithText()
    || myStartTreeNode.elementType == Angular2HtmlTokenTypes.INTERPOLATION_START

  override fun endsWithText(): Boolean =
    super.endsWithText()
    || myEndTreeNode.elementType == Angular2HtmlTokenTypes.INTERPOLATION_END

  val isEndOfTag: Boolean = myEndTreeNode.elementType === XmlTokenType.XML_TAG_END

  val isStartOfTag: Boolean = myStartTreeNode.elementType === XmlTokenType.XML_START_TAG_START
                              || myStartTreeNode.elementType === XmlTokenType.XML_END_TAG_START

}