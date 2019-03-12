package name.kropp.intellij.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.util.*
import name.kropp.intellij.makefile.*
import name.kropp.intellij.makefile.psi.*

abstract class MakefilePrerequisiteMixin internal constructor(node: ASTNode) : ASTWrapperPsiElement(node), MakefilePrerequisite {

  override fun getReferences(): Array<PsiReference> {
    val targetReference = MakefileTargetReference(this)
    if (isPhonyTarget) {
      return arrayOf(targetReference)
    }
    val references = FileReferenceSet(this).allReferences
    return ArrayUtil.prepend(targetReference, references, PsiReference.ARRAY_FACTORY)
  }
}