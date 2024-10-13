package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemProperties
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeProblemNode

class QodanaTreeProblemNodeImpl(
  override val primaryData: QodanaTreeProblemNode.PrimaryData,
  override val sarifProblemProperties: SarifProblemProperties,
  override val virtualFile: VirtualFile,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeProblemNode {
  override val problemsCount: Int
    get() =  if (sarifProblemProperties.isMissing || sarifProblemProperties.isFixed || excluded) 0 else 1

  override val excluded by lazy { isNodeToExclude() }

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludedData(primaryData, initExcludedData)
  }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return problem == primaryData.sarifProblem
  }

  private fun isNodeToExclude(): Boolean {
    return excludedData.any { item -> item.excludesPath(primaryData.sarifProblem.relativeNioFile, primaryData.sarifProblem.inspectionId) }
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeProblemNode {
    return when (event) {
      is QodanaTreeProblemEvent -> {
        val newActualProperties = event.sarifProblemsToProperties.firstOrNull() ?: return this
        if (newActualProperties.properties == sarifProblemProperties) return this
        QodanaTreeProblemNodeImpl(primaryData, newActualProperties.properties, virtualFile, excludedData)
      }
      is QodanaTreeExcludeEvent -> {
        val newExcludedData = filterExcludedData(primaryData, event.excludedData)
        if (newExcludedData == excludedData) return this

        QodanaTreeProblemNodeImpl(primaryData, sarifProblemProperties, virtualFile, newExcludedData)
      }
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeProblemNode, QodanaTreeProblemNode.PrimaryData> {
    return QodanaUiTreeProblemNode(parent, primaryData)
  }
}

private fun filterExcludedData(primaryData: QodanaTreeProblemNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
}