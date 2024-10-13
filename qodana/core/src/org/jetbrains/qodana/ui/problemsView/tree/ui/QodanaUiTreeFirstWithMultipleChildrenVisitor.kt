package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ui.tree.TreeVisitor
import com.intellij.util.ui.tree.TreeUtil
import javax.swing.tree.TreePath

class QodanaUiTreeFirstWithMultipleChildrenVisitor : TreeVisitor {
  override fun visitThread(): TreeVisitor.VisitThread = TreeVisitor.VisitThread.BGT

  override fun visit(path: TreePath): TreeVisitor.Action {
    val qodanaUiTreeNode = TreeUtil.getLastUserObject(path) as? QodanaUiTreeNode<*, *> ?: return TreeVisitor.Action.SKIP_CHILDREN
    val modelTreeNode = qodanaUiTreeNode.modelTreeNode ?: return TreeVisitor.Action.SKIP_CHILDREN

    val childrenCount = modelTreeNode.children.nodesSequence.count()
    return if (childrenCount > 1) TreeVisitor.Action.INTERRUPT else TreeVisitor.Action.CONTINUE
  }
}