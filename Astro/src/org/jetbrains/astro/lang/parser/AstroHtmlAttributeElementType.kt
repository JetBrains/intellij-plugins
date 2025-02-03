// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.PsiGenerator
import com.intellij.psi.stubs.*
import com.intellij.psi.xml.IXmlAttributeElementType
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.psi.AstroHtmlAttributeImpl
import org.jetbrains.astro.lang.stub.AstroHtmlAttributeStubImpl

class AstroHtmlAttributeElementType : IStubElementType<AstroHtmlAttributeStubImpl, AstroHtmlAttributeImpl>
                                      ("XML_ATTRIBUTE", AstroLanguage.INSTANCE),
                                      PsiGenerator<XmlAttribute>, IXmlAttributeElementType {

  override fun construct(node: ASTNode): XmlAttribute {
    return AstroHtmlAttributeImpl(node)
  }

  override fun shouldCreateStub(node: ASTNode): Boolean {
    return false
  }

  override fun indexStub(stub: AstroHtmlAttributeStubImpl, sink: IndexSink) {
  }

  override fun createPsi(stub: AstroHtmlAttributeStubImpl): AstroHtmlAttributeImpl {
    return AstroHtmlAttributeImpl(stub, this)
  }

  override fun createStub(psi: AstroHtmlAttributeImpl, parentStub: StubElement<*>?): AstroHtmlAttributeStubImpl {
    return AstroHtmlAttributeStubImpl(psi, parentStub, this)
  }

  override fun getExternalId(): String {
    return "ASTRO:HTML_ATTRIBUTE"
  }

  override fun serialize(stub: AstroHtmlAttributeStubImpl, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): AstroHtmlAttributeStubImpl {
    return AstroHtmlAttributeStubImpl(parentStub, dataStream, this)
  }

}

