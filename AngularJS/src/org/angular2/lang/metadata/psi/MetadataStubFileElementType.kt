// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata.psi

import com.intellij.lang.Language
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.tree.IStubFileElementType
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl

open class MetadataStubFileElementType(language: Language) : IStubFileElementType<MetadataFileStubImpl>(language) {

  override fun getExternalId(): String {
    return language.toString() + ":" + toString()
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): MetadataFileStubImpl {
    return MetadataFileStubImpl(null, this)
  }
}
