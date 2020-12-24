package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.jetbrains.lang.makefile.psi.*

abstract class MakefileVariableNamedElementImpl(private val node: ASTNode) : ASTWrapperPsiElement(node), MakefileNamedElement {
  override fun toString() = "MakefileVariableImpl(VARIABLE)"
  override fun getNode(): ASTNode = node
}