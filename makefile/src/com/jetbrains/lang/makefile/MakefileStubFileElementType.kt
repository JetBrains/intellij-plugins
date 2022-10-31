package com.jetbrains.lang.makefile

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType

class MakefileStubFileElementType : IStubFileElementType<PsiFileStub<MakefileFile>>("makefile.FILE", MakefileLanguage)