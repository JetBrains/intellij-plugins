package name.kropp.intellij.makefile.stub

import com.intellij.psi.stubs.*
import name.kropp.intellij.makefile.MakefileLanguage
import name.kropp.intellij.makefile.psi.MakefileTarget

object MakefileTargetStubElementType : IStubElementType<MakefileTargetStubElement, MakefileTarget>("MakefileTarget", MakefileLanguage) {
  override fun createStub(p0: MakefileTarget, p1: StubElement<*>?): MakefileTargetStubElement {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getExternalId() = "Makefile"

  override fun createPsi(p0: MakefileTargetStubElement): MakefileTarget {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun indexStub(p0: MakefileTargetStubElement, p1: IndexSink) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun serialize(e: MakefileTargetStubElement, outputStream: StubOutputStream) {
    outputStream.writeName(e.name)
  }

  override fun deserialize(inputStream: StubInputStream, parent: StubElement<*>?) =
      MakefileTargetStubElementImpl(parent, inputStream.readName()?.string)
}