package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeSeverityNode

class QodanaUiTreeSeverityNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData : QodanaTreeSeverityNode.PrimaryData
) : QodanaUiTreeNodeBase<QodanaTreeSeverityNode, QodanaTreeSeverityNode.PrimaryData>(parent, primaryData) {

  override fun computeModelTreeNode(): QodanaTreeSeverityNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> = emptyList()

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeSeverityNode) {
    val qodanaSeverity = primaryData.qodanaSeverity
    presentation.setIcon(qodanaSeverity.icon)
    if (modelTreeNode.excluded) {
      presentation.addText(qodanaSeverity.toString(), SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }
    presentation.addText(qodanaSeverity.toString(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}