// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata.psi

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import org.angular2.lang.metadata.stubs.MetadataElementStub

import java.io.IOException

open class MetadataElementType<Stub : MetadataElementStub<*>>(debugName: String,
                                                              language: Language,
                                                              private val myStubConstructor: MetadataStubConstructor<out Stub>,
                                                              private val myPsiConstructor: MetadataElementConstructor<Stub>) : IStubElementType<Stub, MetadataElement<Stub>>(
  debugName, language) {

  override fun createPsi(stub: Stub): MetadataElement<Stub> {
    return myPsiConstructor.construct(stub)
  }

  override fun createStub(psi: MetadataElement<Stub>, parentStub: StubElement<out PsiElement>?): Stub {
    throw UnsupportedOperationException()
  }


  override fun getExternalId(): String {
    return toString()
  }

  @Throws(IOException::class)
  override fun serialize(stub: Stub, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): Stub {
    return myStubConstructor.construct(dataStream, parentStub)
  }

  override fun indexStub(stub: Stub, sink: IndexSink) {
    stub.index(sink)
  }

  fun interface MetadataStubConstructor<Stub : MetadataElementStub<*>> {
    @Throws(IOException::class)
    fun construct(stream: StubInputStream, parent: StubElement<*>): Stub
  }

  fun interface MetadataElementConstructor<Stub : MetadataElementStub<*>> {
    fun construct(stub: Stub): MetadataElement<Stub>
  }
}
