package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*

fun newEmptyInspectionOrFileSystemLevelChildren(
  treeContext: QodanaTreeContext,
  excludedData: Set<ConfigExcludeItem>
): QodanaTreeInspectionOrFileSystemLevelChildren {
  return if (treeContext.groupByInspection) {
    InspectionCategoryNodesImpl(treeContext, emptyList(), excludedData)
  } else {
    FileSystemLevelNodesImpl(newEmptyFileSystemLevelChildren(treeContext, excludedData))
  }
}

private class InspectionCategoryNodesImpl(
  private val treeContext: QodanaTreeContext,
  override val nodes: List<QodanaTreeInspectionCategoryNode>,
  private val excludedData: Set<ConfigExcludeItem>,
) : QodanaTreeInspectionOrFileSystemLevelChildren.InspectionCategoryNodes {
  override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
    get() = nodes.asSequence()

  override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(
    nodes.size
  )

  override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeInspectionOrFileSystemLevelChildren.InspectionCategoryNodes? {
    return when (event) {
      is QodanaTreeProblemEvent -> {
        val primaryDataToEvents = event.sarifProblemsToProperties
          .groupBy { treeContext.inspectionInfoProvider.getCategory(it.problem.inspectionId) }
          .mapKeys { QodanaTreeInspectionCategoryNode.PrimaryData(it.key) }
          .mapValues { QodanaTreeProblemEvent(it.value.toSet()) }

        val nodesToEvents = nodes.nodesToEvents(primaryDataToEvents, pathBuilder) { primaryData, _ ->
          QodanaTreeInspectionCategoryNodeImpl.newEmpty(primaryData.inspectionCategory, treeContext, excludedData)
        }

        val newNodes = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, nodesToEvents, nodes) ?: return null
        InspectionCategoryNodesImpl(treeContext, newNodes, excludedData)
      }
      is QodanaTreeExcludeEvent -> {
        val newNodes = computeNewQodanaChildrenNodesExcludeEvent(event, nodes)
        InspectionCategoryNodesImpl(treeContext, newNodes, event.excludedData)
      }
    }
  }
}

private class FileSystemLevelNodesImpl(override val fileSystemLevelChildren: QodanaTreeFileSystemLevelChildren) :
  QodanaTreeInspectionOrFileSystemLevelChildren.FileSystemLevelNodes,
  QodanaTreeNode.Children<QodanaTreeInspectionOrFileSystemLevelChildren> by delegateToChildren(fileSystemLevelChildren, { FileSystemLevelNodesImpl(it) })
