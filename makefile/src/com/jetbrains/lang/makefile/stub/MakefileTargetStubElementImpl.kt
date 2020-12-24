package name.kropp.intellij.makefile.stub

import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileTargetStubElementImpl(parent: StubElement<*>?, name: String?) : NamedStubBase<MakefileTarget>(parent, MakefileTargetStubElementType, name), MakefileTargetStubElement