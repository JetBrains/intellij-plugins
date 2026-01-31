package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.jetbrains.lang.makefile.MakefileVariableReference
import com.jetbrains.lang.makefile.psi.MakefileVariableUsage

abstract class MakefileVariableUsageMixin internal constructor(node: ASTNode) : ASTWrapperPsiElement(node), MakefileVariableUsage {
  override fun getReferences(): Array<PsiReference> = myReference
  private val myReference by lazy { arrayOf<PsiReference>(MakefileVariableReference(this)) }
}