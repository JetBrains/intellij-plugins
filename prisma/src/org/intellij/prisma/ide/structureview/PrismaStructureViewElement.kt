package org.intellij.prisma.ide.structureview

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaFile

class PrismaStructureViewElement(val element: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {
  override fun getPresentation(): ItemPresentation = element.presentation ?: PresentationData()

  override fun navigate(requestFocus: Boolean) = element.navigate(requestFocus)

  override fun canNavigate(): Boolean = element.canNavigate()

  override fun canNavigateToSource(): Boolean = element.canNavigateToSource()

  override fun getValue(): Any = element

  override fun getAlphaSortKey(): String = element.name.orEmpty()

  override fun getChildren(): Array<TreeElement> {
    return when (element) {
      is PrismaFile -> element.declarations.map { PrismaStructureViewElement(it) }.toTypedArray()
      is PrismaDeclaration -> element.getMembers().map { PrismaStructureViewElement(it) }.toTypedArray()
      else -> emptyArray()
    }
  }
}