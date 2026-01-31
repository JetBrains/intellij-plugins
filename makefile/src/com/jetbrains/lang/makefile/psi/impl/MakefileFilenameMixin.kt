package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet

open class MakefileFilenameMixin internal constructor(astNode: ASTNode) : ASTWrapperPsiElement(astNode) {
  override fun getReferences(): Array<out FileReference> = FileReferenceSet(node.psi).allReferences
}
