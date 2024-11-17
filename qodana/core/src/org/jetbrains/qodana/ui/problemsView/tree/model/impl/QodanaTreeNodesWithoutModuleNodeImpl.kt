package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNodesWithoutModuleNode
import kotlin.io.path.Path

class QodanaTreeNodesWithoutModuleNodeImpl(
  private val moduleDataProvider: ModuleDataProvider,
  override val primaryData: QodanaTreeNodesWithoutModuleNode.PrimaryData,
  override val children: QodanaTreeModuleNode.Children,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeNodesWithoutModuleNode {
  companion object {
    fun newEmpty(treeContext: QodanaTreeContext, moduleDataProvider: ModuleDataProvider, excludedData: Set<ConfigExcludeItem>): QodanaTreeNodesWithoutModuleNodeImpl {
      val projectPath = Path("")
      val primaryData = QodanaTreeNodesWithoutModuleNode.PrimaryData(projectPath)
      val nodeExcludedData = filterExcludedData(primaryData, excludedData)
      val children = QodanaTreeModuleNodeImpl.newEmptyChildren(treeContext.groupByDirectory, projectPath, nodeExcludedData)
      return QodanaTreeNodesWithoutModuleNodeImpl(moduleDataProvider, primaryData, children, nodeExcludedData)
    }
  }

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludedData(primaryData, initExcludedData)
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean = false

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return moduleDataProvider.getModuleDataForSarifProblem(problem) == null
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeNodesWithoutModuleNode {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludedData(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeNodesWithoutModuleNodeImpl(moduleDataProvider, primaryData, newChildren, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeNodesWithoutModuleNode, QodanaTreeNodesWithoutModuleNode.PrimaryData>? {
    return QodanaUiTreeNodesWithoutModuleNode(parent, primaryData)
  }
}

private fun filterExcludedData(primaryData: QodanaTreeNodesWithoutModuleNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
}