package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.stubs.*
import com.jetbrains.lang.makefile.psi.*
import com.jetbrains.lang.makefile.stub.*

abstract class MakefileTargetNamedElementImpl : StubBasedPsiElementBase<MakefileTargetStubElement>, MakefileNamedElement {
  constructor(node: ASTNode) : super(node)
  constructor(stub: MakefileTargetStubElement, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

  override fun toString() = "MakefileTargetImpl(TARGET)"
}