package name.kropp.intellij.makefile

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType

class MakefileStubFileElementType : IStubFileElementType<PsiFileStub<MakefileFile>>(MakefileLanguage)