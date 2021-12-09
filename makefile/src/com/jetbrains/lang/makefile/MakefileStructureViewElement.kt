package com.jetbrains.lang.makefile

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.jetbrains.lang.makefile.psi.MakefileTarget

class MakefileStructureViewElement(private val element: PsiElement) : StructureViewTreeElement {
  override fun getPresentation(): ItemPresentation {
    if (element is MakefileTarget) return MakefileTargetStructureViewPresentation(element)
    return (element as NavigationItem).presentation!!
  }

  override fun getChildren(): Array<out TreeElement> {
    if (element is MakefileFile) {
      return element.targets.map(::MakefileStructureViewElement).toTypedArray()
    } else {
      return emptyArray()
    }
  }

  override fun canNavigate() = (element as? NavigationItem)?.canNavigate() ?: false
  override fun canNavigateToSource() = (element as? NavigationItem)?.canNavigateToSource() ?: false

  override fun navigate(requestFocus: Boolean) {
    (element as? NavigationItem)?.navigate(requestFocus)
  }

  override fun getValue() = element
}