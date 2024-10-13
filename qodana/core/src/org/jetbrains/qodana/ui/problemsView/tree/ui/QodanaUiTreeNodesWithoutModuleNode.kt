package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNodesWithoutModuleNode
import kotlin.io.path.pathString

class QodanaUiTreeNodesWithoutModuleNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeNodesWithoutModuleNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeNodesWithoutModuleNode, QodanaTreeNodesWithoutModuleNode.PrimaryData>(parent, primaryData) {
  override fun computeModelTreeNode(): QodanaTreeNodesWithoutModuleNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> = emptyList()

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeNodesWithoutModuleNode) {
    if (modelTreeNode.excluded) {
      presentation.addText(primaryData.path.pathString, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }
    presentation.addText(primaryData.path.pathString, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}