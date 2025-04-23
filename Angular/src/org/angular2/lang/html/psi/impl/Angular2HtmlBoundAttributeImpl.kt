// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.source.xml.XmlStubBasedAttributeBase
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTokenType
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute
import org.angular2.lang.html.stub.Angular2HtmlAttributeStubElementType
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl
import org.jetbrains.annotations.NonNls

internal open class Angular2HtmlBoundAttributeImpl
  : XmlStubBasedAttributeBase<Angular2HtmlBoundAttributeStubImpl>, Angular2HtmlBoundAttribute {

  constructor(stub: Angular2HtmlBoundAttributeStubImpl, nodeType: Angular2HtmlAttributeStubElementType)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override fun getNameElement(): XmlElement? {
    val result = super.getNameElement()
    return if (result == null
               && firstChild is PsiErrorElement
               && firstChild.firstChild.node.elementType === XmlTokenType.XML_NAME) {
      firstChild.firstChild as XmlElement
    }
    else result
  }

  override val attributeInfo: Angular2AttributeNameParser.AttributeInfo
    get() {
      val info = Angular2AttributeNameParser.parseBound(name, parent?.localName ?: "")
      if (info.type.elementType !== elementType) {
        LOG.error("Element type mismatch on attribute info. Expected " + elementType
                  + ", but got " + info.type.elementType + ". Error for " +
                  javaClass.simpleName.removeSuffix(IMPL_SUFFIX) + " <" + name + ">",
                  Throwable())
      }
      return info
    }

  override fun getName(): String {
    getGreenStub()
      ?.let { return it.getName() }
    return super.getName()
  }

  override fun getValue(): String? {
    getGreenStub()
      ?.let { return it.getValue() }
    return super.getValue()
  }

  override fun toString(): String {
    return javaClass.simpleName.removeSuffix(IMPL_SUFFIX) + " " + attributeInfo.toString()
  }

  companion object {
    private val LOG: @NonNls Logger = Logger.getInstance(Angular2HtmlBoundAttributeImpl::class.java)
    private const val IMPL_SUFFIX: @NonNls String = "Impl"
  }
}