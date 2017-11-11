package name.kropp.intellij.makefile

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.enumeration.ArrayEnumeration
import com.intellij.util.enumeration.EmptyEnumeration
import java.util.*
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class MakeToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.title = "make"

    val model = DefaultTreeModel(MakefileFileNode("Makefile", arrayOf(MakefileTargetNode("all"))))
    val tree = Tree(model)
    tree.cellRenderer = MakefileCellRenderer()

    toolWindow.component.add(tree)
  }
}

class MakefileCellRenderer : ColoredTreeCellRenderer() {
  override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
    value as MakefileTreeNode
    icon = value.icon
    append(value.name)
  }
}


abstract class MakefileTreeNode(val name: String) : TreeNode {
  abstract val icon: Icon
}

class MakefileFileNode(name: String, private val targets: Array<MakefileTargetNode>) : MakefileTreeNode(name) {
  init {
    for (target in targets) {
      target.parent = this
    }
  }

  override val icon: Icon
    get() = MakefileIcon

  override fun children(): Enumeration<*> = ArrayEnumeration(targets)

  override fun isLeaf() = false

  override fun getChildCount() = targets.size

  override fun getParent() = null

  override fun getChildAt(i: Int) = targets[i]

  override fun getIndex(node: TreeNode) = targets.indexOf(node)

  override fun getAllowsChildren() = true
}

class MakefileTargetNode(name: String) : MakefileTreeNode(name) {
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
