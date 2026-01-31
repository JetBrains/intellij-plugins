package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.jetbrains.lang.makefile.psi.MakefileNamedElement
import com.jetbrains.lang.makefile.stub.MakefileTargetStubElement

abstract class MakefileTargetNamedElementImpl : StubBasedPsiElementBase<MakefileTargetStubElement>, MakefileNamedElement {
  constructor(node: ASTNode) : super(node)
  constructor(stub: MakefileTargetStubElement, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

  override fun toString() = "MakefileTargetImpl(TARGET)"
}