package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ui.tree.TreeVisitor
import com.intellij.util.ui.tree.TreeUtil
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreePath
import javax.swing.tree.TreePath

class QodanaUiTreeVisitor(private val qodanaTreePath: QodanaTreePath) : TreeVisitor {
  private var currentPathIndex = 0

  private val lock = Object()

  override fun visitThread(): TreeVisitor.VisitThread = TreeVisitor.VisitThread.BGT

  override fun visit(path: TreePath): TreeVisitor.Action {
    synchronized(lock) {
      val qodanaUiTreeNode = TreeUtil.getLastUserObject(path) as? QodanaUiTreeNode<*, *> ?: return TreeVisitor.Action.SKIP_CHILDREN

      val currentNodeInTreePath = qodanaTreePath.primaryDataPath.getOrNull(currentPathIndex) == qodanaUiTreeNode.primaryData
      if (!currentNodeInTreePath) return TreeVisitor.Action.SKIP_CHILDREN

      currentPathIndex++
      if (currentPathIndex >= qodanaTreePath.primaryDataPath.size) return TreeVisitor.Action.INTERRUPT

      return TreeVisitor.Action.CONTINUE
    }
  }
}