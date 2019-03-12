package name.kropp.intellij.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.stubs.*
import name.kropp.intellij.makefile.psi.*
import name.kropp.intellij.makefile.stub.*

abstract class MakefileNamedElementImpl : StubBasedPsiElementBase<MakefileTargetStubElement>, MakefileNamedElement {
  constructor(node: ASTNode) : super(node)
  constructor(stub: MakefileTargetStubElement, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

  override fun toString() = "MakefileTargetImpl(TARGET)"
}