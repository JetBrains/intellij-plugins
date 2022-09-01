// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.xml.IXmlAttributeElementType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeImpl
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeStubImpl
import java.io.IOException

class VueRefAttributeElementType :
  XmlStubBasedElementType<VueRefAttributeStubImpl, VueRefAttributeImpl>("REF_ATTRIBUTE", VueLanguage.INSTANCE),
  ICompositeElementType, IXmlAttributeElementType {

  @Throws(IOException::class)
  override fun serialize(stub: VueRefAttributeStubImpl, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): VueRefAttributeStubImpl {
    return VueRefAttributeStubImpl(parentStub, dataStream, this)
  }

  override fun createPsi(stub: VueRefAttributeStubImpl): VueRefAttributeImpl {
    return VueRefAttributeImpl(stub, this)
  }

  override fun createPsi(node: ASTNode): VueRefAttributeImpl {
    return VueRefAttributeImpl(node)
  }

  override fun createStub(psi: VueRefAttributeImpl, parentStub: StubElement<*>): VueRefAttributeStubImpl {
    return VueRefAttributeStubImpl(psi, parentStub, this)
  }

}