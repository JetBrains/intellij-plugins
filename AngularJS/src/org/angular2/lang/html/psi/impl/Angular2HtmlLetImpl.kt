// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.parser.Angular2HtmlVarAttrTokenType
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlLet

class Angular2HtmlLetImpl(type: Angular2HtmlElementTypes.Angular2ElementType) : Angular2HtmlBoundAttributeImpl(type), Angular2HtmlLet {
  override fun getNameElement(): XmlElement? {
    val res = super.getNameElement()
    return if (res == null && firstChild.node.elementType === Angular2HtmlVarAttrTokenType.LET) {
      firstChild as XmlElement
    }
    else res
  }

  override val variable: JSVariable?
    get() = PsiTreeUtil.findChildOfType(this, JSVariable::class.java)

  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitLet(this)
      }
      is XmlElementVisitor -> {
        visitor.visitXmlAttribute(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val variableName: String
    get() = attributeInfo.name
}