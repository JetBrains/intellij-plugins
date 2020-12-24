package com.jetbrains.lang.makefile.stub

import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import com.jetbrains.lang.makefile.psi.MakefileTarget

class MakefileTargetStubElementImpl(parent: StubElement<*>?, name: String?) : NamedStubBase<MakefileTarget>(parent, MakefileTargetStubElementType, name), MakefileTargetStubElement