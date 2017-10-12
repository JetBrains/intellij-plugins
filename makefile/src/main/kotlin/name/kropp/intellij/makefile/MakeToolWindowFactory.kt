package name.kropp.intellij.makefile

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.TreeList
import com.intellij.util.enumeration.EmptyEnumeration
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class MakeToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.title = "make"

    val model = DefaultTreeModel(MakefileTargetNode("all"))
    val tree = TreeList(model)
    tree.setCellRenderer(MakefileCellRenderer())

    toolWindow.component.add(tree)
  }
}

class MakefileCellRenderer : ListCellRenderer<MakefileTargetNode> {
  override fun getListCellRendererComponent(p0: JList<out MakefileTargetNode>?, p1: MakefileTargetNode?, p2: Int, p3: Boolean, p4: Boolean): Component {
    return JLabel(p0?.name, MakefileTargetIcon, 0)
  }

}


class MakefileTargetNode(val name: String) : TreeNode {
  override fun children() = EmptyEnumeration.INSTANCE

  override fun isLeaf() = true

  override fun getChildCount() = 0

  override fun getParent() = null

  override fun getChildAt(i: Int) = null

  override fun getIndex(node: TreeNode) = 0

  override fun getAllowsChildren() = false
}
