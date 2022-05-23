package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.*
import javax.swing.tree.*

abstract class MakefileTreeNode(@NlsSafe val name: String) : TreeNode {
  abstract val icon: Icon
  object Comparator : kotlin.Comparator<MakefileTreeNode> {
    override fun compare(a: MakefileTreeNode, b: MakefileTreeNode): Int = a.name.compareTo(b.name, ignoreCase = true)
  }

  protected open fun renderIcon(renderer: ColoredTreeCellRenderer) {
    renderer.icon = this.icon
  }

  protected open fun renderName(renderer: ColoredTreeCellRenderer) {
    renderer.append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
  }

  open fun render(renderer: ColoredTreeCellRenderer) {
    renderIcon(renderer)
    renderName(renderer)
  }
}