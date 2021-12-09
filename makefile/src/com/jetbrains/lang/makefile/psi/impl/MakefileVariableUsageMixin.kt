package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*
import com.jetbrains.lang.makefile.*
import com.jetbrains.lang.makefile.psi.*

abstract class MakefileVariableUsageMixin internal constructor(node: ASTNode) : ASTWrapperPsiElement(node), MakefileVariableUsage {
  override fun getReferences(): Array<PsiReference> = myReference
  private val myReference by lazy { arrayOf<PsiReference>(MakefileVariableReference(this)) }
}