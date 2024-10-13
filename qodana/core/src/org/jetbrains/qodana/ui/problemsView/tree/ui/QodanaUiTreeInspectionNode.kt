package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeInspectionNode

class QodanaUiTreeInspectionNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeInspectionNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeInspectionNode, QodanaTreeInspectionNode.PrimaryData,>(parent, primaryData) {
  override fun computeModelTreeNode(): QodanaTreeInspectionNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> {
    if (modelTreeNode?.excluded != false) return emptyList()
    val inspectionId = primaryData.inspectionId
    return listOf(
      QodanaUiTreeNode.ExcludeActionDescriptor(
        actionName = QodanaBundle.message("action.Qodana.ProblemTree.ExcludeInspection.text", inspectionId),
        ConfigExcludeItem(inspectionId, initPath = null)
      )
    )
  }

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeInspectionNode) {
    if (modelTreeNode.excluded) {
      presentation.addText(modelTreeNode.inspectionName ?: modelTreeNode.primaryData.inspectionId, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }
    presentation.addText(modelTreeNode.inspectionName ?: modelTreeNode.primaryData.inspectionId, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}