package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemProperties
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeFileNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import java.nio.file.Path
import kotlin.io.path.pathString

class QodanaTreeFileNodeImpl(
  override val primaryData: QodanaTreeFileNode.PrimaryData,
  override val children: QodanaTreeFileNode.Children,
  override val virtualFile: VirtualFile,
  initExcludedData: Set<ConfigExcludeItem>
) : QodanaTreeFileNode {
  companion object {
    fun newEmpty(parentPath: Path, file: Path, virtualFile: VirtualFile, excludedData: Set<ConfigExcludeItem>): QodanaTreeFileNode {
      val primaryData = QodanaTreeFileNode.PrimaryData(parentPath, file)
      val nodeExcludedData = filterExcludedData(primaryData, excludedData)
      return QodanaTreeFileNodeImpl(
        primaryData,
        ChildrenImpl(emptyList(), virtualFile, nodeExcludedData),
        virtualFile,
        nodeExcludedData
      )
    }
  }

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludedData(primaryData, initExcludedData)
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean by lazy { isNodeToExclude() }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return primaryData.file == problem.relativeNioFile
  }

  private fun isNodeToExclude(): Boolean {
    return excludedData.any { item -> item.excludesPath(primaryData.file) }
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeFileNode {
    return processTreeEventBase(
      event,
      pathBuilder,
      excludeDataFilter = { filterExcludedData(primaryData, it) }
    ) { newChildren, newExcludedData ->
      QodanaTreeFileNodeImpl(primaryData, newChildren, virtualFile, newExcludedData)
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeFileNode, QodanaTreeFileNode.PrimaryData> {
    return QodanaUiTreeFileNode(parent, primaryData)
  }

  private class ChildrenImpl(
    override val nodes: List<QodanaTreeProblemNode>,
    private val virtualFile: VirtualFile,
    private val excludedData: Set<ConfigExcludeItem>
  ) : QodanaTreeFileNode.Children {
    override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
      get() = nodes.asSequence()

    override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(
      nodes.size
    )

    override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeFileNode.Children? {
      return when (event) {
        is QodanaTreeProblemEvent -> {
          val primaryDataToEvents = event.sarifProblemsToProperties
            .groupBy { it.problem }
            .mapKeys { QodanaTreeProblemNode.PrimaryData(it.key) }
            .mapValues { QodanaTreeProblemEvent(it.value.toSet()) }

          val nodesToEvents = nodes.nodesToEvents(primaryDataToEvents, pathBuilder) { primaryData, _ ->
            val properties = notValidSarifPropertiesForNodeCreation // actual properties will be passed further from events
            QodanaTreeProblemNodeImpl(primaryData, properties, virtualFile, excludedData)
          }
          val newChildren = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, nodesToEvents, nodes, updatePath = false) ?: return null
          ChildrenImpl(newChildren, virtualFile, excludedData)
        }
        is QodanaTreeExcludeEvent -> {
          val newChildren = computeNewQodanaChildrenNodesExcludeEvent(event, nodes)
          ChildrenImpl(newChildren, virtualFile, event.excludedData)
        }
      }
    }
  }
}

private fun filterExcludedData(primaryData: QodanaTreeFileNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
    .filter {
      it.isRelatedToPath(primaryData.file.pathString)
    }
    .toSet()
}

private val notValidSarifPropertiesForNodeCreation = SarifProblemProperties(
  isPresent = false,
  isMissing = true,
  isFixed = false,
  line = -1,
  column = -1
)