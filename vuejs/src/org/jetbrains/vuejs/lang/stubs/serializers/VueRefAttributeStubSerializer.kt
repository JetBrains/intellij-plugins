// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.serializers

import com.intellij.lang.stubs.XmlStubBasedStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.REF_ATTRIBUTE
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeStubImpl

class VueRefAttributeStubSerializer : XmlStubBasedStubSerializer<VueRefAttributeStubImpl>(REF_ATTRIBUTE) {
  override fun serialize(stub: VueRefAttributeStubImpl, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): VueRefAttributeStubImpl =
    VueRefAttributeStubImpl(parentStub, dataStream, elementType)
}