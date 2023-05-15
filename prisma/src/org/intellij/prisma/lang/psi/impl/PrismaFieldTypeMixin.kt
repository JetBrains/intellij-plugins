package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.intellij.prisma.lang.psi.PrismaFieldType
import org.intellij.prisma.lang.psi.PrismaTypeReference
import org.intellij.prisma.lang.types.PrismaType
import org.intellij.prisma.lang.types.createTypeFromSignature

abstract class PrismaFieldTypeMixin(node: ASTNode) : PrismaElementImpl(node), PrismaFieldType {
  override fun getTypeReference(): PrismaTypeReference = findNotNullChildByClass(PrismaTypeReference::class.java)

  override val type: PrismaType
    get() = CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result.create(createTypeFromSignature(this), containingFile)
    }

}