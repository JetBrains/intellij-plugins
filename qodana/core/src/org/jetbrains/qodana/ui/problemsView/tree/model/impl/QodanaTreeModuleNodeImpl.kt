package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeModuleNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import java.nio.file.Path
import kotlin.io.path.pathString

class QodanaTreeModuleNodeImpl(
  private val moduleDataProvider: ModuleDataProvider,
  override val primaryData: QodanaTreeModuleNode.PrimaryData,
  override val children: QodanaTreeModuleNode.Children,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeModuleNode {
  companion object {
    fun newEmpty(
      moduleData: ModuleData,
      moduleDataProvider: ModuleDataProvider,
      groupByDirectory: Boolean,
      modulePath: Path,
      excludedData: Set<ConfigExcludeItem>
    ): QodanaTreeModuleNodeImpl {
      val primaryData = QodanaTreeModuleNode.PrimaryData(moduleData)
      val nodeExcludedData = filterExcludedData(primaryData, excludedData)
      return QodanaTreeModuleNodeImpl(
        moduleDataProvider,
        primaryData,
        newEmptyChildren(groupByDirectory, modulePath, nodeExcludedData),
        nodeExcludedData
      )
    }

    fun newEmptyChildren(groupByDirectory: Boolean, modulePath: Path, excludedData: Set<ConfigExcludeItem>): QodanaTreeModuleNode.Children {
      return if (groupByDirectory) {
        FileAndDirectoryNodesImpl(QodanaTreeFileAndDirectoryNodeChildrenImpl.newEmpty(modulePath, excludedData))
      } else {
        FileNodesImpl(FileNodesChildrenImpl.newEmpty(modulePath, excludedData))
      }
    }
  }

  override val excludedData: Set<ConfigExcludeItem> by lazy { filterExcludedData(primaryData, initExcludedData) }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean by lazy { isNodeToExclude() }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return moduleDataProvider.getModuleDataForSarifProblem(problem) == primaryData.moduleData
  }

  private fun isNodeToExclude(): Boolean {
    return excludedData.any { item -> item.excludesPath(primaryData.moduleData.modulePathRelativeToProject) }
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeModuleNode {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludedData(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeModuleNodeImpl(moduleDataProvider, primaryData, newChildren, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeModuleNode, QodanaTreeModuleNode.PrimaryData> {
    return QodanaUiTreeModuleNode(parent, primaryData)
  }

  private class FileAndDirectoryNodesImpl(override val fileAndDirectoryNodeChildren: QodanaTreeFileAndDirectoryNodeChildren) :
    QodanaTreeModuleNode.Children.FileAndDirectoryNodes,
    QodanaTreeNode.Children<QodanaTreeModuleNode.Children> by delegateToChildren(fileAndDirectoryNodeChildren, { FileAndDirectoryNodesImpl(it) })

  private data class FileNodesImpl(override val fileNodesChildren: FileNodesChildren) :
    QodanaTreeModuleNode.Children.FileNodes,
    QodanaTreeNode.Children<QodanaTreeModuleNode.Children> by delegateToChildren(fileNodesChildren, { FileNodesImpl(it) })
}

private fun filterExcludedData(primaryData: QodanaTreeModuleNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
    .filter {
      it.isRelatedToPath(primaryData.moduleData.modulePathRelativeToProject.pathString)
    }
    .toSet()
}
