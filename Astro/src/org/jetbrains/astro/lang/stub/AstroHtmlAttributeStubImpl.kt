// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.stub

import com.intellij.psi.impl.source.xml.stub.XmlAttributeStub
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.jetbrains.astro.lang.psi.AstroHtmlAttributeImpl
import java.io.IOException

class AstroHtmlAttributeStubImpl : StubBase<AstroHtmlAttributeImpl>, XmlAttributeStub<AstroHtmlAttributeImpl> {
  val name: String
  val value: String?

  constructor(parent: StubElement<*>?,
              dataStream: StubInputStream,
              elementType: IStubElementType<*, *>) : super(parent, elementType) {
    name = (StringRef.toString(dataStream.readName())) ?: ""
    value = StringRef.toString(dataStream.readName())
  }

  constructor(psi: AstroHtmlAttributeImpl,
              parent: StubElement<*>?,
              elementType: IStubElementType<*, *>) : super(parent, elementType) {
    name = psi.name
    value = psi.value
  }

  @Throws(IOException::class)
  fun serialize(stream: StubOutputStream) {
    stream.writeName(name)
    stream.writeName(value)
  }
}

