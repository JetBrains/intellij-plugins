package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode

class QodanaTreeRootImpl private constructor(
  override val children: QodanaTreeRoot.Children,
  override val primaryData: QodanaTreeRoot.PrimaryData,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeRoot {
  companion object {
    fun newEmpty(
      treeContext: QodanaTreeContext,
      rootData: QodanaTreeRoot.PrimaryData,
      excludedData: Set<ConfigExcludeItem>,
    ): QodanaTreeRoot {
      val newExcludedData = filterExcludedData(rootData, excludedData)

      val rootChildren = if (treeContext.groupBySeverity) {
        SeverityNodesImpl(treeContext, emptyList(), newExcludedData)
      } else {
        InspectionOrFileSystemLevelNodes(newEmptyInspectionOrFileSystemLevelChildren(treeContext, newExcludedData))
      }
      return QodanaTreeRootImpl(rootChildren, rootData, newExcludedData)
    }
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean = false

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludedData(primaryData, initExcludedData)
  }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean = true

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeRoot {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludedData(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeRootImpl(newChildren, primaryData, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeRoot, QodanaTreeRoot.PrimaryData>? = null

  private class SeverityNodesImpl(
    private val treeContext: QodanaTreeContext,
    override val nodes: List<QodanaTreeSeverityNode>,
    private val excludedData: Set<ConfigExcludeItem>,
  ) : QodanaTreeRoot.Children.SeverityNodes {
    override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
      get() = nodes.asSequence()

    override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(
      nodes.size
    )

    override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeRoot.Children? {
      return when(event) {
        is QodanaTreeProblemEvent -> {
          val primaryDataToEvents = event.sarifProblemsToProperties
            .groupBy { it.problem.qodanaSeverity }
            .mapKeys { QodanaTreeSeverityNode.PrimaryData(it.key) }
            .mapValues { QodanaTreeProblemEvent(it.value.toSet()) }

          val nodesToEvents = nodes.nodesToEvents(primaryDataToEvents, pathBuilder) { primaryData, _ ->
            QodanaTreeSeverityNodeImpl.newEmpty(treeContext, primaryData.qodanaSeverity, excludedData)
          }
          val newChildren = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, nodesToEvents, nodes) ?: return null
          SeverityNodesImpl(treeContext, newChildren, excludedData)
        }
        is QodanaTreeExcludeEvent -> {
          val newChildren = computeNewQodanaChildrenNodesExcludeEvent(event, nodes)
          SeverityNodesImpl(treeContext, newChildren, excludedData)
        }
      }
    }
  }

  private class InspectionOrFileSystemLevelNodes(override val inspectionOrFileSystemLevelChildren: QodanaTreeInspectionOrFileSystemLevelChildren) :
    QodanaTreeRoot.Children.InspectionOrFileSystemLevelNodes,
    QodanaTreeNode.Children<QodanaTreeRoot.Children> by delegateToChildren(inspectionOrFileSystemLevelChildren, { InspectionOrFileSystemLevelNodes(it) })
}

private fun filterExcludedData(primaryData: QodanaTreeRoot.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
}