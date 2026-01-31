package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.jetbrains.lang.makefile.psi.MakefileNamedElement

abstract class MakefileVariableNamedElementImpl(private val node: ASTNode) : ASTWrapperPsiElement(node), MakefileNamedElement {
  override fun toString() = "MakefileVariableImpl(VARIABLE)"
  override fun getNode(): ASTNode = node
}