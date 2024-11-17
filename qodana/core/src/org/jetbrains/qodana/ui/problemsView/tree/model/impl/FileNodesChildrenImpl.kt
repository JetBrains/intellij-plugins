package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import java.nio.file.Path

class FileNodesChildrenImpl(
  private val parentPath: Path,
  override val nodes: List<QodanaTreeFileNode>,
  private val excludedData: Set<ConfigExcludeItem>
) : FileNodesChildren {
  companion object {
    fun newEmpty(parentPath: Path, excludedData: Set<ConfigExcludeItem>): FileNodesChildren {
      return FileNodesChildrenImpl(parentPath, emptyList(), excludedData)
    }
  }

  override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
    get() = nodes.asSequence()

  override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(nodes.size)

  override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): FileNodesChildrenImpl? {
    return when(event) {
      is QodanaTreeProblemEvent -> {
        val primaryDataToEvents = event.sarifProblemsToProperties
          .groupBy { it.problem.relativeNioFile }
          .mapKeys { QodanaTreeFileNode.PrimaryData(parentPath, it.key) }
          .mapValues { QodanaTreeProblemEvent(it.value.toSet()) }

        val nodesToEvents = nodes.nodesToEvents(primaryDataToEvents, pathBuilder) { _, innerEvent ->
          val sarifProblemWithProperties = innerEvent.sarifProblemsToProperties.firstOrNull() ?: return@nodesToEvents null
          val project = sarifProblemWithProperties.project
          val problem = sarifProblemWithProperties.problem
          val virtualFile = innerEvent.sarifProblemsToProperties.asSequence().mapNotNull { it.file }.firstOrNull() ?: problem.getVirtualFile(project) ?: return@nodesToEvents null
          QodanaTreeFileNodeImpl.newEmpty(parentPath, problem.relativeNioFile, virtualFile, excludedData)
        }
        val newNodes = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, nodesToEvents, nodes) ?: return null
        FileNodesChildrenImpl(parentPath, newNodes, excludedData)
      }
      is QodanaTreeExcludeEvent -> {
        val newNodes = computeNewQodanaChildrenNodesExcludeEvent(event, nodes)
        FileNodesChildrenImpl(parentPath, newNodes, event.excludedData)
      }
    }
  }
}