package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import kotlin.io.path.Path

fun newEmptyFileSystemLevelChildren(
  treeContext: QodanaTreeContext,
  excludedData: Set<ConfigExcludeItem>,
): QodanaTreeFileSystemLevelChildren {
  return when {
    treeContext.moduleDataProvider != null -> {
      FileSystemModuleNodesImpl(treeContext, treeContext.moduleDataProvider, emptyList(), null, excludedData)
    }
    treeContext.groupByDirectory -> {
      FileSystemFileAndDirectoryNodesImpl(QodanaTreeFileAndDirectoryNodeChildrenImpl.newEmpty(Path(""), excludedData))
    }
    else -> {
      FileSystemFileNodesImpl(FileNodesChildrenImpl.newEmpty(Path(""), excludedData))
    }
  }
}

private class FileSystemModuleNodesImpl(
  private val treeContext: QodanaTreeContext,
  private val moduleDataProvider: ModuleDataProvider,
  override val moduleNodes: List<QodanaTreeModuleNode>,
  override val nodesWithoutModule: QodanaTreeNodesWithoutModuleNode?,
  private val excludedData: Set<ConfigExcludeItem>,
) : QodanaTreeFileSystemLevelChildren.ModuleNodes {
  override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
    get() = sequence {
      yieldAll(moduleNodes)
      nodesWithoutModule?.let { yield(it) }
    }

  override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(
    moduleNodes.size + (nodesWithoutModule?.let { 1 } ?: 0)
  )

  override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeFileSystemLevelChildren? {
    when (event) {
      is QodanaTreeProblemEvent -> {
        val modulePrimaryDataToEvents = event.sarifProblemsToProperties
          .groupBy { moduleDataProvider.getModuleDataForSarifProblem(it.problem) }
          .mapKeys { entry -> entry.key?.let { QodanaTreeModuleNode.PrimaryData(it) } }
          .mapValues { QodanaTreeProblemEvent(it.value.toSet()) }

        val eventForNodeWithoutModule = modulePrimaryDataToEvents[null]
        val newNodeWithoutModule = if (eventForNodeWithoutModule != null) {
          val nodeToEvent = mapOf(getOrCreateNodesWithoutModule() to eventForNodeWithoutModule)
          val result = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, nodeToEvent, listOfNotNull(nodesWithoutModule))
          when {
            result == null -> nodesWithoutModule
            result.isEmpty() -> null
            else -> result.first()
          }
        }
        else {
          nodesWithoutModule
        }

        val moduleNodesToEvents = moduleNodes.nodesToEvents(modulePrimaryDataToEvents.filterKeys { it != null }, pathBuilder) { primaryData, _ ->
          createNewModuleNode(primaryData!!.moduleData, moduleDataProvider)
        }
        val newModuleNodes = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, moduleNodesToEvents, moduleNodes) ?: moduleNodes

        if (newNodeWithoutModule === nodesWithoutModule && newModuleNodes === moduleNodes) return null

        return FileSystemModuleNodesImpl(treeContext, moduleDataProvider, newModuleNodes, newNodeWithoutModule, excludedData)
      }
      is QodanaTreeExcludeEvent -> {
        val newNodeWithoutModule = nodesWithoutModule?.processTreeEvent(event, pathBuilder)
        val newModuleNodes = computeNewQodanaChildrenNodesExcludeEvent(event, moduleNodes)
        return FileSystemModuleNodesImpl(treeContext, moduleDataProvider, newModuleNodes, newNodeWithoutModule, event.excludedData)
      }
    }
  }

  private fun getOrCreateNodesWithoutModule(): QodanaTreeNodesWithoutModuleNode {
    return nodesWithoutModule ?: QodanaTreeNodesWithoutModuleNodeImpl.newEmpty(treeContext, moduleDataProvider, excludedData)
  }

  private fun createNewModuleNode(moduleData: ModuleData, moduleDataProvider: ModuleDataProvider): QodanaTreeModuleNodeImpl {
    val moduleRelativePath = moduleData.modulePathRelativeToProject
    return QodanaTreeModuleNodeImpl.newEmpty(moduleData, moduleDataProvider, treeContext.groupByDirectory, moduleRelativePath, excludedData)
  }
}

private class FileSystemFileAndDirectoryNodesImpl(override val fileAndDirectoryNodeChildren: QodanaTreeFileAndDirectoryNodeChildren) :
  QodanaTreeFileSystemLevelChildren.FileAndDirectoryNodes,
  QodanaTreeNode.Children<QodanaTreeFileSystemLevelChildren> by delegateToChildren(fileAndDirectoryNodeChildren, { FileSystemFileAndDirectoryNodesImpl(it) })

private class FileSystemFileNodesImpl(override val fileNodesChildren: FileNodesChildren) :
  QodanaTreeFileSystemLevelChildren.FileNodes,
  QodanaTreeNode.Children<QodanaTreeFileSystemLevelChildren> by delegateToChildren(fileNodesChildren, { FileSystemFileNodesImpl(it) })