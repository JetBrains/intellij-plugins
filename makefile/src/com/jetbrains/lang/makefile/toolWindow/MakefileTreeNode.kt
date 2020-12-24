package name.kropp.intellij.makefile.toolWindow

import javax.swing.*
import javax.swing.tree.*

abstract class MakefileTreeNode(val name: String) : TreeNode {
  abstract val icon: Icon
  object Comparator : kotlin.Comparator<MakefileTreeNode> {
    override fun compare(a: MakefileTreeNode, b: MakefileTreeNode): Int = a.name.compareTo(b.name, ignoreCase = true)
  }
}