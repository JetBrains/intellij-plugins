package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeInspectionCategoryNode

class QodanaUiTreeInspectionCategoryNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeInspectionCategoryNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeInspectionCategoryNode, QodanaTreeInspectionCategoryNode.PrimaryData>(parent, primaryData) {
  override fun computeModelTreeNode(): QodanaTreeInspectionCategoryNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> = emptyList()

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeInspectionCategoryNode) {
    if (modelTreeNode.excluded) {
      presentation.addText(primaryData.inspectionCategory, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }
    presentation.addText(primaryData.inspectionCategory, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}