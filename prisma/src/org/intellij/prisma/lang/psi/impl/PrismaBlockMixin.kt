package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.childrenOfType
import org.intellij.prisma.lang.psi.PrismaBlock
import org.intellij.prisma.lang.psi.PrismaElement
import org.intellij.prisma.lang.psi.PrismaNamedElement

abstract class PrismaBlockMixin(node: ASTNode) : PrismaElementImpl(node), PrismaBlock {

  override fun getMembers(): List<PrismaElement> = childrenOfType()

  override fun findMemberByName(name: String): PrismaNamedElement? {
    return getMembersByName()[name]
  }

  override fun getMembersByName(): Map<String, PrismaNamedElement> {
    return CachedValuesManager.getCachedValue(this) {
      val members = mutableMapOf<String, PrismaNamedElement>()
      for (element in getMembers().filterIsInstance<PrismaNamedElement>()) {
        val name = element.name
        if (!name.isNullOrEmpty()) {
          members[name] = element
        }
      }
      CachedValueProvider.Result.create(members, this)
    }
  }
}