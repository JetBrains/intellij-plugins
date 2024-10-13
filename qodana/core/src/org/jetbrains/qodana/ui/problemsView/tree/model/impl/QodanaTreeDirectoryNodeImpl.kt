package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeDirectoryNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import java.nio.file.Path
import kotlin.io.path.pathString

class QodanaTreeDirectoryNodeImpl(
  override val primaryData: QodanaTreeDirectoryNode.PrimaryData,
  override val children: QodanaTreeFileAndDirectoryNodeChildren,
  initExcludedData: Set<ConfigExcludeItem> = emptySet()
) : QodanaTreeDirectoryNode {
  companion object {
    fun newEmpty(parentPath: Path, fullPath: Path, excludedData: Set<ConfigExcludeItem>): QodanaTreeDirectoryNodeImpl {
      val primaryData = QodanaTreeDirectoryNode.PrimaryData(fullPath, parentPath)
      val nodeExcludedData = filterExcludeData(primaryData, excludedData)
      return QodanaTreeDirectoryNodeImpl(primaryData, QodanaTreeFileAndDirectoryNodeChildrenImpl.newEmpty(fullPath, nodeExcludedData), nodeExcludedData)
    }
  }

  override val excludedData: Set<ConfigExcludeItem> by lazy {
    filterExcludeData(primaryData, initExcludedData)
  }

  override val problemsCount: Int by lazyChildrenProblemsCount()

  override val excluded: Boolean by lazy { isNodeToExclude() }

  override fun isRelatedToProblem(problem: SarifProblem): Boolean {
    return problem.relativeNioFile.startsWith(primaryData.fullPath)
  }

  private fun isNodeToExclude(): Boolean {
    return excludedData.any { item -> item.excludesPath(primaryData.fullPath) }
  }

  override fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeDirectoryNode {
    return when (event) {
      is QodanaTreeProblemEvent -> {
        val pathBuilderToPassForward = QodanaTreePath.Builder()
        val newChildren = children.computeNewChildren(event, pathBuilderToPassForward) ?: return this

        val isSingleDirectoryNodeChildLeft = newChildren.let {
          it.fileNodeChildren.isEmpty() && it.directoryNodeChildren.size == 1
        }

        val result = if (isSingleDirectoryNodeChildLeft) {
          val singleDirectoryNodeChild = newChildren.directoryNodeChildren.first()
          val newDirectoryNodePrimaryData = QodanaTreeDirectoryNode.PrimaryData(
            fullPath = singleDirectoryNodeChild.primaryData.fullPath,
            parentPath = this.primaryData.parentPath
          )
          QodanaTreeDirectoryNodeImpl(newDirectoryNodePrimaryData, singleDirectoryNodeChild.children, excludedData)
        } else {
          pathBuilder.addPath(pathBuilderToPassForward.buildPath())
          QodanaTreeDirectoryNodeImpl(primaryData, newChildren, excludedData)
        }
        result
      }
      is QodanaTreeExcludeEvent -> {
        processExcludeEventBase(event, excludeDataFilter = { filterExcludeData(primaryData, it) }) { newChildren, newExcludedData ->
          QodanaTreeDirectoryNodeImpl(primaryData, newChildren, newExcludedData)
        }
      }
    }
  }

  override fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<QodanaTreeDirectoryNode, QodanaTreeDirectoryNode.PrimaryData> {
    return QodanaUiTreeDirectoryNode(parent, primaryData)
  }
}

private fun filterExcludeData(primaryData: QodanaTreeDirectoryNode.PrimaryData, excludedData: Set<ConfigExcludeItem>): Set<ConfigExcludeItem> {
  return excludedData
    .filter {
      it.isRelatedToPath(primaryData.fullPath.pathString)
    }
    .toSet()
}