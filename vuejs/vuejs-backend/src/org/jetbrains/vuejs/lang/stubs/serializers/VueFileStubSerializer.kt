// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.serializers

import com.intellij.psi.stubs.*
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.VueFileElementType
import org.jetbrains.vuejs.lang.html.stub.impl.VueFileStubImpl

class VueFileStubSerializer : StubSerializer<VueFileStubImpl> {
  override fun getExternalId(): String =
    VueFileElementType.INSTANCE.toString()

  override fun serialize(stub: VueFileStubImpl, dataStream: StubOutputStream) {
    dataStream.writeName(stub.langMode.canonicalAttrValue)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): VueFileStubImpl =
    VueFileStubImpl(LangMode.fromAttrValue(dataStream.readNameString()!!))

  override fun indexStub(stub: VueFileStubImpl, sink: IndexSink) {
  }
}