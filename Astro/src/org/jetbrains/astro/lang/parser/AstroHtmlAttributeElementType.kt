// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.e4x.impl.JSXmlAttributeImpl
import com.intellij.lang.javascript.types.JSXmlAttributeElementType
import com.intellij.psi.impl.source.xml.stub.XmlAttributeStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.psi.AstroHtmlAttributeImpl
import org.jetbrains.astro.lang.stub.AstroHtmlAttributeStubImpl

class AstroHtmlAttributeElementType : JSXmlAttributeElementType() {

  override fun getLanguage(): Language = AstroLanguage.INSTANCE

  override fun construct(node: ASTNode): XmlAttribute {
    return AstroHtmlAttributeImpl(node)
  }

  override fun createPsi(stub: XmlAttributeStub<JSXmlAttributeImpl>): XmlAttribute {
    return (stub as AstroHtmlAttributeStubImpl).createPsi()
  }

  override fun createStub(psi: XmlAttribute, parentStub: StubElement<*>): XmlAttributeStub<JSXmlAttributeImpl> {
    return AstroHtmlAttributeStubImpl(psi as JSXmlAttributeImpl, parentStub, this)
  }

  override fun getExternalId(): String {
    return "ASTRO:HTML_ATTRIBUTE"
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): XmlAttributeStub<JSXmlAttributeImpl> {
    return AstroHtmlAttributeStubImpl(dataStream, parentStub, this)
  }


}