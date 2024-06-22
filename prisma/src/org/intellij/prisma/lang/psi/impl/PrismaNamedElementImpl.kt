package org.intellij.prisma.lang.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.startOffset
import org.intellij.prisma.lang.presentation.getPresentation
import org.intellij.prisma.lang.presentation.icon
import org.intellij.prisma.lang.psi.PrismaElementFactory
import org.intellij.prisma.lang.psi.PrismaElementTypes
import org.intellij.prisma.lang.psi.PrismaNameIdentifierOwner
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub
import org.intellij.prisma.lang.resolve.getSchemaScope
import javax.swing.Icon

abstract class PrismaNamedElementImpl<S : PrismaNamedStub<*>> : StubBasedPsiElementBase<S>, PrismaNameIdentifierOwner, StubBasedPsiElement<S> {
  constructor(node: ASTNode) : super(node)

  constructor(stub: S, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

  override fun getName(): String? = nameIdentifier?.text

  override fun setName(name: String): PsiElement {
    nameIdentifier?.replace(PrismaElementFactory.createIdentifier(project, name))
    return this
  }

  override fun getNameIdentifier(): PsiElement? = findChildByType(PrismaElementTypes.IDENTIFIER)

  override fun getTextOffset(): Int = nameIdentifier?.startOffset ?: super.getTextOffset()

  override fun getPresentation(): ItemPresentation? = getPresentation(this)

  override fun getIcon(flags: Int): Icon? = icon

  override fun getUseScope(): SearchScope = getSchemaScope(this)

  override fun toString(): String = "${javaClass.simpleName}($elementType)"
}