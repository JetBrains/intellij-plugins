// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import org.angular2.lang.html.parser.Angular2HtmlVarAttrTokenType
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlReference
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl

internal class Angular2HtmlReferenceImpl : Angular2HtmlBoundAttributeImpl, Angular2HtmlReference {

  constructor(stub: Angular2HtmlBoundAttributeStubImpl, nodeType: IElementType)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override fun getNameElement(): XmlElement? {
    val res = super.getNameElement()
    return if (res == null && firstChild.node.elementType === Angular2HtmlVarAttrTokenType.REFERENCE) {
      firstChild as XmlElement
    }
    else res
  }

  override val variable: JSVariable?
    get() = PsiTreeUtil.findChildOfType(this, JSVariable::class.java)

  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitReference(this)
      }
      is XmlElementVisitor -> {
        visitor.visitXmlAttribute(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val referenceName: String
    get() = attributeInfo.name
}