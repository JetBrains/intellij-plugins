package tanvd.grazi.ide.ui.components.rules

import com.intellij.ide.ui.search.SearchUtil
import com.intellij.ui.CheckboxTree
import com.intellij.util.ui.UIUtil
import javax.swing.JTree

class GraziCheckboxTreeCellRenderer(val searchString: () -> String) : CheckboxTree.CheckboxTreeCellRenderer(true) {
    override fun customizeRenderer(tree: JTree?, node: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        if (node !is GraziTreeNode || tree !is GraziRulesTree) return

        val background = UIUtil.getTreeBackground(selected, true)
        UIUtil.changeBackGround(this, background)

        SearchUtil.appendFragments(searchString(), node.nodeText, node.attrs.style, node.attrs.fgColor, background, textRenderer)
    }
}
