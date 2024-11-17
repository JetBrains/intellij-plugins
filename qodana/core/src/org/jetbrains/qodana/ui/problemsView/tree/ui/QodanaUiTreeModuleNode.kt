package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeModuleNode
import kotlin.io.path.pathString

class QodanaUiTreeModuleNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeModuleNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeModuleNode, QodanaTreeModuleNode.PrimaryData>(parent, primaryData) {
  override fun computeModelTreeNode(): QodanaTreeModuleNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> {
    val moduleName = primaryData.moduleData.module.name
    return excludeActionsDescriptorsForPathNode(
      path = primaryData.moduleData.modulePathRelativeToProject,
      pathOnlyTextProvider = { QodanaBundle.message("action.Qodana.ProblemTree.ExcludeModule.text", moduleName) },
      pathWithInspectionTextProvider = { inspectionId ->
        QodanaBundle.message("action.Qodana.ProblemTree.ExcludeModuleForInspection.text", inspectionId, moduleName)
      }
    )
  }

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeModuleNode) {
    val moduleData = primaryData.moduleData
    val module = moduleData.module
    val icon = if (module.isDisposed) ModuleType.EMPTY.icon else ModuleType.get(module).icon

    presentation.setIcon(icon)
    if (modelTreeNode.excluded) {
      presentation.addText(module.name, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }
    presentation.addText(module.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    presentation.appendGrayedText(moduleData.modulePathRelativeToProject.pathString)
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}