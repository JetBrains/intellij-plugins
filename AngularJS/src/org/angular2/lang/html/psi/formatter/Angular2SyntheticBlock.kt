// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.tree.IElementType

class Angular2SyntheticBlock(subBlocks: List<Block>, parent: Block, indent: Indent?, policy: XmlFormattingPolicy, childIndent: Indent?)
  : SyntheticBlock(subBlocks, parent, indent, policy, childIndent) {
  override fun isAttributeElementType(elementType: IElementType): Boolean {
    return Angular2HtmlTagBlock.isAngular2AttributeElementType(elementType)
  }
}