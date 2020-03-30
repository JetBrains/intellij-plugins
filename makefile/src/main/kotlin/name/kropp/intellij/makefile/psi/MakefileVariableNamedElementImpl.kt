package name.kropp.intellij.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import name.kropp.intellij.makefile.psi.*

abstract class MakefileVariableNamedElementImpl(private val node: ASTNode) : ASTWrapperPsiElement(node), MakefileNamedElement {
  override fun toString() = "MakefileVariableImpl(VARIABLE)"
  override fun getNode(): ASTNode = node
}