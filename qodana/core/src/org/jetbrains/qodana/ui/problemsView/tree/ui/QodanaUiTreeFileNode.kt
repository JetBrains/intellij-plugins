package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import com.intellij.util.IconUtil
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.toSelectedNodeType
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeFileNode
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

class QodanaUiTreeFileNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeFileNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeFileNode, QodanaTreeFileNode.PrimaryData>(parent, primaryData) {
  override fun getNavigatable(): Navigatable? {
    val project = project ?: return null
    val modelTreeNode = modelTreeNode ?: return null

    val nodeNavigatable = super.getNavigatable() ?: return null
    return object : Navigatable by nodeNavigatable {
      override fun navigate(requestFocus: Boolean) {
        QodanaPluginStatsCounterCollector.PROBLEM_NAVIGATED.log(
          project,
          modelTreeNode.toSelectedNodeType(),
          modelTreeNode.problemsCount
        )
        nodeNavigatable.navigate(requestFocus)
      }

      override fun canNavigateToSource(): Boolean {  // needed because of bug KT-18324
        return nodeNavigatable.canNavigateToSource()
      }

      override fun canNavigate(): Boolean {
        return nodeNavigatable.canNavigate()
      }
    }
  }

  override fun computeModelTreeNode(): QodanaTreeFileNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> {
    val file = primaryData.file
    return excludeActionsDescriptorsForPathNode(
      file,
      pathOnlyTextProvider = { QodanaBundle.message("action.Qodana.ProblemTree.ExcludeFile.text", file.fileName) },
      pathWithInspectionTextProvider = { inspectionId ->
        QodanaBundle.message("action.Qodana.ProblemTree.ExcludeFileForInspection.text", inspectionId, file.fileName)
      }
    )
  }

  override fun getVirtualFile(): VirtualFile? = modelTreeNode?.virtualFile

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeFileNode) {
    val virtualFile = modelTreeNode.virtualFile
    presentation.setIcon(IconUtil.getIcon(virtualFile, 0, project))

    val parentDirectoryPath: Path? = primaryData.file.parent
    val parentNodePath = primaryData.parentPath
    val pathFromParentNodePathToDirectory = parentDirectoryPath?.let { parentNodePath.relativize(it) } ?: Path("")

    if (modelTreeNode.excluded) {
      presentation.addText(virtualFile.presentableName, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      if (pathFromParentNodePathToDirectory.nameCount != 0) {
        presentation.appendGrayedText(pathFromParentNodePathToDirectory.pathString)
      }
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }

    presentation.addText(virtualFile.presentableName, SimpleTextAttributes.REGULAR_ATTRIBUTES)

    if (pathFromParentNodePathToDirectory.nameCount != 0) {
      presentation.appendGrayedText(pathFromParentNodePathToDirectory.pathString)
    }
    presentation.appendProblemsCount(modelTreeNode.problemsCount)
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.NEVER
}