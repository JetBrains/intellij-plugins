package com.jetbrains.lang.makefile.stub

import com.intellij.psi.stubs.*
import com.jetbrains.lang.makefile.MakefileLanguage
import com.jetbrains.lang.makefile.TARGET_INDEX_KEY
import com.jetbrains.lang.makefile.psi.MakefileTarget
import com.jetbrains.lang.makefile.psi.impl.MakefileTargetImpl

object MakefileTargetStubElementType : IStubElementType<MakefileTargetStubElement, MakefileTarget>("TARGET", MakefileLanguage) {
  @Suppress("UNUSED_PARAMETER")
  @JvmStatic
  fun getInstance(debugName: String) = MakefileTargetStubElementType

  override fun getExternalId() = "Makefile"

  override fun createStub(psi: MakefileTarget, parent: StubElement<*>?) = MakefileTargetStubElementImpl(parent, psi.name)
  override fun createPsi(stub: MakefileTargetStubElement) = MakefileTargetImpl(stub, stub.stubType)

  override fun indexStub(stub: MakefileTargetStubElement, sink: IndexSink) {
    sink.occurrence(TARGET_INDEX_KEY, stub.name!!)
  }

  override fun serialize(e: MakefileTargetStubElement, outputStream: StubOutputStream) {
    outputStream.writeName(e.name)
  }
  override fun deserialize(inputStream: StubInputStream, parent: StubElement<*>?) =
    MakefileTargetStubElementImpl(parent, inputStream.readName()?.string)
}