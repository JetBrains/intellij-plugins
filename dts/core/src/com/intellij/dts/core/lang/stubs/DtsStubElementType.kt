package com.intellij.dts.core.lang.stubs

import com.intellij.dts.core.lang.DtsLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement

abstract class DtsStubElementType<Stub : StubElement<Psi>, Psi : PsiElement>(debugName: String) : IStubElementType<Stub, Psi>(debugName,
                                                                                                                              DtsLanguage) {
  override fun getExternalId(): String {
    return "DTS.$debugName"
  }
}