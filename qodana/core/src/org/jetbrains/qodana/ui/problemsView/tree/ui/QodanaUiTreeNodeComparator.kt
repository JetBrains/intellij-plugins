package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.analysis.problemsView.toolWindow.Node
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNodeComparator

object QodanaUiTreeNodeComparator : Comparator<Node> {
  override fun compare(node1: Node?, node2: Node?): Int {
    if (node1 !is QodanaUiTreeNode<*, *> || node2 !is QodanaUiTreeNode<*, *>) return 0
    val modelNode1 = node1.modelTreeNode ?: return 0
    val modelNode2 = node2.modelTreeNode ?: return 0

    return QodanaTreeNodeComparator.compare(modelNode1, modelNode2)
  }
}