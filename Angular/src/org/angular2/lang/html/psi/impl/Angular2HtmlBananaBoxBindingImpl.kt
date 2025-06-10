// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl

internal class Angular2HtmlBananaBoxBindingImpl : Angular2HtmlPropertyBindingBase, Angular2HtmlBananaBoxBinding {

  constructor(stub: Angular2HtmlBoundAttributeStubImpl, nodeType: IElementType)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitBananaBoxBinding(this)
      }
      is XmlElementVisitor -> {
        visitor.visitXmlAttribute(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }
}