// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.stub

import com.intellij.lang.javascript.psi.e4x.impl.JSXmlAttributeImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSXmlAttributeStubImpl
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.astro.lang.parser.AstroStubElementTypes
import org.jetbrains.astro.lang.psi.AstroHtmlAttributeImpl

class AstroHtmlAttributeStubImpl : JSXmlAttributeStubImpl {

  constructor(psi: JSXmlAttributeImpl, parent: StubElement<*>, elementType: IStubElementType<*, *>)
    : super(psi, parent, elementType)

  constructor(dataStream: StubInputStream, parentStub: StubElement<*>, elementType: IStubElementType<*, *>)
    : super(dataStream, parentStub, elementType)


  override fun createPsi(): JSXmlAttributeImpl {
    @Suppress("UNCHECKED_CAST")
    return AstroHtmlAttributeImpl(this, AstroStubElementTypes.HTML_ATTRIBUTE as IStubElementType<JSXmlAttributeStubImpl, XmlAttribute>)
  }

}