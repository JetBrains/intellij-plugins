package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.util.ArrayUtil
import com.jetbrains.lang.makefile.MakefileTargetReference
import com.jetbrains.lang.makefile.psi.MakefilePrerequisite

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