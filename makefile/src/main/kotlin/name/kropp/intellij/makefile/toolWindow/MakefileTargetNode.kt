package name.kropp.intellij.makefile.toolWindow

import com.intellij.util.enumeration.*
import name.kropp.intellij.makefile.*
import name.kropp.intellij.makefile.psi.*
import javax.swing.*
import javax.swing.tree.*

class MakefileTargetNode(target: MakefileTarget) : MakefileTreeNode(target.name ?: "") {
  override val icon: Icon
    get() = MakefileTargetIcon

  internal lateinit var parent: MakefileFileNode

  override fun children() = EmptyEnumeration.INSTANCE

  override fun isLeaf() = true

  override fun getChildCount() = 0

  override fun getParent() = parent

  override fun getChildAt(i: Int) = null

  override fun getIndex(node: TreeNode) = 0

  override fun getAllowsChildren() = false
}