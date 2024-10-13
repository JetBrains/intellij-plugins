package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.highlight.InspectionInfoProvider
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeInspectionNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode

class QodanaTreeInspectionNodeImpl(
  private val inspectionInfoProvider: InspectionInfoProvider,
  override val primaryData: QodanaTreeInspectionNode.PrimaryData,
  override val children: QodanaTreeFileSystemLevelChildren,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeInspectionNode {
  companion object {
    fun newEmpty(treeContext: QodanaTreeContext, inspectionId: String, excludedData: Set<ConfigExcludeItem>): QodanaTreeInspectionNode {
      val primaryData = QodanaTreeInspectionNode.PrimaryData(inspectionId)
      val childrenExcludedData = filterExcludeDataForChildren(primaryData, excludedData)

      val fileSystemLevelChildren = newEmptyFileSystemLevelChildren(treeContext, childrenExcludedData)
      return QodanaTreeInspectionNodeImpl(
        treeContext.inspectionInfoProvider,
        primaryData,
        fileSystemLevelChildren,
        excludedData
      )
    }
  }

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludeData(primaryData, initExcludedData).toSet()
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean by lazy { isNodeToExclude(excludedData) }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return problem.inspectionId == primaryData.inspectionId
  }

  private fun isNodeToExclude(items: Set<ConfigExcludeItem>): Boolean {
    return items.any {
      it.path == null && (it.inspectionId == primaryData.inspectionId || it.inspectionId == null)
    }
  }

  override val inspectionName: String?
    get() = inspectionInfoProvider.getName(primaryData.inspectionId)

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeInspectionNode {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludeData(primaryData, it).toSet() },
      childrenExcludeDataFilter = { filterExcludeDataForChildren(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeInspectionNodeImpl(inspectionInfoProvider, primaryData, newChildren, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeInspectionNode, QodanaTreeInspectionNode.PrimaryData>? {
    return QodanaUiTreeInspectionNode(parent, primaryData)
  }
}

private fun filterExcludeData(primaryData: QodanaTreeInspectionNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Sequence<ConfigExcludeItem> {
  return excludedData
    .asSequence()
    .filter {
      it.inspectionId == null || it.inspectionId == primaryData.inspectionId
    }
}

private fun filterExcludeDataForChildren(primaryData: QodanaTreeInspectionNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return filterExcludeData(primaryData, excludedData)
    .map {
      ConfigExcludeItem(inspectionId = null, it.path)
    }
    .toSet()
}
