// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.xml.XmlBlock
import com.intellij.psi.formatter.xml.XmlTagBlock
import org.jetbrains.vuejs.lang.html.VueLanguage
import java.util.*

class VueTagBlock(node: ASTNode,
                  wrap: Wrap?,
                  alignment: Alignment?,
                  policy: VueRootFormattingPolicy,
                  indent: Indent?,
                  preserveSpace: Boolean)
  : XmlTagBlock(node, wrap, alignment, policy, indent, preserveSpace) {

  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return VueHtmlTagBlock(child, wrap, alignment, (myXmlFormattingPolicy as VueRootFormattingPolicy).htmlPolicy,
                           indent ?: Indent.getNoneIndent(), isPreserveSpace)
  }

  override fun createSimpleChild(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?, range: TextRange?): XmlBlock {
    return VueBlock(child, wrap, alignment, myXmlFormattingPolicy as VueRootFormattingPolicy, indent, range, isPreserveSpace)
  }

  override fun createSyntheticBlock(localResult: ArrayList<Block>, childrenIndent: Indent?): Block {
    return VueSyntheticBlock(localResult, this, Indent.getNoneIndent(),
                             (myXmlFormattingPolicy as VueRootFormattingPolicy).htmlPolicy,
                             childrenIndent, null)
  }

  override fun useMyFormatter(myLanguage: Language, childLanguage: Language, childPsi: PsiElement): Boolean {
    return childLanguage === VueLanguage.INSTANCE || super.useMyFormatter(myLanguage, childLanguage, childPsi)
  }
}