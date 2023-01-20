// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.e4x.impl.JSXmlAttributeImpl.isExpressionValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.XmlElementFactoryImpl
import com.intellij.psi.impl.source.xml.XmlAttributeDelegate
import com.intellij.psi.impl.source.xml.XmlStubBasedAttributeBase
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.astro.lang.stub.AstroHtmlAttributeStubImpl

class AstroHtmlAttributeImpl : XmlStubBasedAttributeBase<AstroHtmlAttributeStubImpl>, JSElement {

  constructor(stub: AstroHtmlAttributeStubImpl, nodeType: IStubElementType<out AstroHtmlAttributeStubImpl, out XmlAttribute>)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override fun createDelegate(): XmlAttributeDelegate {
    return AstroHtmlAttributeImplDelegate()
  }

  private inner class AstroHtmlAttributeImplDelegate : XmlStubBasedAttributeBaseDelegate() {
    override fun createAttribute(name: String, valueText: String, quoteStyle: Char?): XmlAttribute {
      return Companion.createAttribute(this@AstroHtmlAttributeImpl, name, valueText, quoteStyle)
    }
  }

  companion object {
    fun createAttribute(context: PsiElement, qname: String, value: String, quoteStyle: Char?): AstroHtmlAttributeImpl {
      val quotedValue = if (!isExpressionValue(value)) {
        XmlElementFactoryImpl.quoteValue(value, quoteStyle)
      }
      else value
      val text = "<a $qname=$quotedValue/>"
      val file = PsiFileFactory.getInstance(context.project).createFileFromText("foo.astro", context.language, text, false, false)
      val root = file.firstChild
      assert(root is AstroRootContent) { "Failed to parse as tag $text" }
      val tag = (root as AstroRootContent).firstChild
      assert(tag is AstroHtmlTag) { "Failed to parse as tag $text" }
      return (tag as AstroHtmlTag).getAttribute(qname) as AstroHtmlAttributeImpl
    }
  }

}