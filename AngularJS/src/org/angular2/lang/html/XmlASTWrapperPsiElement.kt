// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElement
import com.intellij.xml.util.XmlPsiUtil

class XmlASTWrapperPsiElement(node: ASTNode) : ASTWrapperPsiElement(node), XmlElement, JSEmbeddedContent {
  override fun processElements(processor: PsiElementProcessor<in PsiElement>, place: PsiElement): Boolean {
    return XmlPsiUtil.processXmlElements(this, processor, false)
  }

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun skipValidation(): Boolean {
    return true
  }

  override fun getElementType(): IElementType {
    return node.elementType
  }

  override fun getQuoteChar(): Char? {
    return JSEmbeddedContentImpl.getQuoteChar(this)
  }
}