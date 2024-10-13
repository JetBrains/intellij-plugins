package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeSeverityNode

class QodanaTreeSeverityNodeImpl private constructor(
  override val primaryData: QodanaTreeSeverityNode.PrimaryData,
  override val children: QodanaTreeInspectionOrFileSystemLevelChildren,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeSeverityNode {
  companion object {
    fun newEmpty(treeContext: QodanaTreeContext, qodanaSeverity: QodanaSeverity, excludedData: Set<ConfigExcludeItem>): QodanaTreeSeverityNode {
      val primaryData = QodanaTreeSeverityNode.PrimaryData(qodanaSeverity)
      val nodeExcludedData = filterExcludedData(primaryData, excludedData)
      return QodanaTreeSeverityNodeImpl(
        primaryData,
        newEmptyInspectionOrFileSystemLevelChildren(treeContext, nodeExcludedData),
        nodeExcludedData
      )
    }
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludedData(primaryData, initExcludedData)
  }

  override val excluded: Boolean = false

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return problem.qodanaSeverity == primaryData.qodanaSeverity
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeSeverityNode {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludedData(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeSeverityNodeImpl(primaryData, newChildren, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeSeverityNode, QodanaTreeSeverityNode.PrimaryData>? {
    return QodanaUiTreeSeverityNode(parent, primaryData)
  }
}

private fun filterExcludedData(primaryData: QodanaTreeSeverityNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
}