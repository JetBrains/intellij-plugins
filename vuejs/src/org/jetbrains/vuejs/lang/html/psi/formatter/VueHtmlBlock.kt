// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.BlockEx
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueHtmlBlock(node: ASTNode,
                   wrap: Wrap?,
                   alignment: Alignment?,
                   policy: XmlFormattingPolicy,
                   indent: Indent?,
                   textRange: TextRange?,
                   preserveSpace: Boolean)
  : XmlBlock(node, wrap, alignment, policy, indent, textRange, preserveSpace), BlockEx {

  override fun getLanguage(): Language? {
    return HTMLLanguage.INSTANCE
  }

  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return VueHtmlTagBlock(child, wrap, alignment, myXmlFormattingPolicy, indent ?: Indent.getNoneIndent(),
                           isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return VueHtmlBlock(child, wrap, alignment, myXmlFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage === VueLanguage.INSTANCE || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }
}
