package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeDirectoryNode
import kotlin.io.path.pathString

class QodanaUiTreeDirectoryNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeDirectoryNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeDirectoryNode, QodanaTreeDirectoryNode.PrimaryData>(parent, primaryData) {
  override fun computeModelTreeNode(): QodanaTreeDirectoryNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> {
    val directoryUiPath = primaryData.ownPath.toString()
    return excludeActionsDescriptorsForPathNode(
      path = primaryData.fullPath,
      pathOnlyTextProvider = { QodanaBundle.message("action.Qodana.ProblemTree.ExcludeDirectory.text", directoryUiPath) },
      pathWithInspectionTextProvider = { inspectionId ->
        QodanaBundle.message("action.Qodana.ProblemTree.ExcludeDirectoryForInspection.text", inspectionId, directoryUiPath)
      }
    )
  }

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeDirectoryNode) {
    presentation.setIcon(AllIcons.Nodes.Folder)
    if (modelTreeNode.excluded) {
      presentation.addText(primaryData.ownPath.pathString, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }
    presentation.addText(primaryData.ownPath.pathString, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}