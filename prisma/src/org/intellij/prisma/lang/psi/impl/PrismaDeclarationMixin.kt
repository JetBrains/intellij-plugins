package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaBlock
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaDeclarationMixin<S : PrismaNamedStub<*>> : PrismaNamedElementImpl<S>, PrismaDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: S, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

  override fun getBlock(): PrismaBlock? = findChildByClass(PrismaBlock::class.java)

  override fun getMembers(): List<PrismaMemberDeclaration> =
    getBlock()?.getMembers()?.filterIsInstance<PrismaMemberDeclaration>() ?: emptyList()

  override fun findMemberByName(name: String): PrismaMemberDeclaration? =
    getBlock()?.findMemberByName(name) as? PrismaMemberDeclaration

  override fun processDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement,
  ): Boolean {
    for (member in getMembers()) {
      if (!processor.execute(member, state)) {
        return false
      }
    }

    return super.processDeclarations(processor, state, lastParent, place)
  }
}