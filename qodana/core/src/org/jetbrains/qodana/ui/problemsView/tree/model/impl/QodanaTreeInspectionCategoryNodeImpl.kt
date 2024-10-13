package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.highlight.InspectionInfoProvider
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeInspectionCategoryNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode

class QodanaTreeInspectionCategoryNodeImpl private constructor(
  private val inspectionInfoProvider: InspectionInfoProvider,
  override val primaryData: QodanaTreeInspectionCategoryNode.PrimaryData,
  override val children: QodanaTreeInspectionCategoryNode.Children,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeInspectionCategoryNode {
  companion object {
    fun newEmpty(
      inspectionCategory: @Nls String?,
      treeContext: QodanaTreeContext,
      excludedData: Set<ConfigExcludeItem>
    ): QodanaTreeInspectionCategoryNode {
      val primaryData = QodanaTreeInspectionCategoryNode.PrimaryData(inspectionCategory)
      val nodeExcludedData = filterExcludedData(primaryData, excludedData)
      return QodanaTreeInspectionCategoryNodeImpl(
        treeContext.inspectionInfoProvider,
        primaryData,
        Children(treeContext, emptyList(), nodeExcludedData),
        nodeExcludedData
      )
    }
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean = false

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludedData(primaryData, initExcludedData)
  }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return inspectionInfoProvider.getCategory(problem.inspectionId) == primaryData.inspectionCategory
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeInspectionCategoryNode {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludedData(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeInspectionCategoryNodeImpl(inspectionInfoProvider, primaryData, newChildren, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeInspectionCategoryNode, QodanaTreeInspectionCategoryNode.PrimaryData> {
    return QodanaUiTreeInspectionCategoryNode(parent, primaryData)
  }

  private class Children(
    val treeContext: QodanaTreeContext,
    override val nodes: List<QodanaTreeInspectionNode>,
    private val excludedData: Set<ConfigExcludeItem>,
  ) : QodanaTreeInspectionCategoryNode.Children {
    override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
      get() = nodes.asSequence()

    override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(
      nodes.size
    )

    override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeInspectionCategoryNode.Children? {
      return when (event) {
        is QodanaTreeProblemEvent -> {
          val primaryDataToEvents = event.sarifProblemsToProperties
            .groupBy { it.problem.inspectionId }
            .mapKeys { QodanaTreeInspectionNode.PrimaryData(it.key) }
            .mapValues { QodanaTreeProblemEvent(it.value.toSet()) }

          val nodesToEvents = nodes.nodesToEvents(primaryDataToEvents, pathBuilder) { primaryData, _ ->
            QodanaTreeInspectionNodeImpl.newEmpty(treeContext, primaryData.inspectionId, excludedData)
          }

          val newNodes = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, nodesToEvents, nodes) ?: return null
          Children(treeContext, newNodes, excludedData)
        }
        is QodanaTreeExcludeEvent -> {
          val newNodes = computeNewQodanaChildrenNodesExcludeEvent(event, nodes)
          Children(treeContext, newNodes, event.excludedData)
        }
      }
    }
  }
}

private fun filterExcludedData(primaryData: QodanaTreeInspectionCategoryNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
}