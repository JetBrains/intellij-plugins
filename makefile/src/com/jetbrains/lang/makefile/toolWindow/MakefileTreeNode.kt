package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.util.NlsSafe
import javax.swing.*
import javax.swing.tree.*

abstract class MakefileTreeNode(@NlsSafe val name: String) : TreeNode {
  abstract val icon: Icon
  object Comparator : kotlin.Comparator<MakefileTreeNode> {
    override fun compare(a: MakefileTreeNode, b: MakefileTreeNode): Int = a.name.compareTo(b.name, ignoreCase = true)
  }
}