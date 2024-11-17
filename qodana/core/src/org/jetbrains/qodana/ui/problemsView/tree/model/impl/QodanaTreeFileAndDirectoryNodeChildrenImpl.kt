package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.problem.SarifProblemWithPropertiesAndFile
import org.jetbrains.qodana.problem.findRelativeVirtualFile
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import java.nio.file.Path
import kotlin.io.path.Path

class QodanaTreeFileAndDirectoryNodeChildrenImpl(
  private val currentFullPath: Path,
  override val fileNodeChildren: List<QodanaTreeFileNode>,
  override val directoryNodeChildren: List<QodanaTreeDirectoryNode>,
  private val excludedData: Set<ConfigExcludeItem>,
) : QodanaTreeFileAndDirectoryNodeChildren {
  companion object {
    fun newEmpty(path: Path, excludedData: Set<ConfigExcludeItem>): QodanaTreeFileAndDirectoryNodeChildren {
      return QodanaTreeFileAndDirectoryNodeChildrenImpl(path, emptyList(), emptyList(), excludedData)
    }
  }

  override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
    get() = sequence {
      yieldAll(directoryNodeChildren)
      yieldAll(fileNodeChildren)
    }

  override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>? by lazyNodeByPrimaryDataFinder(
    fileNodeChildren.size + directoryNodeChildren.size
  )

  override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): QodanaTreeFileAndDirectoryNodeChildren? {
    return when(event) {
      is QodanaTreeProblemEvent -> {
        val (childrenToProcess, pathBuilderToPassForward) = computeNewChildrenWithProblemNodeAndPathBuilder(event, pathBuilder)
        if (childrenToProcess.children !== this) return childrenToProcess.children.computeNewChildren(event, pathBuilderToPassForward)

        val fileNodesToEvents = childrenToProcess.fileChildMapping.map { it.key to QodanaTreeProblemEvent(it.value) }.toMap()
        val directoryNodesToEvents = childrenToProcess.directoryChildMapping.map { it.key to QodanaTreeProblemEvent(it.value) }.toMap()

        val newFileNodeChildren = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, fileNodesToEvents, fileNodeChildren) ?: fileNodeChildren
        val newDirectoryNodeChildren = computeNewQodanaChildrenNodesProblemEvent(pathBuilder, directoryNodesToEvents, directoryNodeChildren) ?: directoryNodeChildren
        if (newFileNodeChildren === fileNodeChildren && newDirectoryNodeChildren === directoryNodeChildren) return null

        QodanaTreeFileAndDirectoryNodeChildrenImpl(currentFullPath, newFileNodeChildren, newDirectoryNodeChildren, excludedData)
      }
      is QodanaTreeExcludeEvent -> {
        val newFileNodeChildren = computeNewQodanaChildrenNodesExcludeEvent(event, fileNodeChildren)
        val newDirectoryNodeChildren = computeNewQodanaChildrenNodesExcludeEvent(event, directoryNodeChildren)
        QodanaTreeFileAndDirectoryNodeChildrenImpl(currentFullPath, newFileNodeChildren, newDirectoryNodeChildren, event.excludedData)
      }
    }
  }

  private fun computeNewChildrenWithProblemNodeAndPathBuilder(
    event: QodanaTreeProblemEvent,
    pathBuilder: QodanaTreePath.Builder,
  ): Pair<NewChildrenWithMappings, QodanaTreePath.Builder> {
    val childrenToProcess = computeNewChildrenWithProblemNode(event, pathBuilder)
    val wasInsertedNewDirectorySubroot = childrenToProcess.children !== this &&
                                         childrenToProcess.children.directoryNodeChildren.size == this.directoryNodeChildren.size

    val pathBuilderToPassForward = if (wasInsertedNewDirectorySubroot) {
      val ignored = QodanaTreePath.Builder()
      ignored
    } else {
      pathBuilder
    }

    return childrenToProcess to pathBuilderToPassForward
  }

  /**
   * Having:
   * - already present nodes: [fileNodeChildren], [directoryNodeChildren]
   * - event with new problems to be added/modified
   *
   * Computes new [fileNodeChildren], [directoryNodeChildren] on this level
   *
   * For example, if [currentFullPath] is `a/` and already present nodes:
   * - a/b/c/
   * - a/x/y/
   * - a/file.txt
   *
   * And new problems in files:
   * - a/b/d/...
   * - a/x/y/file2.txt
   * - a/file3.txt
   *
   * new children will be:
   * - /a/b/ (a/b/c/ and /a/b/d got merged to /a/b/, c/ and d/ are now children of /a/b/)
   * - /a/x/y/
   * - /a/file.txt
   * - /a/file3.txt
   */
  private fun computeNewChildrenWithProblemNode(
    event: QodanaTreeProblemEvent,
    pathBuilder: QodanaTreePath.Builder
  ): NewChildrenWithMappings {
    val fileNodesMapping = mutableMapOf<QodanaTreeFileNode, MutableSet<SarifProblemWithPropertiesAndFile>>()
    val directoryNodesMapping = mutableMapOf<QodanaTreeDirectoryNode, MutableSet<SarifProblemWithPropertiesAndFile>>()

    // take problems which don't belong to already present nodes
    val notRelatedProblems = event.sarifProblemsToProperties.filter { problemWithPropertiesAndFile ->
      nodesSequence.all { node ->
        val isNodeRelated = node.isRelatedToProblem(problemWithPropertiesAndFile.problem)
        if (isNodeRelated) {
          when(node) {
            is QodanaTreeFileNode -> {
              fileNodesMapping.getOrPut(node) { mutableSetOf() }.add(problemWithPropertiesAndFile)
            }
            is QodanaTreeDirectoryNode -> {
              directoryNodesMapping.getOrPut(node) { mutableSetOf() }.add(problemWithPropertiesAndFile)
            }
          }
        }
        !isNodeRelated
      }
    }

    if (notRelatedProblems.isEmpty()) return NewChildrenWithMappings(this, fileNodesMapping, directoryNodesMapping)

    val newFileNodes = fileNodeChildren.toMutableSet()
    val newDirectoryNodes = directoryNodeChildren.toMutableSet()
    val presentDirectoryNodesByOwnPaths: MutableMap<Path, QodanaTreeDirectoryNode> = newDirectoryNodes.associateBy {
      it.primaryData.ownPath
    }.toMutableMap()
    notRelatedProblems.groupBy { it.problem.relativeNioFile }.forEach { (file, problems) ->
      val project = problems.firstOrNull()?.project ?: return@forEach

      val precomputedFile = problems.firstNotNullOfOrNull { it.file }
      val fullPath = if (file.startsWith(currentFullPath)) currentFullPath else Path("")

      val problemDirectory = file.parent ?: Path("")
      // full directory of the problem is a current directory, create file node
      if (fullPath == problemDirectory) {
        val problemVirtualFile = precomputedFile ?: project.findRelativeVirtualFile(file.toString()) ?: return@forEach
        val newFileNode = QodanaTreeFileNodeImpl.newEmpty(fullPath, file, problemVirtualFile, excludedData)

        pathBuilder.excludeNode(newFileNode)
        newFileNodes.add(newFileNode)
        fileNodesMapping[newFileNode] = problems.toMutableSet()
        return@forEach
      }

      // now we know that the problem belongs to some new directory
      val directoryNodeWithFileProblem = QodanaTreeDirectoryNodeImpl.newEmpty(
        parentPath = fullPath,
        fullPath = problemDirectory,
        excludedData = excludedData
      )

      val problemOwnPath = directoryNodeWithFileProblem.primaryData.ownPath
      // find already created directory node which shares own path with this new directory:
      // current dir: /a/
      // already created: /a/b/c/d
      // new directory above: /a/b/c/e
      // so they both have b/c/ as common, we will create a new directory node for them
      val (presentDirectoryNodeWithCommonPrefix: QodanaTreeDirectoryNode?, commonPrefixPath: Path?) =
        presentDirectoryNodesByOwnPaths
          .asSequence()
          .map { (directoryNodeOwnPath, directoryNode) ->
            directoryNode to problemOwnPath.longestCommonPrefixWith(directoryNodeOwnPath)
          }
          .filter { it.second != null }
          .firstOrNull()
        ?: (null to null)

      // no matching already created directories nodes found, create a new directory node
      if (presentDirectoryNodeWithCommonPrefix == null || commonPrefixPath == null) {
        pathBuilder.excludeNode(directoryNodeWithFileProblem)
        newDirectoryNodes.add(directoryNodeWithFileProblem)
        presentDirectoryNodesByOwnPaths[directoryNodeWithFileProblem.primaryData.ownPath] = directoryNodeWithFileProblem
        directoryNodesMapping[directoryNodeWithFileProblem] = problems.toMutableSet()
        return@forEach
      }

      // now we understand that we need to create or reuse /a/b/c directory node
      val newSubrootDirectoryPath = fullPath.resolve(commonPrefixPath)

      // there is no d/: this /a/b/c is a full path of already created directory node, just delegate events to it
      if (newSubrootDirectoryPath == presentDirectoryNodeWithCommonPrefix.primaryData.fullPath) {
        directoryNodesMapping[presentDirectoryNodeWithCommonPrefix]!!.addAll(problems)
        return@forEach
      }

      // create a new node for /a/b/c/d (with parent path /a/b/c/ and own path d/) instead of previous one
      val updatedPresentDirectoryNodeWithCommonPrefix = QodanaTreeDirectoryNodeImpl(
        QodanaTreeDirectoryNode.PrimaryData(
          fullPath = presentDirectoryNodeWithCommonPrefix.primaryData.fullPath,
          parentPath = newSubrootDirectoryPath
        ),
        QodanaTreeFileAndDirectoryNodeChildrenImpl(
          presentDirectoryNodeWithCommonPrefix.primaryData.fullPath,
          presentDirectoryNodeWithCommonPrefix.children.fileNodeChildren,
          presentDirectoryNodeWithCommonPrefix.children.directoryNodeChildren,
          excludedData
        ),
        excludedData
      )

      val (updatedDirectoryNodeWithFileProblem, fileNodeWithProblem) = if (newSubrootDirectoryPath == file.parent) {
        // there is no e/: common /a/b/c/ is a path to directory with file problem then we need a file node
        val problemVirtualFile = precomputedFile ?: project.findRelativeVirtualFile(file.toString()) ?: return@forEach

        val fileNode = QodanaTreeFileNodeImpl.newEmpty(newSubrootDirectoryPath, file, problemVirtualFile, excludedData)
        null to fileNode
      } else {
        // otherwise we need a new directory node
        val updatedPrimaryData = directoryNodeWithFileProblem.primaryData.copy(parentPath = newSubrootDirectoryPath)
        val directoryNode = QodanaTreeDirectoryNodeImpl(updatedPrimaryData, directoryNodeWithFileProblem.children, excludedData)
        directoryNode to null
      }

      // we create a new subroot directory: /a/b/c/
      val subrootDirectoryToInsert = QodanaTreeDirectoryNodeImpl(
        QodanaTreeDirectoryNode.PrimaryData(fullPath = newSubrootDirectoryPath, parentPath = fullPath),
        QodanaTreeFileAndDirectoryNodeChildrenImpl(
          currentFullPath = newSubrootDirectoryPath,
          fileNodeChildren = listOfNotNull(fileNodeWithProblem),
          directoryNodeChildren = listOfNotNull(updatedPresentDirectoryNodeWithCommonPrefix, updatedDirectoryNodeWithFileProblem),
          excludedData
        ),
        excludedData
      )

      // remove already present directory from children, add newly created
      newDirectoryNodes.apply {
        remove(presentDirectoryNodeWithCommonPrefix)
        add(subrootDirectoryToInsert)
      }
      presentDirectoryNodesByOwnPaths.remove(presentDirectoryNodeWithCommonPrefix.primaryData.ownPath)
      presentDirectoryNodesByOwnPaths[subrootDirectoryToInsert.primaryData.ownPath] = subrootDirectoryToInsert

      // delegate events to newly created dir
      val problemsFromAlreadyPresentNode = directoryNodesMapping.remove(presentDirectoryNodeWithCommonPrefix) ?: mutableSetOf()
      directoryNodesMapping[subrootDirectoryToInsert] = problemsFromAlreadyPresentNode.apply { addAll(problems) }
    }
    return NewChildrenWithMappings(
      QodanaTreeFileAndDirectoryNodeChildrenImpl(currentFullPath, newFileNodes.toList(), newDirectoryNodes.toList(), excludedData),
      fileNodesMapping,
      directoryNodesMapping
    )
  }
}

private class NewChildrenWithMappings(val children: QodanaTreeFileAndDirectoryNodeChildren,
                                      val fileChildMapping: Map<QodanaTreeFileNode, Set<SarifProblemWithPropertiesAndFile>>,
                                      val directoryChildMapping: Map<QodanaTreeDirectoryNode, Set<SarifProblemWithPropertiesAndFile>>)

private fun Path.longestCommonPrefixWith(other: Path): Path? {
  var longestPrefix: Path? = null
  for (i in this.toList().indices) {
    val currentPrefix = this.subpath(0, i + 1)
    if (other.startsWith(currentPrefix)) {
      longestPrefix = currentPrefix
    }
    else {
      break
    }
  }
  return longestPrefix
}