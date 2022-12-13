package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.suggested.startOffset
import org.intellij.prisma.lang.presentation.getPresentation
import org.intellij.prisma.lang.presentation.icon
import org.intellij.prisma.lang.psi.PrismaElementFactory
import org.intellij.prisma.lang.psi.PrismaElementTypes
import org.intellij.prisma.lang.psi.PrismaNameIdentifierOwner
import javax.swing.Icon

abstract class PrismaNamedElementImpl(node: ASTNode) : PrismaElementImpl(node), PrismaNameIdentifierOwner {
  override fun getName(): String? = nameIdentifier?.text

  override fun setName(name: String): PsiElement {
    nameIdentifier?.replace(PrismaElementFactory.createIdentifier(project, name))
    return this
  }

  override fun getNameIdentifier(): PsiElement? = findChildByType(PrismaElementTypes.IDENTIFIER)

  override fun getTextOffset(): Int = nameIdentifier?.startOffset ?: super.getTextOffset()

  override fun getPresentation(): ItemPresentation? = getPresentation(this)

  override fun getIcon(flags: Int): Icon? = icon

  override fun getUseScope(): SearchScope {
    return LocalSearchScope(containingFile)
  }
}