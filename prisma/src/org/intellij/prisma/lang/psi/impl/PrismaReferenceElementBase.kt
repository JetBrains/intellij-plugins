package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.intellij.prisma.lang.psi.PrismaReferenceElement
import org.intellij.prisma.lang.resolve.PrismaReference

abstract class PrismaReferenceElementBase(node: ASTNode) : PrismaElementImpl(node), PrismaReferenceElement {

  final override fun getReference(): PrismaReference? {
    return CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result.create(createReference(), this)
    }
  }

  protected abstract fun createReference(): PrismaReference?

  override fun resolve(): PsiElement? = reference?.resolve()
}