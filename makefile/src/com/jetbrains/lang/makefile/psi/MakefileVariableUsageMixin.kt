package name.kropp.intellij.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.*
import name.kropp.intellij.makefile.psi.*

abstract class MakefileVariableUsageMixin internal constructor(node: ASTNode) : ASTWrapperPsiElement(node), MakefileVariableUsage {
  override fun getReferences(): Array<PsiReference> = myReference
  private val myReference by lazy { arrayOf<PsiReference>(MakefileVariableReference(this)) }
}