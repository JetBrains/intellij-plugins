// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.expr.psi.impl.Angular2EmptyTemplateBindings
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings

internal class Angular2HtmlTemplateBindingsImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : Angular2HtmlBoundAttributeImpl(type), Angular2HtmlTemplateBindings {
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