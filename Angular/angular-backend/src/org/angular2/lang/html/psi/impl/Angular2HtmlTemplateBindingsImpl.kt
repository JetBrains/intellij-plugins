// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.expr.psi.impl.Angular2EmptyTemplateBindings
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl

internal class Angular2HtmlTemplateBindingsImpl : Angular2HtmlBoundAttributeImpl, Angular2HtmlTemplateBindings {

  constructor(stub: Angular2HtmlBoundAttributeStubImpl, nodeType: IElementType)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitTemplateBindings(this)
      }
      is XmlElementVisitor -> {
        visitor.visitXmlAttribute(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val bindings: Angular2TemplateBindings
    get() = PsiTreeUtil.findChildrenOfType(this, Angular2TemplateBindings::class.java).firstOrNull()
            ?: Angular2EmptyTemplateBindings(this, templateName)
  override val templateName: String
    get() = attributeInfo.name
}