package name.kropp.intellij.makefile.toolWindow

import javax.swing.*
import javax.swing.tree.*

abstract class MakefileTreeNode(val name: String) : TreeNode {
  abstract val icon: Icon
}