package name.kropp.intellij.makefile.toolWindow

import com.intellij.ui.*
import javax.swing.*

class MakefileCellRenderer : ColoredTreeCellRenderer() {
  override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
    value as MakefileTreeNode
    icon = value.icon
    if (value is MakefileTargetNode && value.target.isSpecialTarget) {
      append(value.name, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES)
    } else {
      append(value.name)
    }
  }
}